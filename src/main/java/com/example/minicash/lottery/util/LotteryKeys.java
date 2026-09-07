package com.example.minicash.lottery.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class LotteryKeys {

    public static NamespacedKey LOTTO_ID;
    public static NamespacedKey TICKET_TYPE; // "SINGLE" or "PACK"
    public static NamespacedKey TICKET_UUID; // バラ用：固有ID
    public static NamespacedKey LOTTO_GROUP; // バラ用：組
    public static NamespacedKey LOTTO_NUMBER; // バラ用：番号
    public static NamespacedKey PACK_SESSION_ID; // パック用：セッションID
    public static NamespacedKey PACK_TYPE; // パック用："CONSECUTIVE" or "RANDOM"
    public static NamespacedKey TICKET_AMOUNT;  // パック用： 中のチケット枚数

    // プラグイン有効化時に呼び出す
    public LotteryKeys(JavaPlugin plugin) {
        LOTTO_ID = new NamespacedKey(plugin, "lotto_id");
        TICKET_TYPE = new NamespacedKey(plugin, "ticket_type");
        TICKET_UUID = new NamespacedKey(plugin, "ticket_uuid");
        LOTTO_GROUP = new NamespacedKey(plugin, "lotto_group");
        LOTTO_NUMBER = new NamespacedKey(plugin, "lotto_number");
        PACK_SESSION_ID = new NamespacedKey(plugin, "pack_session_id");
        PACK_TYPE = new NamespacedKey(plugin, "pack_type");
        TICKET_AMOUNT = new NamespacedKey(plugin, "ticket_amount");
    }


}
