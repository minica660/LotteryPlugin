package com.example.minicash.lottery.listener;

import com.example.minicash.lottery.Lottery;
import com.example.minicash.lottery.manager.shop.VillagerShop;
import com.example.minicash.lottery.model.ShopType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopEvent implements Listener {

    private final JavaPlugin plugin;
    private final VillagerShop villagerShop;

    public ShopEvent(JavaPlugin plugin,VillagerShop villagerShop) {
        this.plugin = plugin;
        this.villagerShop = villagerShop;
    }

    @EventHandler
    public void onShopVillagerClick(PlayerInteractEntityEvent event) {

        Entity entity = event.getRightClicked();

        if (!(entity instanceof Villager villager)) {
            return;
        }

        Player player = event.getPlayer();

        PersistentDataContainer pdc = villager.getPersistentDataContainer();


        if (!pdc.has(villagerShop.getShopTypeKey(), PersistentDataType.STRING)) {
            return;
        }

        String typeId = pdc.get(villagerShop.getShopTypeKey(), PersistentDataType.STRING);
        ShopType shopType = ShopType.fromId(typeId);

        if(shopType == null){
            return;
        }


        event.setCancelled(true);

        player.sendMessage(
                Component.text(shopType.getDisplayName()).append(
                        Component.text(" いらっしゃいませ！", NamedTextColor.GOLD)
                )
        );

        Sound sound = Sound.sound(Key.key("entity.villager.yes"), Sound.Source.MASTER, 100f, 1.3f);

        player.playSound(sound);

        Bukkit.getScheduler().runTaskLater(plugin , () ->{


            player.sendMessage(
                    Component.text(shopType.getDisplayName()).append(
                            Component.text(shopType.getMessage(), NamedTextColor.LIGHT_PURPLE)
                    )
            );

            player.performCommand(shopType.getCommand());


        }, 20L);





    }


}
