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
import org.bukkit.Material;
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
import java.util.Map;

public class ItemClickEvent implements Listener {

    private final LotteryConfigManager lotteryConfigManager;


    public ItemClickEvent(LotteryConfigManager lotteryConfigManager ) {
        this.lotteryConfigManager = lotteryConfigManager;
    }


    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {


        if(event.getHand() != EquipmentSlot.HAND){
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }

        ItemStack item = event.getItem();

        if(item == null || !item.hasItemMeta()){
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

            if (!hasEnoughEmptySlots(player,ticketAmount)){
                player.sendMessage(Lottery.getMessage(
                        Component.text("インベントリに空きがありません！", NamedTextColor.RED)
                ));
                return;
            }


            List<ItemStack> lottoItems = LotteryTicketGenerator.generateTickets(lotteryConfig ,sessionId ,lottoType,ticketAmount);

            lottoItems.forEach(itemStack -> {

                Map<Integer, ItemStack> leftover = player.getInventory().addItem(itemStack);

                if (!leftover.isEmpty()) {

                    leftover.values().forEach(dropItem ->
                            player.getWorld().dropItemNaturally(player.getLocation(), dropItem)
                    );

                    player.sendMessage(Lottery.getMessage(
                            Component.text("インベントリに空きがないため宝くじチケットをドロップさせました！", NamedTextColor.DARK_PURPLE)
                    ));

                }


            });


            Sound sound = Sound.sound(Key.key("entity.player.levelup"), Sound.Source.MASTER, 200f, 1.6f);

            player.playSound(sound);

            player.sendMessage(Lottery.getMessage(
                    Component.text("宝くじパックを開封しました！(" + ticketAmount + "枚)", NamedTextColor.LIGHT_PURPLE)
            ));



        }
    }


    /**
     * プレイヤーのインベントリに指定した数以上の完全な空きスロットがあるかを確認
     *
     * @param player 対象のプレイヤー
     * @param requiredSlots 必要な空きスロット数
     * @return 空きスロット数が足りている場合は true
     */
    public static boolean hasEnoughEmptySlots(Player player, int requiredSlots) {

        if (requiredSlots <= 0) {
            return true;
        }

        int emptyCount = 0;


        for (ItemStack item : player.getInventory().getStorageContents()) {

            if (item == null || item.getType() == Material.AIR) {
                emptyCount++;
                if (emptyCount >= requiredSlots) {
                    return true;
                }
            }

        }

        return false;
    }


}
