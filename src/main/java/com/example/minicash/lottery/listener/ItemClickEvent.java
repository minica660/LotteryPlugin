package com.example.minicash.lottery.listener;

import com.example.minicash.lottery.Lottery;
import com.example.minicash.lottery.data.LotteryConfig;
import com.example.minicash.lottery.manager.LotteryPurchaseManager;
import com.example.minicash.lottery.manager.LotteryTicketGenerator;
import com.example.minicash.lottery.manager.config.LotteryConfigManager;
import com.example.minicash.lottery.model.LottoType;
import com.example.minicash.lottery.util.LotteryKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ItemClickEvent implements Listener {

    private final LotteryConfigManager lotteryConfigManager;

    private final LotteryTicketGenerator lotteryTicketGenerator;

    public ItemClickEvent(LotteryConfigManager lotteryConfigManager , LotteryTicketGenerator lotteryTicketGenerator) {
        this.lotteryConfigManager = lotteryConfigManager;
        this.lotteryTicketGenerator = lotteryTicketGenerator;
    }


    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {


        if(event.getHand() != EquipmentSlot.HAND){
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR || event.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }

        ItemStack item = event.getItem();

        if(item == null){
            return;
        }

        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        if (!pdc.has(LotteryKeys.TICKET_TYPE, PersistentDataType.STRING)){
            return;
        }

        String ticketType = pdc.get(LotteryKeys.TICKET_TYPE, PersistentDataType.STRING);


        // パック状態のチケットだった場合
        if ("PACK".equals(ticketType)) {

            event.setCancelled(true);

            String lottoId = pdc.get(LotteryKeys.LOTTO_ID, PersistentDataType.STRING);
            String sessionId = pdc.get(LotteryKeys.PACK_SESSION_ID, PersistentDataType.STRING);
            // 連番かバラか
            LottoType lottoType = LottoType.valueOf(pdc.get(LotteryKeys.PACK_TYPE, PersistentDataType.STRING));
            int ticketAmount = pdc.get(LotteryKeys.TICKET_AMOUNT, PersistentDataType.INTEGER);

            Player player = event.getPlayer();

            LotteryConfig lotteryConfig = lotteryConfigManager.getLotteryConfig(lottoId);

            if(lotteryConfig == null){
                player.sendMessage(
                        Lottery.getMessage(
                                Component.text("そのアイテムの宝くじデータが不明です",NamedTextColor.RED)
                        )
                );

                return;
            }


            // 手のアイテムを1つ減らす
            item.setAmount(item.getAmount() - 1);

            List<ItemStack> lottoItems = lotteryTicketGenerator.generateTickets(lotteryConfig ,sessionId ,lottoType,ticketAmount);

            lottoItems.forEach(itemStack -> {

                if(LotteryPurchaseManager.hasEmptySlot(player)){
                    player.getInventory().addItem(itemStack);
                }

            });


            Sound sound = Sound.sound(Key.key("entity.item.pickup"), Sound.Source.PLAYER, 1f, 1f);

            player.playSound(sound);

            player.sendMessage(Lottery.getMessage(
                    Component.text("宝くじパックを開封しました！(" + ticketAmount + "枚)", NamedTextColor.LIGHT_PURPLE)
            ));



        }
    }


}
