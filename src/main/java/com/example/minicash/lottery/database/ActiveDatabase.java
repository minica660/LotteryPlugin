package com.example.minicash.lottery.database;

import com.example.minicash.library.common.response.NormalResponse;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ActiveDatabase {

    private final HikariDataSource hikariDataSource;

    public ActiveDatabase(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }


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


}
