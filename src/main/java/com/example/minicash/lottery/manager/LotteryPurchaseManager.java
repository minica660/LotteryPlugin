package com.example.minicash.lottery.manager;

import com.example.minicash.library.common.response.NormalResponse;
import com.example.minicash.lottery.Lottery;
import com.example.minicash.lottery.data.LotteryConfig;
import com.example.minicash.lottery.database.ActiveDatabase;
import com.example.minicash.lottery.database.PlayerDatabase;
import com.example.minicash.lottery.model.BuyType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class LotteryPurchaseManager {

    private final JavaPlugin plugin;
    private final Economy economy;

    private final ActiveDatabase activeDatabase;
    private final PlayerDatabase playerDatabase;


    public LotteryPurchaseManager(Economy economy) {
        this.economy = economy;
    }


    /**
     * 宝くじ購入のメインメソッド
     * @param amount 個数
     * @return
     */
    public NormalResponse buyLot(LotteryConfig lotteryConfig, Player player, int amount, BuyType buyType){

        playerDatabase.getTicketCount(player.getUniqueId(),lotteryConfig.getLottoID()).thenAccept(count -> {

            Bukkit.getScheduler().runTask(plugin,() ->{

                if(count >= lotteryConfig.getMaxTicketsPerPlayer()){
                    Bukkit.getPlayer(player.getUniqueId()).forEachAudience(target -> target.sendMessage(Lottery.getMessage(
                            Component.text("あなたは" + lotteryConfig.getDisplayName() + "の最大購入枚数を到達しています", NamedTextColor.RED)
                    )));

                    return;
                }



            });

        });

        int money = lotteryConfig.getTicketPrice() * amount;

        activeDatabase.addTotalMoney(lotteryConfig.getLottoID(),amount).thenAccept(result -> {

            Bukkit.getScheduler().runTask(plugin,() ->{

                if(result.isSuccess()){

                    EconomyResponse economyResponse = economy.withdrawPlayer(player,money);




                }


            });


        });

        playerDatabase.addTicket(player.getUniqueId(),lotteryConfig.getLottoID()  , amount ,lotteryConfig.getMaxTicketsPerPlayer()).thenAccept(result -> {



        })




    }










}
