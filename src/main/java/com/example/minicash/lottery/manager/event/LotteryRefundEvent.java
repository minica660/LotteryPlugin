package com.example.minicash.lottery.manager.event;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LotteryRefundEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    private final Player player;
    private final String sessionID;
    private final int refundPrice;
    private final String reason;

    private boolean refundSuccess = false;
    private Component errorMessage = null;

    public LotteryRefundEvent(Player player, String sessionID, int refundPrice, String reason) {
        this.player = player;
        this.sessionID = sessionID;
        this.refundPrice = refundPrice;
        this.reason = reason;
    }


    public Player getPlayer() {
        return player;
    }

    public int getRefundPrice() {
        return refundPrice;
    }

    public String getReason() {
        return reason;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setRefundSuccess(boolean refundSuccess) {
        this.refundSuccess = refundSuccess;
    }

    public boolean isRefundSuccess() {
        return refundSuccess;
    }

    public void setErrorMessage(Component errorMessage) {
        this.errorMessage = errorMessage;
    }

    public @Nullable Component getErrorMessage(){
        return errorMessage;
    }



    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}
