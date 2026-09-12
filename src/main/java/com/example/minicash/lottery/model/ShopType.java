package com.example.minicash.lottery.model;

import net.kyori.adventure.text.format.NamedTextColor;

public enum ShopType {

    SHOP("shop", "宝くじ販売屋", NamedTextColor.GOLD, "lotto gui", " 好きな宝くじのタイプから選んでくれ！"),
    CLAIM("claim", "宝くじ換金所", NamedTextColor.GREEN, "lotto claim", " アイテム調べ中・・・"),
    INFO("info", "宝くじ案内所", NamedTextColor.AQUA, "lotto info", " 現在の宝くじ開催状況はこちらだよ！");

    private final String id;
    private final String displayName;
    private final NamedTextColor color;
    private final String command;
    private final String message;

    ShopType(String id, String displayName, NamedTextColor color, String command, String message) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.command = command;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public NamedTextColor getColor() {
        return color;
    }

    public String getCommand() {
        return command;
    }

    public String getMessage() {
        return message;
    }

    public static ShopType fromId(String id) {
        for (ShopType type : values()) {

            if (type.id.equalsIgnoreCase(id)) {

                return type;
            }

        }
        return null;
    }


}
