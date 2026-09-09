package com.example.minicash.lottery.response;

import com.example.minicash.library.common.response.NormalResponse;
import com.example.minicash.lottery.model.ClaimDBResult;
import com.example.minicash.lottery.model.RewardType;
import net.kyori.adventure.text.Component;

public class ClaimResuponse  {

    private final ClaimDBResult claimResult;
    private final Component cMessage;
    private final RewardType rewardType;
    private final String rewardValue;
    private final Exception exception;

    public ClaimResuponse(ClaimDBResult claimResult, Component cMessage, RewardType rewardType , String rewardValue , Exception exception) {
        this.claimResult = claimResult;
        this.cMessage = cMessage;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.exception = exception;

    }


    public ClaimDBResult getClaimResult() {
        return claimResult;
    }

    public Component getcMessage() {
        return cMessage;
    }

    public RewardType getRewardType() {
        return rewardType;
    }

    public String getRewardValue() {
        return rewardValue;
    }

    public Exception getException() {
        return exception;
    }

}
