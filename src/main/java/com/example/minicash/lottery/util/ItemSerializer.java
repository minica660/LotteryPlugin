package com.example.minicash.lottery.util;

import com.example.minicash.lottery.Lottery;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public class ItemSerializer {

    private static Lottery plugin;


    public ItemSerializer(Lottery plugin) {
        ItemSerializer.plugin = plugin;
    }

    /**
     * ItemStackをBase64 文字列に変換
     */
    public static String itemSerializer(ItemStack item) {
        if (item == null || item.getType().isAir()){
            return null;
        }

        byte[] bytes = item.serializeAsBytes();
        return Base64.getEncoder().encodeToString(bytes);

    }

    /**
     * Base64 文字列から ItemStackに変換
     */
    public static ItemStack deitemSerializer(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            // コンポーネント構造を崩さずに復元
            return ItemStack.deserializeBytes(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }




}
