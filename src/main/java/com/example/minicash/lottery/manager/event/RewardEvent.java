package com.example.minicash.lottery.manager.event;

import com.example.minicash.lottery.model.RewardType;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RewardEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();


    private final OfflinePlayer player;
    private final RewardType rewardType;

    private boolean cancelled = false;


    public RewardEvent(OfflinePlayer player, RewardType rewardType) {
        this.player = player;
        this.rewardType = rewardType;

    }

    public Player getPlayer() {
        return player.getPlayer();
    }

    public OfflinePlayer getOfflinePlayer(){
        return player;
    }

    public RewardType getRewardType() {
        return rewardType;
    }




    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
