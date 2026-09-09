package com.example.minicash.lottery.model;

public enum ClaimDBResult {
    SUCCESS,    // 賞金受け取り！
    PENDING,    // 抽選がまだ
    REDEEMED,   // 換金済み
    EXPIRED,    // 期限切れ
    LOSE,        // ハズレチケット
    ERROR
}
