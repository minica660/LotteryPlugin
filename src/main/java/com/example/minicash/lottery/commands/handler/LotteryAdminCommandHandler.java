package com.example.minicash.lottery.commands.handler;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.example.minicash.library.common.response.NormalResponse;
import com.example.minicash.lottery.Lottery;
import com.example.minicash.lottery.data.ActiveLotterySession;
import com.example.minicash.lottery.data.LotteryConfig;
import com.example.minicash.lottery.database.ActiveDatabase;
import com.example.minicash.lottery.database.PlayerDatabase;
import com.example.minicash.lottery.manager.LotteryManager;
import com.example.minicash.lottery.manager.config.LotteryConfigManager;
import com.example.minicash.lottery.manager.shop.VillagerShop;
import com.example.minicash.lottery.model.ShopType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.logging.Level;

public class LotteryAdminCommandHandler {

    private final Lottery plugin;
    private final LotteryManager lotteryManager;
    private final ActiveDatabase activeDatabase;
    private final PlayerDatabase playerDatabase;
    private final LotteryConfigManager lotteryConfigManager;
    private final VillagerShop villagerShop;


    public LotteryAdminCommandHandler(Lottery plugin, LotteryManager lotteryManager, ActiveDatabase activeDatabase, PlayerDatabase playerDatabase, LotteryConfigManager lotteryConfigManager, VillagerShop villagerShop) {

        this.plugin = plugin;
        this.lotteryManager = lotteryManager;
        this.activeDatabase = activeDatabase;
        this.playerDatabase = playerDatabase;
        this.lotteryConfigManager = lotteryConfigManager;
        this.villagerShop = villagerShop;

    }


    public int handleStart(CommandContext<CommandSourceStack> ctx) {

        CommandSender sender = ctx.getSource().getSender();

        String lottoID = ctx.getArgument("lottoID", String.class);

        LotteryConfig lotteryConfig = lotteryConfigManager.getLotteryConfig(lottoID);


        if (lotteryConfig == null) {

            sender.sendMessage(Lottery.getMessage(
                    Component.text("指定した宝くじIDのデータは存在しません", NamedTextColor.RED)
            ));

            return 0;

        }


        NormalResponse result = lotteryManager.startLotto(lotteryConfig);

        NamedTextColor color = result.isSuccess() ? NamedTextColor.GREEN : NamedTextColor.RED;

        sender.sendMessage(Lottery.getMessage(Component.text(result.getMessage(), color)));

        return 1;


    }


    public int handleStop(CommandContext<CommandSourceStack> ctx) {

        CommandSender sender = ctx.getSource().getSender();

        ActiveLotterySession activeLottery = lotteryManager.getActiveLotterySession();

        if (activeLottery == null || !lotteryManager.isSessionActive()) {

            sender.sendMessage(Lottery.getMessage(Component.text("現在開催中の宝くじはありません", NamedTextColor.RED)));

            return 0;
        }


        LotteryConfig lotteryConfig = lotteryConfigManager.getLotteryConfig(activeLottery.getLottoId());

        lotteryManager.finishSession(activeLottery, lotteryConfig);

        sender.sendMessage(Lottery.getMessage(
                Component.text("開催中の" + activeLottery.getSessionId() + "宝くじを終了し、抽選を開始しました")
        ));

        return 1;

    }


