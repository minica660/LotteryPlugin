package com.example.minicash.lottery.manager;

import com.example.minicash.lottery.data.LotteryConfig;
import com.example.minicash.lottery.model.LottoType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LotteryTicketGenerator {

    private static final Random random = new Random();

    /**
     * LottoTypeに応じてチケットを生成する
     */
    public static List<ItemStack> generateTickets(LotteryConfig lotteryConfig, String sessionID, LottoType type, int packAmount) {
        List<ItemStack> tickets = new ArrayList<>();


        int maxGroup = lotteryConfig.getMaxGroup();
        int maxNumber = lotteryConfig.getMaxNumber();

        switch (type) {
            case SINGLE:
                // 単品1枚
                int singleGroup = random.nextInt(maxGroup) + 1;
                int singleNumber = random.nextInt(maxNumber + 1);
                tickets.add(ItemManager.createSingleTicket(
                        sessionID, lotteryConfig.getLottoID(), lotteryConfig.getDisplayName(), singleGroup, singleNumber
                ));
                break;

            case CONSECUTIVE:
                // 連番
                // 組は一つに固定し、番号はランダムだが続ける
                int consecutiveGroup = random.nextInt(maxGroup) + 1;

                int maxStartNumber = Math.max(0, maxNumber - packAmount + 1);
                int startNumber = random.nextInt(maxStartNumber + 1);

                for (int i = 0; i < packAmount; i++) {
                    int num = startNumber + i;
                    tickets.add(ItemManager.createSingleTicket(
                            sessionID, lotteryConfig.getLottoID(), lotteryConfig.getDisplayName(), consecutiveGroup, num
                    ));
                }
                break;

            case RANDOM:
                // 完全バラ（1枚ごとに組と番号をランダム決定）
                for (int i = 0; i < packAmount; i++) {
                    int randGroup = random.nextInt(maxGroup) + 1;
                    int randNumber = random.nextInt(maxNumber + 1);
                    tickets.add(ItemManager.createSingleTicket(
                            sessionID, lotteryConfig.getLottoID(), lotteryConfig.getDisplayName(), randGroup, randNumber
                    ));
                }
                break;
        }

        return tickets;
    }


}
