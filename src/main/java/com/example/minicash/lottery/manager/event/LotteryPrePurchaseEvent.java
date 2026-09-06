package com.example.minicash.lottery.manager.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LotteryPrePurchaseEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();


    private final Player player;
    private final int ticketCount;

    private boolean cancelled = false;


    public LotteryPrePurchaseEvent(Player player , int ticketCount) {
        this.player = player;
        this.ticketCount = ticketCount;

    }

    public Player getPlayer() {
        return player;
    }

    public int getTicketCount() {
        return ticketCount;
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
