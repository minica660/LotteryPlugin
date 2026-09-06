package com.example.minicash.lottery.model;

public class PrizeReward {

    private String displayName;
    private double poolShare;
    private String mode;
    private String rewardType;

    public PrizeReward(String displayName, double poolShare, String mode, String rewardType) {
        this.displayName = displayName;
        this.poolShare = poolShare;
        this.mode = mode;
        this.rewardType = rewardType;
    }

    public String getDisplayName() { return displayName; }
    public double getPoolShare() { return poolShare; }
    public String getMode() { return mode; }
    public String getRewardType() { return rewardType; }


}
