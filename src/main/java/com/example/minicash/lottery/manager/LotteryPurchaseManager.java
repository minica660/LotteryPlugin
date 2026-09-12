package com.example.minicash.lottery.manager;

import com.example.minicash.lottery.Lottery;
import com.example.minicash.lottery.data.ActiveLotterySession;
import com.example.minicash.lottery.data.LotteryConfig;
import com.example.minicash.lottery.database.ActiveDatabase;
import com.example.minicash.lottery.database.PlayerDatabase;
import com.example.minicash.lottery.manager.event.LotteryChargeEvent;
import com.example.minicash.lottery.manager.event.LotteryRefundEvent;
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

        if(!hasEmptySlot(player)){
            player.sendMessage(Lottery.getMessage(
                    Component.text("インベントリに空きがありません！",NamedTextColor.RED)
            ));
            return;
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

                LotteryChargeEvent lotteryChargeEvent = new LotteryChargeEvent(player,totalPrice,sessionID);

                if(this.economy != null) {

                    if (economy.has(player,totalPrice)) {

                        EconomyResponse economyResponse = economy.withdrawPlayer(player,totalPrice);

                        if(economyResponse.transactionSuccess()){
                            lotteryChargeEvent.setPaymentSuccess(true);
                        }else{
                            lotteryChargeEvent.setErrorMessage(Lottery.getMessage(
                                    Component.text("決済に失敗しました: " + economyResponse.errorMessage, NamedTextColor.RED)));
                        }

                    }else {
                        lotteryChargeEvent.setErrorMessage(Lottery.getMessage(
                                Component.text("所持金が足りません（必要額: " + totalPrice + "円）", NamedTextColor.RED)));
                    }

                }

                Bukkit.getPluginManager().callEvent(lotteryChargeEvent);


                if(!lotteryChargeEvent.isPaymentSuccess()){

                    Component errorMessage = lotteryChargeEvent.getErrorMessage() != null ?
                            lotteryChargeEvent.getErrorMessage() : Lottery.getMessage(Component.text("決済処理が行われなかったため購入できません",NamedTextColor.RED));

                    player.sendMessage(errorMessage);

                    return;
                }






                playerDatabase.addTicket(player.getUniqueId(), sessionID, amount, lotteryConfig.getMaxTicketsPerPlayer()).thenAccept(result ->{

                    Bukkit.getScheduler().runTask(plugin ,() ->{

                        if(!result.isSuccess()){

                           executeRefund(player,totalPrice,"DATABASE_SAVE_ERROR",sessionID);

                            player.sendMessage(Lottery.getMessage(
                                    Component.text("購入処理中にエラーが発生したため処理を停止しました",NamedTextColor.RED)
                            ));

                            plugin.getLogger().severe(result.getMessage());

                            return;
                        }



                        activeDatabase.addTotalMoney(sessionID, totalPrice).thenAccept(response -> {

                            if (!response.isSuccess()) {


                                plugin.getLogger().warning("売上金の加算に失敗しました [セッション: " + sessionID + "]: " + response.getMessage());

                            }

                        }).exceptionally(ex -> {



                            plugin.getLogger().log(Level.SEVERE, "LotteryPurchaseManager.addTotalMoney実行中に予期せぬエラーが発生しました", ex);

                            return null;

                        });




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

                    Bukkit.getScheduler().runTask(plugin,() -> {
                        executeRefund(player,totalPrice,"ASYNC_EXCEPTION",sessionID);
                    });

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


    /**
     * 購入処理中だけじゃなくてもどこからでも呼び出せます
     * 特殊な返金処理をやりたいとき用
     */
    public boolean executeRefund(Player player, int amount , String reason , String sessionID){

        LotteryRefundEvent refundEvent = new LotteryRefundEvent(player,sessionID , amount , reason);

        if(this.economy != null){

            EconomyResponse economyResponse = economy.depositPlayer(player, amount);

            if(economyResponse.transactionSuccess()) {
                refundEvent.setRefundSuccess(true);
            }else {
                refundEvent.setErrorMessage(Lottery.getMessage(Component.text("Vaultでの返金に失敗しました" + economyResponse.errorMessage)));
            }


        }

        Bukkit.getPluginManager().callEvent(refundEvent);


        if(!refundEvent.isRefundSuccess()){
            plugin.getLogger().severe(player.getName() + "への返金処理に失敗しました：" + amount + "円" + "理由：" +
                    (refundEvent.getErrorMessage() != null ? refundEvent.getErrorMessage() : "未定義のエラー"));
            return false;
        }


        return true;



    }




}
