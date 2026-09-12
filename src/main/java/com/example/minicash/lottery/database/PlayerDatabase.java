package com.example.minicash.lottery.database;

import com.example.minicash.library.common.response.NormalResponse;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerDatabase {

    private final HikariDataSource hikariDataSource;

    public PlayerDatabase(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }

    public CompletableFuture<NormalResponse> addTicket(UUID uuid, String sessionID, int amount, int maxAmount) {

        return CompletableFuture.supplyAsync(() -> {

            String selectSql = """
                        SELECT `ticket_count` FROM `player_purchase_counts`
                        WHERE `session_id` = ? AND `player_uuid` = ?
                    """;

            String insertSql = """
                        INSERT INTO `player_purchase_counts` (`session_id`, `player_uuid`, `ticket_count`)
                        VALUES (?, ?, ?)
                        AS new_val ON DUPLICATE KEY UPDATE `ticket_count` = `ticket_count` + new_val.`ticket_count`
                    """;

            try (Connection connection = hikariDataSource.getConnection()) {

                connection.setAutoCommit(false);

                try {
                    int currentCount = 0;

                    try (PreparedStatement selectStmt = connection.prepareStatement(selectSql + " FOR UPDATE")) {
                        selectStmt.setString(1, sessionID);
                        selectStmt.setString(2, uuid.toString());
                        try (ResultSet rs = selectStmt.executeQuery()) {
                            if (rs.next()) {
                                currentCount = rs.getInt("ticket_count");
                            }
                        }
                    }

                    if (currentCount + amount > maxAmount) {
                        connection.rollback();
                        return new NormalResponse(false, "購入上限（" + maxAmount + "枚）を超えるため追加できません。現在の所持数: " + currentCount);
                    }

                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setString(1, sessionID);
                        insertStmt.setString(2, uuid.toString());
                        insertStmt.setInt(3, amount);

                        insertStmt.executeUpdate();
                    }

                    connection.commit();
                    return new NormalResponse(true, "Successfully updated ticket count.");

                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                } finally {
                    connection.setAutoCommit(true);
                }

            } catch (SQLException e) {
                return new NormalResponse(false, "Database error: " + e.getMessage());
            }


        });


    }


    /**
     * 指定したプレイヤーの現在のチケット購入枚数を取得
     *
     * @param uuid      プレイヤーのUUID
     * @param sessionID セッションID
     * @return 購入枚数（レコードが存在しない場合は0）
     */
    public CompletableFuture<Integer> getTicketCount(UUID uuid, String sessionID) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                        SELECT `ticket_count` FROM `player_purchase_counts`
                        WHERE `session_id` = ? AND `player_uuid` = ?;
                    """;

            try (Connection connection = hikariDataSource.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(sql)) {

                stmt.setString(1, sessionID);
                stmt.setString(2, uuid.toString());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("ticket_count");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;
        });
    }

}
