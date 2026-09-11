package com.example.minicash.lottery.manager.event;

import com.example.minicash.lottery.model.RewardType;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LotteryRewardEvent extends Event  {
    private static final HandlerList handlers = new HandlerList();


    private final Player player;
    private final RewardType rewardType;
    private final String rewardValue;

    private final String ticketUUID;
    private final String sessionID;

    private final int ticketGroup;
    private final int ticketNumber;
    private final ItemStack ticketItem;

    private boolean suppressDefaultReward = false;


//    private boolean cancelled = false;


    public LotteryRewardEvent(Player player, RewardType rewardType, String rewardValue , String ticketUUID , String sessionID , int ticketGroup , int ticketNumber , ItemStack ticketItem) {

        this.player = player;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;

        this.ticketUUID = ticketUUID;
        this.sessionID = sessionID;
        this.ticketGroup = ticketGroup;
        this.ticketNumber = ticketNumber;

        this.ticketItem = ticketItem;

    }

    public Player getPlayer() {
        return player.getPlayer();
    }


    public RewardType getRewardType() {
        return rewardType;
    }

    public String getRewardValue(){
        return  rewardValue;
    }

    public String getTicketUUID(){
        return ticketUUID;
    }

    public String getSessionID(){
        return sessionID;
    }

    public int getTicketGroup(){
        return ticketGroup;
    }

    public int getTicketNumber(){
        return ticketNumber;
    }

    public boolean isSuppressDefaultReward() {
        return suppressDefaultReward;
    }


    /**
     * true に設定すると、DB処理とチケット消去は行われますが、標準の報酬給付処理のみスキップされます
     */
    public void setSuppressDefaultReward(boolean suppress) {
        this.suppressDefaultReward = suppress;
    }


//
//    public boolean isCancelled() {
//        return cancelled;
//    }
//
//    public void setCancelled(boolean cancel) {
//        this.cancelled = cancel;
//    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
