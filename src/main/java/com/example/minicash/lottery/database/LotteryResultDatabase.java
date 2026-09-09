package com.example.minicash.lottery.database;

import com.example.minicash.lottery.data.ActiveLotterySession;
import com.example.minicash.lottery.data.DrawLotteryResult;
import com.example.minicash.lottery.data.LotteryConfig;
import com.example.minicash.lottery.data.PrizeReward;
import com.example.minicash.lottery.model.RewardType;
import com.example.minicash.lottery.response.DrawLottoResultResponse;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class LotteryResultDatabase {

    private final HikariDataSource hikariDataSource;

    public LotteryResultDatabase(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }


    public CompletableFuture<DrawLottoResultResponse> createLotteryResult(ActiveLotterySession activeLotterySession, LotteryConfig lotteryConfig , double totalPrizePool){


        Random random = new Random();
        LocalDateTime expiryTime = LocalDateTime.now().plusDays(lotteryConfig.getClaimExpiryDays());


        return CompletableFuture.supplyAsync(() ->{


            String sql = """
                INSERT INTO `lottery_results`
                (`session_id`, `prize_type`, `win_group`, `win_number`, `reward_type`, `reward_value`, `expiry_time`)
                VALUES (?, ?, ?, ?, ?, ?, ?);
            """;

            List<DrawLotteryResult> drawnResults = new ArrayList<>();

            try (Connection connection = hikariDataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {

                for (PrizeReward prize : lotteryConfig.getPrizeList()) {


                    // MONEYかITEM
                    RewardType rewardType = prize.getRewardType();
                    String rewardValueString;

                    if (rewardType == RewardType.MONEY) {

                        // 賞金分配率から賞金をきめる
                        int prizeValue = (int) (totalPrizePool * prize.getPoolShare());

                        rewardValueString = String.valueOf(prizeValue);

                    }else if (rewardType == RewardType.ITEM) {

                        rewardValueString = prize.getItemBase64();

                    }else {

                        rewardValueString = "";

                    }


                    // 当選番号のランダム生成
                    int winGroup = random.nextInt(lotteryConfig.getMaxGroup()) + 1;
                    int winNumber = random.nextInt(lotteryConfig.getMaxNumber() + 1);

                    pstmt.setString(1, activeLotterySession.getSessionId());
                    pstmt.setString(2, prize.getDisplayName()); // 何等かどうか
                    pstmt.setInt(3, winGroup);
                    pstmt.setInt(4, winNumber);
                    pstmt.setString(5, prize.getRewardType().name());// MONEYかITEM
                    pstmt.setString(6, rewardValueString);
                    pstmt.setTimestamp(7, Timestamp.valueOf(expiryTime));

                    pstmt.addBatch();

                    drawnResults.add(new DrawLotteryResult(prize.getDisplayName(), winGroup, winNumber, rewardType , rewardValueString));

                }

                pstmt.executeBatch();

                return new DrawLottoResultResponse(true,"抽選結果のDB保存が完了しました" , drawnResults);

            } catch (SQLException e) {
                return new DrawLottoResultResponse(false,"抽選結果のDB保存中にエラーが発生しました: " + e.getMessage() , null);
            }



        });




    }


}
