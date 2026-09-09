package com.example.minicash.lottery.manager;

import com.example.minicash.lottery.Lottery;
import com.example.minicash.lottery.database.ClaimeTicketDatabase;
import com.example.minicash.lottery.model.ClaimDBResult;
import com.example.minicash.lottery.model.RewardType;
import com.example.minicash.lottery.util.ItemSerializer;
import com.example.minicash.lottery.util.LotteryKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.logging.Level;

public class LotteryClaimManager {

    private final JavaPlugin plugin;
    private final Economy economy;

    private final ClaimeTicketDatabase claimeTicketDatabase;

    public LotteryClaimManager(JavaPlugin plugin, Economy economy,ClaimeTicketDatabase claimeTicketDatabase) {

        this.plugin = plugin;
        this.economy = economy;
        this.claimeTicketDatabase = claimeTicketDatabase;

    }

    public void claimTicket(Player player , ItemStack item){

        if(item == null || !item.hasItemMeta()){

            player.sendMessage(Lottery.getMessage(
                    Component.text("宝くじ用アイテムではありません！", NamedTextColor.RED)
            ));

            return;

        }



        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();

        String ticketType = pdc.get(LotteryKeys.TICKET_TYPE, PersistentDataType.STRING);
        if (!"SINGLE".equals(ticketType)) {

            player.sendMessage(Component.text("これは換金可能な宝くじアイテムではありません", NamedTextColor.RED));

            return;

        }



        String ticketUUID = pdc.get(LotteryKeys.TICKET_UUID, PersistentDataType.STRING);
        String sessionID = pdc.get(LotteryKeys.PACK_SESSION_ID, PersistentDataType.STRING);
        Integer group = pdc.get(LotteryKeys.LOTTO_GROUP, PersistentDataType.INTEGER);
        Integer number = pdc.get(LotteryKeys.LOTTO_NUMBER, PersistentDataType.INTEGER);

        if (ticketUUID == null || sessionID == null || group == null || number == null) {
            player.sendMessage(Component.text("不明な宝くじチケットです", NamedTextColor.RED));
            return;
        }

        claimeTicketDatabase.claimTicket(player,ticketUUID,sessionID,group,number,item).thenAccept(result -> {

            Bukkit.getScheduler().runTask(plugin , () ->{

                ClaimDBResult claimResult = result.getClaimResult();

                switch (claimResult){

                    case SUCCESS -> {

                        item.setAmount(item.getAmount() -1);

                        RewardType type = result.getRewardType();
                        String rawValue = result.getRewardValue();

                        if(type == RewardType.MONEY){

                            double amount = Double.parseDouble(rawValue);

                            economy.depositPlayer(player , amount);

                            player.sendMessage(Lottery.getMessage(result.getcMessage()));

                        }else if (type == RewardType.ITEM) {

                            ItemStack rewardItem = ItemSerializer.deitemSerializer(rawValue);


                            if (rewardItem != null) {

                                Map<Integer, ItemStack> leftover = player.getInventory().addItem(rewardItem);

                                if (!leftover.isEmpty()) {
                                    leftover.values().forEach(dropItem ->
                                            player.getWorld().dropItemNaturally(player.getLocation(), dropItem)
                                    );

                                    player.sendMessage(Lottery.getMessage(
                                            Component.text("インベントリに空きがないため賞品をドロップさせました！", NamedTextColor.DARK_PURPLE)
                                    ));

                                }

                                player.sendMessage(Lottery.getMessage(
                                        Component.text("賞品を付与しました！", NamedTextColor.GREEN)
                                ));

                            }


                        }


                    }
                    case PENDING , EXPIRED , LOSE ->{

                        player.sendMessage(Lottery.getMessage(result.getcMessage()));

                    }
                    case REDEEMED -> {

                        plugin.getLogger().severe("既に換金されている宝くじチケットをプレイヤーが所持しています(" + player.getUniqueId() +") ・チケットID:" + ticketUUID + " セッションID: " + sessionID);

                        player.sendMessage(Lottery.getMessage(result.getcMessage()));

                    }
                    case ERROR ->  {

                        plugin.getLogger().log(Level.SEVERE,"claimTicketのDB処理中にエラーが発生しました " , result.getException());

                        player.sendMessage(Lottery.getMessage(result.getcMessage()));

                    }


                }


            });


        }).exceptionally(ex ->{

            player.sendMessage(Lottery.getMessage(Component.text(ex.getMessage(),NamedTextColor.RED)));

            plugin.getLogger().log(Level.SEVERE , "claimTicketDatabase.claimTicket()処理中に予期せぬエラーが発生しました " ,ex);

            return null;
        });




    }


}
