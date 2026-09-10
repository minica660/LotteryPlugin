package com.example.minicash.lottery.listener;

import com.example.minicash.lottery.manager.gui.LotteryGUIHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class LotteryGUIEvent implements Listener {

    @EventHandler
    public void inventoryClick(InventoryClickEvent event){

        if(event.getInventory().getHolder() instanceof LotteryGUIHolder){

            event.setCancelled(true);


            if(!(event.getWhoClicked() instanceof Player player)){
                return;
            }

            if(event.getCurrentItem() == null){
                return;
            }

            int slot = event.getSlot();

            switch (slot) {
                case 2 -> player.performCommand("lotto buy SINGLE");
                case 4 -> player.performCommand("lotto buy RANDOM");
                case 6 -> player.performCommand("lotto buy CONSECUTIVE");
            }


            player.closeInventory();


        }


    }


}
