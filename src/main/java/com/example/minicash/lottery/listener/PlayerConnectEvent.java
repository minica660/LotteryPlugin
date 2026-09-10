package com.example.minicash.lottery.listener;

import com.example.minicash.lottery.manager.LotteryManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectEvent implements Listener {

    private LotteryManager lotteryManager;
    public PlayerConnectEvent(LotteryManager lotteryManager) {
        this.lotteryManager = lotteryManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){

        Player player = event.getPlayer();

        lotteryManager.sendLotteryInfo(player);

        lotteryManager.showBossBar(player);

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){

        Player player = event.getPlayer();

        lotteryManager.hideBossBar(player);

    }


}
