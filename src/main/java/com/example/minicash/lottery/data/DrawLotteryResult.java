package com.example.minicash.lottery.data;

import com.example.minicash.lottery.model.RewardType;

public record DrawLotteryResult(

        String prizeName,   // 何等か
        int winGroup,   // 何組か
        int winNumber,  // 番号は何か
        RewardType rewardType,  // MONEYかITEM
        String rewardValue  //報酬内容（お金かアイテム）

) {
}
