package com.example.minicash.lottery.response;

import com.example.minicash.library.common.response.NormalResponse;
import com.example.minicash.lottery.data.DrawLotteryResult;

import java.util.List;

public class DrawLottoResultResponse extends NormalResponse {

    private final List<DrawLotteryResult> drawLotteryResults;

    public DrawLottoResultResponse(boolean success, String message,List<DrawLotteryResult> drawLotteryResults) {
        super(success, message);
        this.drawLotteryResults = drawLotteryResults;
    }

    public List<DrawLotteryResult> getDrawLotteryResults() {
        return drawLotteryResults;
    }
}
