package com.example.minicash.lottery.manager.gui;

import com.example.minicash.lottery.data.ActiveLotterySession;
import com.example.minicash.lottery.data.LotteryConfig;
import com.example.minicash.lottery.manager.LotteryManager;
import com.example.minicash.lottery.manager.config.LotteryConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class LotteryGUI {

    private final LotteryManager lotteryManager;
    private final LotteryConfigManager lotteryConfigManager;

    public LotteryGUI(LotteryManager lotteryManager, LotteryConfigManager lotteryConfigManager) {
        this.lotteryManager = lotteryManager;
        this.lotteryConfigManager = lotteryConfigManager;
    }


    public final Component TITLE = Component.text("宝くじ購入メニュー", NamedTextColor.DARK_GRAY);


    public void openLottoGUI(Player player) {

        LotteryConfig lotteryConfig = lotteryConfigManager.getLotteryConfig(lotteryManager.getActiveLotterySession().getLottoId());

        Inventory inventory = Bukkit.createInventory(new LotteryGUIHolder(), 9, TITLE);


        // 単品
        inventory.setItem(2, createGUIItem(Material.PAPER, Component.text("単品購入", NamedTextColor.GREEN).decorate(TextDecoration.BOLD), List.of(Component.text(" 枚数を指定して購入できます！ ", NamedTextColor.AQUA))));

        // バラ
        inventory.setItem(4, createGUIItem(Material.PAPER, Component.text("バラ購入( " + lotteryConfig.getBulkPurchaseAmount() + "枚 )", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD), List.of(Component.text("ランダムな番号のセット", NamedTextColor.AQUA))));

        // 連番
        inventory.setItem(6, createGUIItem(Material.BOOK, Component.text("連番購入( " + lotteryConfig.getBulkPurchaseAmount() + "枚 )", NamedTextColor.DARK_BLUE).decorate(TextDecoration.BOLD), List.of(Component.text(" 一連の連続した番号のセット ", NamedTextColor.AQUA))));

        player.openInventory(inventory);


    }


    private static ItemStack createGUIItem(Material material, Component name, List<Component> lore) {


        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.itemName(name);

        itemMeta.lore(lore);

        item.setItemMeta(itemMeta);

        return item;


    }


}
