package com.example.minicash.lottery.data;

import com.example.minicash.lottery.model.PrizeReward;
import com.example.minicash.lottery.model.RewardType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LotteryConfig {

    private final String lottoID;
    private final String displayName;

    // チケットに関する設定
    private final int ticketPrice;
    private final int bulkPurchaseAmount;
    private final int maxTicketsPerPlayer;

    // チケットの抽選範囲
    private final int maxGroup;
    private final int maxNumber;


    // 期間・演出に関する設定
    private final int claimExpiryDays;
    private final BossBar bossBar;


    // 賞金配分と当選モードの設定
    private final RewardType rewardType;
    private final double totalReturnRate;

    private final List<PrizeReward> prizeList = new ArrayList<>();





    public LotteryConfig(String lottoID , FileConfiguration config) {

        this.lottoID = lottoID;

        this.displayName = config.getString("display-name", lottoID);
        this.ticketPrice = config.getInt("ticket-price");
        this.bulkPurchaseAmount = config.getInt("bulk-purchase-amount");
        this.maxTicketsPerPlayer = config.getInt("max-tickets-per-player");

        this.maxGroup = config.getInt("max-group");
        this.maxNumber = config.getInt("max-number");

        this.claimExpiryDays = config.getInt("claim-expiry-days");
        this.bossBar = BossBar.bossBar(
                Component.text(config.getString("bossbar.title","a")),
                15.0f,
                BossBar.Color.valueOf(config.getString("bossbar.color","RED")),
                BossBar.Overlay.valueOf(config.getString("bossbar.style","NOTCHED_16"))
        );


        this.rewardType = RewardType.valueOf(config.getString("prize-settings.reward-type", "NORMAL"));
        this.totalReturnRate = config.getDouble("prize-settings.total-return-rate",0.70);


        List<Map<?, ?>> rawList = config.getMapList("prize-settings.distribution");

        for (Map<?, ?> map : rawList) {
            String displayName = (String) map.get("display-name");

            double poolShare = ((Number) map.get("pool-share")).doubleValue();
            String mode = (String) map.get("mode");

            String rewardType = (String) map.get("reward-type");

            PrizeReward reward = new PrizeReward(displayName, poolShare, mode, rewardType);
            prizeList.add(reward);
        }



    }


    public String getLottoID(){
        return  lottoID;
    }

    public String getDisplayName(){
        return displayName;
    }

    public int getTicketPrice(){
        return ticketPrice;
    }

    public int getBulkPurchaseAmount(){
        return bulkPurchaseAmount;
    }

    public int getMaxTicketsPerPlayer(){
        return maxTicketsPerPlayer;
    }

    public int getMaxGroup(){
        return maxGroup;
    }

    public int getMaxNumber(){
        return maxNumber;
    }

    public int getClaimExpiryDays(){
        return claimExpiryDays;
    }

    public BossBar getBossBar(){
        return bossBar;
    }

    public RewardType getRewardType(){
        return rewardType;
    }

    public double getTotalReturnRate(){
        return totalReturnRate;
    }

    public List<PrizeReward> getPrizeList(){
        return prizeList;
    }

}
