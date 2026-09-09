package com.example.minicash.lottery.database;

import com.example.minicash.lottery.data.LotteryResultData;
import com.example.minicash.lottery.model.ClaimDBResult;
import com.example.minicash.lottery.model.RewardType;
import com.example.minicash.lottery.response.ClaimResuponse;
import com.zaxxer.hikari.HikariDataSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClaimeTicketDatabase {

    private final HikariDataSource hikariDataSource;
    private final JavaPlugin plugin;

    public ClaimeTicketDatabase(JavaPlugin plugin,HikariDataSource hikariDataSource) {
        this.plugin = plugin;
        this.hikariDataSource = hikariDataSource;
    }



    public CompletableFuture<ClaimResuponse> claimTicket(Player player, String ticketUUID , String sessionID , int group , int number , ItemStack item){

        return CompletableFuture.supplyAsync(() ->{


            try(Connection connection = hikariDataSource.getConnection()){

                connection.setAutoCommit(false);

                try {

                    // 二重換金チェック
                    String checkClaimedSql = "SELECT 1 FROM `claimed_tickets` WHERE `ticket_uuid` = ?;";

                    try (PreparedStatement stmt = connection.prepareStatement(checkClaimedSql)) {

                        stmt.setString(1, ticketUUID);

                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {

                                connection.rollback();

                                return new ClaimResuponse(ClaimDBResult.REDEEMED , Component.text("この宝くじは既に換金されています", NamedTextColor.RED),null , null  , null);
                            }
                        }
                    }



                    // 当選・期限チェック
                    String selectResultsSql = "SELECT * FROM `lottery_results` WHERE `session_id` = ?;";

                    List<LotteryResultData> results = new ArrayList<>();

                    try(PreparedStatement pstmt = connection.prepareStatement(selectResultsSql)) {

                        pstmt.setString(1,sessionID);

                        try(ResultSet rs = pstmt.executeQuery()) {

                            while (rs.next()) {

                                results.add(new LotteryResultData(
                                        rs.getString("prize_type"),
                                        rs.getInt("win_group"),
                                        rs.getInt("win_number"),
                                        RewardType.valueOf(rs.getString("reward_type")),
                                        rs.getString("reward_value"),
                                        rs.getTimestamp("expiry_time").toLocalDateTime()
                                ));

                            }

                        }

                    }



                    if(results.isEmpty()){

                        connection.rollback();

                        return new ClaimResuponse(ClaimDBResult.PENDING , Component.text("この回の宝くじはまだ抽選が行われていません！",NamedTextColor.RED),null , null , null);

                    }

                    if(LocalDateTime.now().isAfter(results.get(0).getExpiryTime())){

                        connection.rollback();

                        return new ClaimResuponse( ClaimDBResult.EXPIRED, Component.text("この宝くじの換金期限は切れています",NamedTextColor.RED),null , null , null);

                    }


                    LotteryResultData resultData = null;

                    for (LotteryResultData data : results){

                        if (data.getWinGroup() == group && data.getWinNumber() == number) {
                            resultData = data;
                            break;
                        }

                    }

                    if(resultData == null){

                        connection.rollback();

                        return new ClaimResuponse( ClaimDBResult.LOSE , Component.text("このチケットは外れています！",NamedTextColor.RED),null , null ,null);

                    }


                    // 換金済みチケット一覧に追加
                    String insertClaimSql = "INSERT INTO `claimed_tickets` (`ticket_uuid`) VALUES (?);";

                    try(PreparedStatement pstmt = connection.prepareStatement(insertClaimSql)) {

                        pstmt.setString(1, ticketUUID);

                        pstmt.executeUpdate();


                    }


                    connection.commit();

                    final RewardType rewardType = resultData.getRewardType();
                    final String rewardValue = resultData.getRewardValue();
                    final String prizeType = resultData.getPrizeType();


                    return new ClaimResuponse(ClaimDBResult.SUCCESS , Component.text("おめでとうございます！ " + prizeType + "に当選しました",NamedTextColor.GOLD ).decorate(TextDecoration.BOLD)  , rewardType , rewardValue ,null);



                }catch (Exception e) {
                    connection.rollback();

                    return new ClaimResuponse(ClaimDBResult.ERROR,Component.text(e.getMessage()) ,null , null , e);

                } finally {
                    connection.setAutoCommit(true);
                }


            } catch (SQLException e) {
                return new ClaimResuponse(ClaimDBResult.ERROR,Component.text(e.getMessage()),null , null , e);
            }


        });


    }







}
