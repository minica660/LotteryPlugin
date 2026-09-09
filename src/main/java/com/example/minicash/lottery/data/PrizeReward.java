package com.example.minicash.lottery.data;

import com.example.minicash.lottery.model.PrizeSettingType;
import com.example.minicash.lottery.model.RewardType;

public class PrizeReward {

    private String displayName;
    private double poolShare;
    private String mode;
    // MONEYかITEMかどうか
    private RewardType rewardType;
    private PrizeSettingType prizeSettingType;

    // 賞金として渡す金額又はItemStackのBase64化された文字列
    private String itemBase64;

    // MONEY用
    public PrizeReward(String displayName, double poolShare, String mode, RewardType rewardType, PrizeSettingType prizeSettingType) {
        this(displayName, poolShare, mode, rewardType, prizeSettingType, null);
    }

    // ITEM用
    public PrizeReward(String displayName, double poolShare, String mode, RewardType rewardType, PrizeSettingType prizeSettingType , String itemBase64) {
        this.displayName = displayName;
        this.poolShare = poolShare;
        this.mode = mode;
        this.rewardType = rewardType;
        this.prizeSettingType = prizeSettingType;
        this.itemBase64 = itemBase64;
    }

    // 何等かどうか(sample.ymlから取得したもののためカラーコードが含まれる)
    public String getDisplayName() {
        return displayName;
    }

    public double getPoolShare() {
        return poolShare;
    }

    public String getMode() {
        return mode;
    }

    public RewardType getRewardType() {
        return rewardType;
    }

    public PrizeSettingType getPrizeSettingType() {
        return prizeSettingType;
    }

    public String getItemBase64(){
        return itemBase64;
    }

}
