package com.example.minicash.lottery.manager;

import com.example.minicash.library.common.response.NormalResponse;
import com.example.minicash.lottery.data.ActiveLotterySession;
import com.example.minicash.lottery.data.LotteryConfig;
import com.example.minicash.lottery.database.ActiveDatabase;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class LotteryManager {

    private final JavaPlugin plugin;
    private final ActiveDatabase activeDatabase;

    private ActiveLotterySession activeLotterySession = null;

    public LotteryManager(JavaPlugin plugin, ActiveDatabase activeDatabase) {
        this.plugin = plugin;
        this.activeDatabase = activeDatabase;
    }


    /**
     * 新しい宝くじの開催！
     */
    public NormalResponse startLotto(LotteryConfig lotteryConfig){

        if(isSessionActive()){
            return new NormalResponse(false,"既に宝くじが開催されているため宝くじ" + lotteryConfig.getDisplayName() + "をスタートさせることが出来ません")
        }


        String sessionID = UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusMinutes(lotteryConfig.getDurationMinutes());

        ActiveLotterySession session = new ActiveLotterySession(sessionID, lotteryConfig.getLottoID(), now, endTime);

        activeDatabase.createSession(sessionID, lotteryConfig.getLottoID(), now, endTime).thenAccept(success -> {

            if (success) {

                this.activeLotterySession = session;

                scheduleEndTask(session);

                plugin.getLogger().info("宝くじ「" + lotteryConfig.getDisplayName() + "」を開催しました！ ID: " + sessionID);


            }

        });



        return new NormalResponse(true,"宝くじ「" + lotteryConfig.getDisplayName() + "」を開催しました！ ID: " + sessionID);


    }




    /**
     * サーバー起動時に呼び出す復帰処理
     */
    public void loadActiveSessionOnStartup() {

        activeDatabase.loadActiveSession().thenAccept(session -> {

            Bukkit.getScheduler().runTask(plugin, () -> {

                if (session == null) {
                    plugin.getLogger().info("現在開催中の宝くじはありません");
                    return;
                }


                if (session.isExpired()) {

                    plugin.getLogger().info("停止中に期限切れになった宝くじデータを終了処理します: " + session.getSessionId());
                    finishSession(session);

                } else {

                    this.activeLotterySession = session;

                    scheduleEndTask(session);

                    long remainingMinutes = Duration.between(LocalDateTime.now(), session.getEndTime()).toMinutes();

                    plugin.getLogger().info("アクティブな宝くじデータを復帰しました: " + session.getLottoId() + " (残り " + remainingMinutes + " 分)");

                }
            });
        });
    }

    /**
     * 終了時刻までの時刻登録
     */
    private void scheduleEndTask(ActiveLotterySession session) {
        long delaySeconds = Duration.between(LocalDateTime.now(), session.getEndTime()).getSeconds();
        if (delaySeconds <= 0) {
            finishSession(session);
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            finishSession(session);
        }, delaySeconds * 20L);

    }

    /**
     * 宝くじの終了処理
     */
    public void finishSession(ActiveLotterySession session) {

        if (activeLotterySession != null && activeLotterySession.getSessionId().equals(session.getSessionId())) {
            this.activeLotterySession = null;
        }

        activeDatabase.deactivateSession(session.getSessionId()).thenAccept(v -> {

            Bukkit.getScheduler().runTask(plugin, () -> {

                plugin.getLogger().info("宝くじセッションが終了しました: " + session.getSessionId());
                // TODO: 抽選処理（lottery_results への書き込み等）の呼び出し

            });
        });
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
