package com.example.minicash.lottery.data;

import java.time.LocalDateTime;

public class ActiveLotterySession {

    private final String sessionId;
    private final String lottoId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public ActiveLotterySession(String sessionId, String lottoId, LocalDateTime startTime, LocalDateTime endTime) {
        this.sessionId = sessionId;
        this.lottoId = lottoId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getSessionId() { return sessionId; }
    public String getLottoId() { return lottoId; }
    public LocalDateTime getEndTime() { return endTime; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endTime);
    }

}
