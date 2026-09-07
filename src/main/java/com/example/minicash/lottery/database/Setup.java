package com.example.minicash.lottery.database;

import com.example.minicash.library.common.response.NormalResponse;
import com.example.minicash.lottery.Lottery;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class Setup {

    private static Lottery plugin;

    private String URL;
    private String USER;
    private String PASSWORD;

    private HikariDataSource hikariSe;

    public Setup(Lottery plugin) {
        Setup.plugin = plugin;

        URL = "jdbc:mysql://"
                + plugin.getConfig().getString("mysql.host")
                + ":" + plugin.getConfig().getInt("mysql.port")
                + "/" + plugin.getConfig().getString("mysql.database")
                + "?useSSL=true&autoReconnect=true&serverTimezone=Asia/Tokyo";
        USER = plugin.getConfig().getString("mysql.user");
        PASSWORD = plugin.getConfig().getString("mysql.password");

    }

    public HikariDataSource getHikariSe() {
        return hikariSe;
    }

    public void connect() {
        if (hikariSe != null && !hikariSe.isClosed()) {
            plugin.getLogger().warning("既にプールが存在します");
            return;
        }

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(10);  // 接続数
        config.setMinimumIdle(2);
        config.setConnectionTimeout(4000);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        try {
            //db接続
            hikariSe = new HikariDataSource(config);

            plugin.getLogger().info("Mysqlデータベースへの接続が完了しました");

            NormalResponse result =  setupTable();

            if(result.isSuccess()){
                plugin.getLogger().info("テーブルの設定が完了しました");
            }else {
                plugin.getLogger().severe(result.getMessage());
            }

        } catch (Exception e) {

            plugin.getLogger().severe("Mysqlデータベースへの接続に失敗しました: " + e.getMessage());


        }


    }

    public void disConnect() {
        if (hikariSe != null && !hikariSe.isClosed()) {
            hikariSe.close();
            plugin.getLogger().info("データベースの接続プールの切断に成功しました");
        }
    }


    public NormalResponse setupTable() {

        // 現在開催中の宝くじ売り上げプール
        String activeSql = """
                
                    CREATE TABLE IF NOT EXISTS `active_lottery_pool` (
                    `session_id` VARCHAR(64) NOT NULL,
                    `lotto_id` VARCHAR(64) NOT NULL,
                    `total_sales` DOUBLE NOT NULL,
                    `start_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    `end_time` TIMESTAMP NOT NULL,
                    `is_active` BOOLEAN NOT NULL DEFAULT TRUE
                    );
                """;

        String playerPurchaseCounts = """
                
                    CREATE TABLE IF NOT EXISTS `player_purchase_counts` (
                    `session_id` VARCHAR(64) NOT NULL,
                    `player_uuid` VARCHAR(36) NOT NULL,
                    `ticket_count` INT NOT NULL

                    );
                """;

        String lotteryResults = """
                
                    CREATE TABLE IF NOT EXISTS `lottery_results` (
                    `session_id` VARCHAR(64) NOT NULL,
                    `prize_type` VARCHAR(32) NOT NULL,
                    `win_group` INT NOT NULL,
                    `win_number` INT NOT NULL,
                    `reward_type` VARCHAR(16) NOT NULL,
                    `reward_value` LONGTEXT NOT NULL,
                    `expiry_time` TIMESTAMP NOT NULL

                    );
                """;


        String claimed_tickets = """
                
                    CREATE TABLE IF NOT EXISTS `claimed_tickets` (
                    `ticket_uuid` VARCHAR(36) NOT NULL,
                    `claimed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP

                    );
                """;


        try (Connection connection = hikariSe.getConnection();
             PreparedStatement activeStmt = connection.prepareStatement(activeSql);
             PreparedStatement playerPurchaseStmt = connection.prepareStatement(playerPurchaseCounts);
             PreparedStatement lotteryResultStmt = connection.prepareStatement(lotteryResults);
             PreparedStatement claimedStmt = connection.prepareStatement(claimed_tickets)) {
            activeStmt.executeUpdate();
            playerPurchaseStmt.executeUpdate();
            lotteryResultStmt.executeUpdate();
            claimedStmt.executeUpdate();

            plugin.getLogger().info("bank_itemsテーブルを作成しました");

            return new NormalResponse(true,"正常にテーブル作成完了");

        } catch (SQLException e) {

            plugin.getLogger().log(Level.SEVERE, "DBのテーブル生成中にエラーが発生しました", e);

            return new NormalResponse(false,e.getMessage());
        }



    }


}
