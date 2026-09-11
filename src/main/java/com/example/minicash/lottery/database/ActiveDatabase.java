package com.example.minicash.lottery.database;

import com.example.minicash.library.common.response.NormalResponse;
import com.example.minicash.lottery.data.ActiveLotterySession;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public class ActiveDatabase {

    private final HikariDataSource hikariDataSource;

    public ActiveDatabase(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }


    /**
     * 新規宝くじを作成してDBに保存
     */
    public CompletableFuture<Boolean> createLotto(String sessionId, String lottoId, LocalDateTime startTime, LocalDateTime endTime) {

        return CompletableFuture.supplyAsync(() -> {

            String deactivateSql = "UPDATE `active_lottery_pool` SET `is_active` = FALSE WHERE `is_active` = TRUE";
            String insertSql = """
                    INSERT INTO `active_lottery_pool` (`session_id`, `lotto_id`, `total_sales`, `start_time`, `end_time`, `is_active`)
                    VALUES (?, ?, 0, ?, ?, TRUE)
                """;

            try (Connection connection = hikariDataSource.getConnection()) {


                connection.setAutoCommit(false);


                try {
                    try (PreparedStatement deactivateStmt = connection.prepareStatement(deactivateSql)) {
                        deactivateStmt.executeUpdate();
                    }

                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {

                        insertStmt.setString(1, sessionId);
                        insertStmt.setString(2, lottoId);
                        insertStmt.setTimestamp(3, Timestamp.valueOf(startTime));
                        insertStmt.setTimestamp(4, Timestamp.valueOf(endTime));
                        insertStmt.executeUpdate();

                    }

                    connection.commit();
                    return true;

                } catch (SQLException e) {
                    connection.rollback();
                    e.printStackTrace();
                    return false;
                } finally {
                    connection.setAutoCommit(false);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }


        });


    }

    /**
     * サーバー起動時用：現在アクティブな単一セッションを取得
     *
     * @return 存在しなければnullを返す
     */
    public CompletableFuture<ActiveLotterySession> loadActiveSession() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                        SELECT `session_id`, `lotto_id`, `start_time`, `end_time`
                        FROM `active_lottery_pool`
                        WHERE `is_active` = TRUE
                        LIMIT 1;
                    """;

            try (Connection connection = hikariDataSource.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    String sessionId = rs.getString("session_id");
                    String lottoId = rs.getString("lotto_id");
                    LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
                    LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();

                    return new ActiveLotterySession(sessionId, lottoId, startTime, endTime);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }


            return null;

        });

    }

    /**
     * セッション終了時に is_activeを falseに更新させる
     */
    public CompletableFuture<Void> deactivateSession(String sessionId) {

        return CompletableFuture.runAsync(() -> {

            String sql = """
                        UPDATE `active_lottery_pool`
                        SET `is_active` = FALSE
                        WHERE `session_id` = ?;
                    """;


            try (Connection connection = hikariDataSource.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(sql)) {


                stmt.setString(1, sessionId);
                stmt.executeUpdate();


            } catch (SQLException e) {

                e.printStackTrace();

            }

        });

    }


    /**
     * 売り上げ金を追加します
     *
     * @param sessionID 開催中の宝くじ識別ID
     * @param amount    金額
     * @return NormalResponse
     */
    public CompletableFuture<NormalResponse> addTotalMoney(String sessionID, int amount) {

        return CompletableFuture.supplyAsync(() -> {


            String updateSql = """
                        UPDATE active_lottery_pool 
                        SET total_sales = total_sales + ? 
                        WHERE session_id = ?;
                    """;

            try (Connection connection = hikariDataSource.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(updateSql)) {

                stmt.setDouble(1, amount);
                stmt.setString(2, sessionID);

                int updatedRows = stmt.executeUpdate();

                if (updatedRows > 0) {
                    return new NormalResponse(true, "売上金追加完了");
                } else {
                    return new NormalResponse(false, "該当する session_id が見つかりませんでした");
                }

            } catch (SQLException e) {
                return new NormalResponse(false, "データベースエラーが発生しました: " + e.getMessage());
            }


        });


    }


    /**
     * 指定したsession_idの総合売上金額を取得
     *
     * @param sessionID 対象のセッションID
     * @return 売上金額（存在しない場合は 0）
     */
    public CompletableFuture<Integer> getTotalMoney(String sessionID) {

        return CompletableFuture.supplyAsync(() -> {

            String sql = """
                        SELECT `total_sales` 
                        FROM `active_lottery_pool` 
                        WHERE `session_id` = ?;
                    """;

            try (Connection connection = hikariDataSource.getConnection();
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {

                pstmt.setString(1, sessionID);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("total_sales");
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;

        });

    }


}
