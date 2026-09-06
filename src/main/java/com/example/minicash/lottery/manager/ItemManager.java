package com.example.minicash.lottery.manager;

import com.example.minicash.lottery.data.LotteryConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemManager {


    public ItemStack sealGet(LotteryConfig lotteryConfig, int amount){

        ItemStack sealItem = new ItemStack(Material.PAPER);

        sealItem.setAmount(amount);


        ItemMeta itemMeta = sealItem.getItemMeta();

        itemMeta.itemName(Component.text(lotteryConfig.getDisplayName() + "封"));
        itemMeta.lore(List.of(Component.text(lotteryConfig.getDisplayName() + "の宝くじが入った封(10枚)")));

        sealItem.setItemMeta(itemMeta);




    }



}
