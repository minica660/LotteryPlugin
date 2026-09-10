package com.example.minicash.lottery.manager;

import com.example.minicash.library.common.response.NormalResponse;
import com.example.minicash.lottery.Lottery;
import com.example.minicash.lottery.data.ActiveLotterySession;
import com.example.minicash.lottery.data.LotteryConfig;
import com.example.minicash.lottery.database.ActiveDatabase;
import com.example.minicash.lottery.database.LotteryResultDatabase;
import com.example.minicash.lottery.manager.config.LotteryConfigManager;
import com.example.minicash.lottery.data.DrawLotteryResult;
import com.example.minicash.lottery.util.ItemSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.logging.Level;

public class LotteryManager {

    private final JavaPlugin plugin;
    private final ActiveDatabase activeDatabase;
    private final LotteryResultDatabase lotteryResultDatabase;

    private final LotteryConfigManager lotteryConfigManager;

    private ActiveLotterySession activeLotterySession = null;


    private BukkitTask lottoBukkitTask = null;


    public LotteryManager(JavaPlugin plugin, ActiveDatabase activeDatabase, LotteryResultDatabase lotteryResultDatabase,LotteryConfigManager lotteryConfigManager) {
        this.plugin = plugin;
        this.activeDatabase = activeDatabase;
        this.lotteryResultDatabase = lotteryResultDatabase;
        this.lotteryConfigManager = lotteryConfigManager;
    }


    /**
     * 新しい宝くじの開催！
     */
    public NormalResponse startLotto(LotteryConfig lotteryConfig) {

        if (isSessionActive()) {
            return new NormalResponse(false, "既に宝くじが開催されているため宝くじ" + lotteryConfig.getDisplayName() + "をスタートさせることが出来ません");
        }


        if(lottoBukkitTask != null){
            lottoBukkitTask.cancel();
        }


        String sessionID = UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusMinutes(lotteryConfig.getDurationMinutes());

        ActiveLotterySession session = new ActiveLotterySession(sessionID, lotteryConfig.getLottoID(), now, endTime);

        activeDatabase.createLotto(sessionID, lotteryConfig.getLottoID(), now, endTime).thenAccept(success -> {

            Bukkit.getScheduler().runTask(plugin, () -> {

                if (success) {

                    this.activeLotterySession = session;

                    scheduleEndTask(session, lotteryConfig);

                    Bukkit.getOnlinePlayers().forEach(player -> {

                        player.showBossBar(lotteryConfig.getBossBar());

                    });


                    startBukkitTask(activeLotterySession,lotteryConfig);


                    plugin.getLogger().info("宝くじ「" + lotteryConfig.getDisplayName() + "」を開催しました！ ID: " + sessionID);


                } else {
                    plugin.getLogger().warning("宝くじを開催しようとしましたが実行されませんでした");
                }


            });


        });


        return new NormalResponse(true, "宝くじ「" + lotteryConfig.getDisplayName() + "」を開催しました！ ID: " + sessionID);


    }


    /**
     * サーバー起動時に呼び出す復帰処理
     */
    public void loadActiveLottery() {

        activeDatabase.loadActiveSession().thenAccept(activeLottery -> {

            Bukkit.getScheduler().runTask(plugin, () -> {

                if (activeLottery == null) {
                    plugin.getLogger().info("現在開催中の宝くじはありません");
                    return;
                }




                if (activeLottery.isExpired()) {

                    LotteryConfig lotteryConfig = lotteryConfigManager.getLotteryConfig(activeLottery.getLottoId());

                    if(lotteryConfig == null){
                        plugin.getLogger().warning("サーバー停止期間中に終了した宝くじがありましたがConfigデータがnullだったため処理が出来ませんでした");
                        return;
                    }

                    plugin.getLogger().info("停止中に期限切れになった宝くじデータを終了処理します: " + activeLottery.getSessionId());
                    finishSession(activeLottery,lotteryConfig);

                } else {

                    this.activeLotterySession = activeLottery;

                    LotteryConfig lotteryConfig = lotteryConfigManager.getLotteryConfig(activeLottery.getLottoId());

                    if(lotteryConfig == null){
                        plugin.getLogger().warning("開催中の宝くじがありましたがConfigデータがnullだったため処理が停止しました");
                        return;
                    }

                    scheduleEndTask(activeLottery,lotteryConfig);

                    startBukkitTask(activeLottery,lotteryConfig);

                    long remainingMinutes = Duration.between(LocalDateTime.now(), activeLottery.getEndTime()).toMinutes();

                    plugin.getLogger().info("アクティブな宝くじデータを復帰しました: " + activeLottery.getLottoId() + " (残り " + remainingMinutes + " 分)");

                }


            });


        });
    }

