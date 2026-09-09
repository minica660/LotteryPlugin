package com.example.minicash.lottery.data;

import com.example.minicash.lottery.model.RewardType;

import java.time.LocalDateTime;

public class LotteryResultData {


    // ここでのprizeTypeは何等かどうか
    private final String prizeType;
    private final int winGroup;
    private final int winNumber;
    private final RewardType rewardType;
    private final String rewardValue;
    private final LocalDateTime expiryTime;

    public LotteryResultData(String prizeType, int winGroup, int winNumber, RewardType rewardType, String rewardValue, LocalDateTime expiryTime) {
        this.prizeType = prizeType;
        this.winGroup = winGroup;
        this.winNumber = winNumber;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.expiryTime = expiryTime;
    }

    public String getPrizeType() {
        return prizeType;
    }

    public int getWinGroup() {
        return winGroup;
    }

    public int getWinNumber() {
        return winNumber;
    }

    public RewardType getRewardType(){
        return rewardType;
    }

    public String getRewardValue() {
        return rewardValue;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }


}
