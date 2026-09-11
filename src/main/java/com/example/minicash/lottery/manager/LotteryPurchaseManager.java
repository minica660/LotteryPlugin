package com.example.minicash.lottery.manager;

import com.example.minicash.lottery.Lottery;
import com.example.minicash.lottery.data.ActiveLotterySession;
import com.example.minicash.lottery.data.LotteryConfig;
import com.example.minicash.lottery.database.ActiveDatabase;
import com.example.minicash.lottery.database.PlayerDatabase;
import com.example.minicash.lottery.model.LottoType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class LotteryPurchaseManager {

    private final JavaPlugin plugin;
    private final Economy economy;
    private final LotteryManager lotteryManager;

    private final ActiveDatabase activeDatabase;
    private final PlayerDatabase playerDatabase;


    public LotteryPurchaseManager(Economy economy,JavaPlugin plugin , ActiveDatabase activeDatabase , PlayerDatabase playerDatabase  , LotteryManager lotteryManager) {
        this.economy = economy;
        this.plugin = plugin;
        this.activeDatabase = activeDatabase;
        this.playerDatabase = playerDatabase;
        this.lotteryManager = lotteryManager;
    }


    /**
     * 宝くじ購入のメインメソッド
     * @param amount 個数
     */
    public void buyLotto(String sessionID , LotteryConfig lotteryConfig, Player player, int amount, LottoType lottoType){

        ActiveLotterySession currentSession = lotteryManager.getActiveLotterySession();

        if (currentSession == null || currentSession.isExpired()) {
            player.sendMessage(Lottery.getMessage(
                    Component.text("現在開催中の宝くじはありません！", NamedTextColor.RED)
            ));
            return;
        }

        if (!currentSession.getLottoId().equals(lotteryConfig.getLottoID())) {
            player.sendMessage(Lottery.getMessage(
                    Component.text("指定された宝くじは現在開催されていません！", NamedTextColor.RED)
            ));
            return;
        }


        if (lottoType == LottoType.RANDOM || lottoType == LottoType.CONSECUTIVE) {
            if (amount % lotteryConfig.getBulkPurchaseAmount() != 0) {
                player.sendMessage(Lottery.getMessage(
                        Component.text("指定した宝くじは" + lotteryConfig.getBulkPurchaseAmount() + "枚ごとのセットですが個数が足りないため購入できません！", NamedTextColor.RED)
                ));
                return;
            }
        }



        playerDatabase.getTicketCount(player.getUniqueId(), sessionID).thenAccept(count -> {

            Bukkit.getScheduler().runTask(plugin,() ->{


                if(count + amount > lotteryConfig.getMaxTicketsPerPlayer()){

                    player.sendMessage(Lottery.getMessage(
                            Component.text("あなたは" + lotteryConfig.getDisplayName() + "の最大購入枚数を到達しているため購入することができません", NamedTextColor.RED)
                    ));

                    return;
                }



                int totalPrice = lotteryConfig.getTicketPrice() * amount;

                if (!economy.has(player, totalPrice)) {

                    player.sendMessage(Lottery.getMessage(
                            Component.text("所持金が足りません（必要額: " + totalPrice + "円）", NamedTextColor.RED)
                    ));

                    return;

                }

                if (!hasEmptySlot(player)) {
                    player.sendMessage(Lottery.getMessage(
                            Component.text("インベントリに空きがありません！", NamedTextColor.RED)
                    ));
                    return;
                }

                EconomyResponse economyResponse = economy.withdrawPlayer(player, totalPrice);
                if (!economyResponse.transactionSuccess()) {
                    player.sendMessage(Lottery.getMessage(
                            Component.text("決済に失敗しました: " + economyResponse.errorMessage, NamedTextColor.RED)
                    ));
                    return;
                }

                activeDatabase.addTotalMoney(sessionID, totalPrice);
                playerDatabase.addTicket(player.getUniqueId(), sessionID, amount, lotteryConfig.getMaxTicketsPerPlayer()).thenAccept(result ->{

                    Bukkit.getScheduler().runTask(plugin ,() ->{

                        if(!result.isSuccess()){

                            economy.depositPlayer(player,totalPrice);

                            player.sendMessage(Lottery.getMessage(
                                    Component.text("購入処理中にエラーが発生したため処理を停止しました",NamedTextColor.RED)
                            ));

                            plugin.getLogger().severe(result.getMessage());

                            return;
                        }

                        if (lottoType == LottoType.RANDOM || lottoType == LottoType.CONSECUTIVE) {

                            ItemStack packItem = ItemManager.createLotteryPack(sessionID, lotteryConfig.getLottoID(), lotteryConfig.getDisplayName(), lottoType, amount);

                            Map<Integer, ItemStack> leftover = player.getInventory().addItem(packItem);

                            if (!leftover.isEmpty()) {

                                for (ItemStack item : leftover.values()) {

                                    Item droppedItem = player.getWorld().dropItemNaturally(player.getLocation(), item);

                                    droppedItem.setOwner(player.getUniqueId());

                                }

                                player.sendMessage(Lottery.getMessage(
                                        Component.text("インベントリに空きがないためドロップしました", NamedTextColor.YELLOW)
                                ));
                            }

                            player.sendMessage(Lottery.getMessage(
                                    Component.text(lotteryConfig.getDisplayName() + "の宝くじパックを付与しました", NamedTextColor.GOLD)
                            ));
                        } else {

                            List<ItemStack> lottoItems = LotteryTicketGenerator.generateTickets(lotteryConfig, sessionID, lottoType, amount);

                            ItemStack[] itemsArray = lottoItems.toArray(new ItemStack[0]);

                            Map<Integer, ItemStack> leftover = player.getInventory().addItem(itemsArray);

                            if (!leftover.isEmpty()) {
                                for (ItemStack item : leftover.values()) {

                                    Item droppedItem = player.getWorld().dropItemNaturally(player.getLocation(), item);

                                    droppedItem.setOwner(player.getUniqueId());

                                }

                                player.sendMessage(Lottery.getMessage(
                                        Component.text("インベントリに空きがないためドロップしました", NamedTextColor.YELLOW)
                                ));

                            }


                            player.sendMessage(Lottery.getMessage(
                                    Component.text(lotteryConfig.getDisplayName() + "の宝くじを" + amount + "枚付与しました", NamedTextColor.GOLD)
                            ));


                        }


                        player.sendMessage(Lottery.getMessage(
                                Component.text("宝くじのご購入ありがとうございました",NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        ));


                    });


                }).exceptionally(ex ->{

                    player.sendMessage(Lottery.getMessage(
                            Component.text(ex.getMessage(), NamedTextColor.RED)
                    ));

                    plugin.getLogger().log(Level.SEVERE,"buyLotメソッド内でエラーが発生しました " , ex);


                    return null;

                });


            });

        });








    }





    public static boolean hasEmptySlot(Player player) {
        return player.getInventory().firstEmpty() != -1;
    }





}