    public int handleInfo(CommandContext<CommandSourceStack> ctx) {

        CommandSender sender = ctx.getSource().getSender();

        ActiveLotterySession activeLottery = lotteryManager.getActiveLotterySession();


        if (activeLottery == null || !lotteryManager.isSessionActive()) {

            sender.sendMessage(Lottery.getMessage(Component.text("現在開催中の宝くじはありません", NamedTextColor.RED)));

            return 0;

        }


        sender.sendMessage(Component.text("====== 開催中の宝くじ情報 ======", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("セッションID: ", NamedTextColor.GRAY).append(Component.text(activeLottery.getSessionId(), NamedTextColor.GREEN)));
        sender.sendMessage(Component.text("Lotto ID: ", NamedTextColor.GRAY).append(Component.text(activeLottery.getLottoId(), NamedTextColor.GREEN)));


        activeDatabase.getTotalMoney(activeLottery.getSessionId())
                .thenAccept(totalSales -> runMainThred(() -> {

                    sendPlayerMessage(sender, Component.text("現在の売上総額: ", NamedTextColor.GRAY)
                            .append(Component.text(String.format("%d 円", totalSales), NamedTextColor.YELLOW)));

                }))
                .exceptionally(ex -> {

                    runMainThred(() -> sendPlayerMessage(sender, Lottery.getMessage(Component.text(activeLottery.getSessionId() + "の売上データの取得に失敗しました", NamedTextColor.RED))));

                    plugin.getLogger().severe("売上取得エラー: " + ex.getMessage());

                    return null;


                });

        return 1;
    }


    public int handlePlayerInfo(CommandContext<CommandSourceStack> ctx) {

        CommandSender sender = ctx.getSource().getSender();

        OfflinePlayer target;


        ActiveLotterySession activeLottery = lotteryManager.getActiveLotterySession();


        if (activeLottery == null || !lotteryManager.isSessionActive()) {

            sender.sendMessage(Lottery.getMessage(Component.text("現在開催中の宝くじはありません", NamedTextColor.RED)));

            return 0;

        }


        try {

            Collection<PlayerProfile> profiles = ctx.getArgument("target", PlayerProfileListResolver.class)
                    .resolve(ctx.getSource());

            if (profiles.isEmpty()) {

                sender.sendMessage(Lottery.getMessage(
                        Component.text("指定したプレイヤーのデータが不明です", NamedTextColor.RED)
                ));

                return 0;
            }

            PlayerProfile profile = profiles.iterator().next();

            target = Bukkit.getOfflinePlayer(profile.getId());

        } catch (Exception e) {

            sender.sendMessage(Lottery.getMessage(
                    Component.text("指定したプレイヤーのデータ取得中に予期せぬエラーが発生しました:" + e.getMessage(), NamedTextColor.RED)
            ));

            return 0;
        }


        String sessionID = activeLottery.getSessionId();


        playerDatabase.getTicketCount(target.getUniqueId(), sessionID).thenAccept(count -> runMainThred(() -> {

                    sendPlayerMessage(sender, Component.text("===== " + target.getName() + " の宝くじ情報 =====", NamedTextColor.GOLD));

                    sendPlayerMessage(sender, Component.text("購入済み枚数: ", NamedTextColor.GRAY)
                            .append(Component.text(count + " 枚", NamedTextColor.GREEN)));

                }))
                .exceptionally(ex -> {
                    runMainThred(() -> sendPlayerMessage(sender, Lottery.getMessage(Component.text(sessionID + ":プレイヤー宝くじ購入枚数の取得に失敗しました", NamedTextColor.RED))));
                    plugin.getLogger().log(Level.SEVERE, sessionID + "プレイヤーの宝くじ購入枚数取得中に予期せぬエラーが発生しました ", ex);
                    return null;
                });


        return 1;


    }

    public int handleSpawnShopVillager(CommandContext<CommandSourceStack> ctx) {

        CommandSender sender = ctx.getSource().getSender();

        ShopType shopType;

        try {

            shopType = ShopType.valueOf(ctx.getArgument("shoptype", String.class));

        } catch (IllegalArgumentException e) {
            sender.sendMessage(Lottery.getMessage(
                    Component.text("ショップタイプが不明です", NamedTextColor.RED)
            ));
            return 0;
        }



        if (sender instanceof Player player) {

            villagerShop.spawnVillager(player.getLocation(), shopType);

            player.sendMessage(Lottery.getMessage(
                    Component.text(shopType.getDisplayName() + "をスポーンさせました", NamedTextColor.GOLD)
            ));


        } else {
            sender.sendMessage(Lottery.getMessage(
                    Component.text("このコマンドはプレイヤーのみ実行可能です", NamedTextColor.RED)
            ));
        }

        return 1;

    }


    // 必ずメインスレッドで実行させるためのメソッド！
    private void runMainThred(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }


    private void sendPlayerMessage(CommandSender sender, Component message) {

        if (sender instanceof Player player) {

            if (player.isOnline()) {
                player.sendMessage(message);
            }

        } else if (sender != null) {
            sender.sendMessage(message);
        }


    }


}