    /**
     * 終了時刻までの時刻登録
     */
    private void scheduleEndTask(ActiveLotterySession session,LotteryConfig lotteryConfig) {

        long delaySeconds = Duration.between(LocalDateTime.now(), session.getEndTime()).getSeconds();

        if (delaySeconds <= 0) {

            finishSession(session,lotteryConfig);

            return;

        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            finishSession(session,lotteryConfig);

        }, delaySeconds * 20L);


    }

    /**
     * 宝くじの終了処理
     */
    public void finishSession(ActiveLotterySession session,LotteryConfig lotteryConfig) {

        stopBukkitTask();

        if (lotteryConfig != null && lotteryConfig.getBossBar() != null) {

            Bukkit.getOnlinePlayers().forEach(player -> {
                player.hideBossBar(lotteryConfig.getBossBar());
            });


        }

        if (activeLotterySession != null && activeLotterySession.getSessionId().equals(session.getSessionId())) {
            this.activeLotterySession = null;
        }

        activeDatabase.deactivateSession(session.getSessionId()).thenAccept(v -> {

            Bukkit.getScheduler().runTask(plugin, () -> {

                plugin.getLogger().info("宝くじが終了しました: " + session.getSessionId());

                processDraw(session, lotteryConfig);


            });
        });
    }


    /**
     * 宝くじの抽選を行うメソッド！
     */
    public void processDraw(ActiveLotterySession activeSession, LotteryConfig lotteryConfig) {

        activeDatabase.getTotalMoney(activeSession.getSessionId()).thenCompose(totalSales -> {

                    // 全体の賞金として渡せる額
                    double totalPrizePool = totalSales * lotteryConfig.getTotalReturnRate();


                    return lotteryResultDatabase.createLotteryResult(activeSession, lotteryConfig, totalPrizePool);


                })
                .thenAccept(result -> {

                    if (!result.isSuccess()) {
                        plugin.getLogger().severe(result.getMessage());
                        return;
                    }


                    Server server = plugin.getServer();

                    Bukkit.getScheduler().runTask(plugin, () -> {

                        server.broadcast(Component.text("========== " + lotteryConfig.getDisplayName() + "の抽選結果！！ ==========", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));

                        for (DrawLotteryResult drawResult : result.getDrawLotteryResults()) {

                            Component rewardMessage = Component.text("???");

                            switch (drawResult.rewardType()){
                                case MONEY -> rewardMessage = Component.text("(賞金: " + drawResult.rewardValue() + "円)", NamedTextColor.YELLOW);
                                case ITEM -> {

                                    ItemStack item = ItemSerializer.deitemSerializer(drawResult.rewardValue());

                                    ItemMeta itemMeta = item.getItemMeta();

                                    if(itemMeta.hasDisplayName()){
                                        rewardMessage = Component.text("賞品： " + itemMeta.displayName(),NamedTextColor.YELLOW);
                                    }else if (itemMeta.hasItemName()) {

                                        rewardMessage = Component.text("賞品： " + itemMeta.itemName(),NamedTextColor.YELLOW);

                                    }else {

                                        String translationKey = item.getType().getItemTranslationKey();

                                        if (translationKey == null) {
                                            translationKey = item.getType().getBlockTranslationKey();
                                        }

                                        Component materialKey = Component.translatable(translationKey);

                                        rewardMessage = Component.text("賞品： " + materialKey ,NamedTextColor.YELLOW);


                                    }


                                }
                            }


                            String winNumberString = String.format("%06d", drawResult.winNumber());

                            Component prizeText = Component.text("・ " + drawResult.prizeName() + ": ", NamedTextColor.AQUA)
                                    .append(Component.text(drawResult.winGroup() + "組 ", NamedTextColor.WHITE))
                                    .append(Component.text(winNumberString + "番 ", NamedTextColor.GREEN))
                                    .append(rewardMessage);


                            server.broadcast(prizeText);


                        }


                        server.broadcast(Component.text("=========================", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));


                    });


                }).exceptionally(ex -> {

                    plugin.getLogger().log(Level.SEVERE, "processDrawメソッド内で予期せぬエラーが発生しました ", ex);

                    return null;

                });

    }



    /**
     *
     */
    public void startBukkitTask(ActiveLotterySession activeLottery , LotteryConfig lotteryConfig){

        int count = 0;

        // BukkitTask処理
        lottoBukkitTask = Bukkit.getScheduler().runTaskTimer(plugin , new Runnable(){

            private int count;

            @Override
            public void run() {

                if (!isSessionActive()) {
                    stopBukkitTask();
                    return;
                }

                if (count == 3) {

                    Bukkit.getOnlinePlayers().forEach(player -> sendLotteryInfo(player));

                    count = 0;

                }


                String rawTitle = lotteryConfig.getBossBarTitle();
                Component updatedTitle = formatBossBarTitle(rawTitle, activeLottery);

                lotteryConfig.getBossBar().name(updatedTitle);


            }

        },0L , 1200L);

    }

    public void stopBukkitTask(){


        if(lottoBukkitTask != null){
            lottoBukkitTask.cancel();
            lottoBukkitTask = null;
        }


    }



    /**
     * ボスバーの特殊文字を置きかえる
     */
    public Component formatBossBarTitle(String title, ActiveLotterySession activeLottery) {

        Duration duration = Duration.between(LocalDateTime.now(), activeLottery.getEndTime());
        long totalSecond = duration.getSeconds();

        String timeString;

        if (totalSecond <= 0) {

            timeString = "終了間近";

        }else {

            long days = duration.toDays();
            long hours = duration.toHoursPart();
            long minutes = duration.toMinutesPart();

            if (days > 0) {

                //残り期間が1日以上の場合
                timeString = String.format("%d日 %d時間", days, hours);

            } else if (duration.toHours() > 0) {

                // 残り期間が１日未満で１時間以上なら
                timeString = String.format("%d時間 %d分", hours, minutes);
            } else {

                // 残り時間が１時間未満なら
                timeString = String.format("%d分", minutes);

            }
        }

        String formattedText = title.replace("%time%", timeString);
        return Component.text(formattedText);
    }


    /**
     * プレイヤー参加時のボスバー表示
     */
    public void showBossBar(Player player) {

        if (isSessionActive()) {

            LotteryConfig lotteryConfig = lotteryConfigManager.getLotteryConfig(getActiveLotterySession().getLottoId());

            if (lotteryConfig != null && lotteryConfig.getBossBar() != null) {
                player.showBossBar(lotteryConfig.getBossBar());
            }

        }

    }

    /**
     * プレイヤー退出時のボスバー非表示
     */
    public void hideBossBar(Player player) {

        if (isSessionActive()) {

            LotteryConfig lotteryConfig = lotteryConfigManager.getLotteryConfig(getActiveLotterySession().getLottoId());

            if (lotteryConfig != null && lotteryConfig.getBossBar() != null) {
                player.hideBossBar(lotteryConfig.getBossBar());
            }

        }

    }



    /**
     * プレイヤーがゲームに参加した際に送信するメッセージ
     */
    public void sendLotteryInfo(Player player){

        if(isSessionActive()) {

            ActiveLotterySession activeLottery = getActiveLotterySession();
            LotteryConfig lotteryConfig = lotteryConfigManager.getLotteryConfig(getActiveLotterySession().getLottoId());


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
            String endTimeString = activeLottery.getEndTime().format(formatter);

            // 残り時間の計算
            Duration remaining = Duration.between(LocalDateTime.now(), activeLottery.getEndTime());
            long hours = remaining.toHours();
            long minutes = remaining.toMinutesPart();

            String timeString = (hours > 0)
                    ? String.format("%d時間 %d分", hours, minutes)
                    : String.format("%d分", minutes);


            player.sendMessage(Lottery.getMessage(
                    Component.text("========== 宝くじ開催情報！ ==========", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
            ));


            player.sendMessage(Lottery.getMessage(
                    Component.text("")
            ));

            player.sendMessage(Lottery.getMessage(
                    Component.text("開催中宝くじ：" + lotteryConfig.getDisplayName())
            ));

            player.sendMessage(Lottery.getMessage(
                    Component.text("残り時間：" + endTimeString + "( " + timeString +" )")
            ));

            player.sendMessage(Lottery.getMessage(
                    Component.text("")
            ));


            player.sendMessage(Lottery.getMessage(
                    Component.text("==============================", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
            ));



        }

    }




    /**
     * 現在アクティブな宝くじを取得
     */
    public ActiveLotterySession getActiveLotterySession() {
        return activeLotterySession;
    }

    /**
     * 現在宝くじが開催中かどうか
     */
    public boolean isSessionActive() {
        return activeLotterySession != null && !activeLotterySession.isExpired();
    }

}
