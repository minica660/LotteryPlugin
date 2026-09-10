package com.example.minicash.lottery.manager.shop;

import com.example.minicash.lottery.model.ShopType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class VillagerShop {

    private final NamespacedKey shopTypeKey;


    public VillagerShop(JavaPlugin plugin){

        this.shopTypeKey = new NamespacedKey(plugin, "lottery_npc_type");

    }


    public void spawnVillager(Location location, ShopType shopType){

        Villager shopVillager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);


        shopVillager.customName(Component.text(shopType.getDisplayName() , NamedTextColor.GOLD));
        shopVillager.setCustomNameVisible(true);

        shopVillager.customName(Component.text(shopType.getDisplayName(), shopType.getColor()));
        shopVillager.setCustomNameVisible(true);

        shopVillager.setAI(false);
        shopVillager.setInvulnerable(true);
        shopVillager.setPersistent(true);
        shopVillager.setRemoveWhenFarAway(false);

        shopVillager.getPersistentDataContainer().set(shopTypeKey, PersistentDataType.STRING, shopType.getId());



    }


    public NamespacedKey getShopTypeKey() {
        return shopTypeKey;
    }



}
