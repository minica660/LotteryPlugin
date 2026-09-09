package com.example.minicash.lottery.manager;

import com.example.minicash.lottery.model.LottoType;
import com.example.minicash.lottery.util.LotteryKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class ItemManager {


    /**
     * 未開封のパックを作成
     *
     * @param lottoID    宝くじID
     * @param sessionID  開催回の識別ID
     * @param displayName  表示名
     * @param packType   "CONSECUTIVE"(連番) または "RANDOM"(バラ)
     * @param ticketAmount パック内の枚数
     */
    public static ItemStack createLotteryPack(String sessionID , String lottoID , String displayName, LottoType packType , int ticketAmount) {

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta == null){
            return item;
        }

        String typeName = packType.equals(LottoType.CONSECUTIVE) ? "連番" + ticketAmount +"枚" : "バラ" + ticketAmount +"枚";

        itemMeta.displayName(Component.text(displayName + " [" + typeName + "パック]", NamedTextColor.LIGHT_PURPLE));
        itemMeta.lore(List.of(
                Component.text("右クリックで開封します", NamedTextColor.GRAY),
                Component.text("種類: " + typeName, NamedTextColor.AQUA),
                Component.text(displayName + "の宝くじが入った封(" + ticketAmount +"枚入り)")
        ));

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        pdc.set(LotteryKeys.LOTTO_ID, PersistentDataType.STRING, lottoID);
        pdc.set(LotteryKeys.PACK_SESSION_ID, PersistentDataType.STRING, sessionID);
        pdc.set(LotteryKeys.TICKET_TYPE, PersistentDataType.STRING, "PACK");
        pdc.set(LotteryKeys.PACK_TYPE, PersistentDataType.STRING, packType.name());
        pdc.set(LotteryKeys.TICKET_AMOUNT, PersistentDataType.INTEGER, ticketAmount);

        item.setItemMeta(itemMeta);
        return item;

    }



    /**
     * バラ宝くじ（1枚）を作成
     *
     * @param lottoID     宝くじID
     * @param sessionID   開催回の識別ID
     * @param displayName 表示名
     * @param group       組番号
     * @param number      くじ番号
     */
    public static ItemStack createSingleTicket(String sessionID ,String lottoID , String displayName , int group, int number) {

        ItemStack item = new ItemStack(Material.PAPER);

        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta == null){
            return item;
        }

        itemMeta.itemName(Component.text(displayName + " チケット"));

        itemMeta.lore(List.of(
                Component.text("組: " + group + "組", NamedTextColor.YELLOW),
                Component.text("番号: " + String.format("%06d", number), NamedTextColor.WHITE)
        ));

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        pdc.set(LotteryKeys.LOTTO_ID, PersistentDataType.STRING, lottoID);
        pdc.set(LotteryKeys.PACK_SESSION_ID, PersistentDataType.STRING, sessionID);
        pdc.set(LotteryKeys.TICKET_TYPE, PersistentDataType.STRING, "SINGLE");

        pdc.set(LotteryKeys.TICKET_UUID, PersistentDataType.STRING, UUID.randomUUID().toString());

        // 組と番号
        pdc.set(LotteryKeys.LOTTO_GROUP, PersistentDataType.INTEGER, group);
        pdc.set(LotteryKeys.LOTTO_NUMBER, PersistentDataType.INTEGER, number);

        item.setItemMeta(itemMeta);

        return item;
    }


}
