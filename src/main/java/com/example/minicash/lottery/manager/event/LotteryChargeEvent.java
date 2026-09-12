package com.example.minicash.lottery.manager.event;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 宝くじの購入の際に呼び出されるイベント
 */
public class LotteryChargeEvent extends Event {

    private final HandlerList handlerList = new HandlerList();

    private final Player player;
    private final int totalMoney;
    private final String sessionID;

    private boolean paymentSuccess = false;
    private Component errorMessage = null;

    public LotteryChargeEvent(Player player, int totalMoney, String sessionID) {
        this.player = player;
        this.totalMoney = totalMoney;
        this.sessionID = sessionID;
    }


    public Player getPlayer() {
        return player;
    }

    public int getTotalMoney() {
        return totalMoney;
    }

    public String getSessionID() {
        return sessionID;
    }


    public void setPaymentSuccess(boolean paymentSuccess) {
        this.paymentSuccess = paymentSuccess;
    }

    public boolean isPaymentSuccess() {
        return paymentSuccess;
    }

    public void setErrorMessage(@NotNull Component errorMessage) {
        this.errorMessage = errorMessage;
    }

    public @Nullable Component getErrorMessage() {
        return errorMessage;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return null;
    }
}
