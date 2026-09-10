package com.example.minicash.lottery.commands;

import com.example.minicash.lottery.Lottery;
import com.example.minicash.lottery.data.ActiveLotterySession;
import com.example.minicash.lottery.data.LotteryConfig;
import com.example.minicash.lottery.manager.LotteryClaimManager;
import com.example.minicash.lottery.manager.LotteryManager;
import com.example.minicash.lottery.manager.LotteryPurchaseManager;
import com.example.minicash.lottery.manager.config.LotteryConfigManager;
import com.example.minicash.lottery.manager.gui.LotteryGUI;
import com.example.minicash.lottery.manager.shop.VillagerShop;
import com.example.minicash.lottery.model.LottoType;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public class LottoCommand implements BasicCommand {

    private final LotteryPurchaseManager lotteryPurchaseManager;
    private final LotteryClaimManager lotteryClaimManager;
    private final LotteryGUI lotteryGUI;
    private final LotteryManager lotteryManager;
    private final LotteryConfigManager lotteryConfigManager;

    public LottoCommand(LotteryPurchaseManager lotteryPurchaseManager, LotteryGUI lotteryGUI, LotteryClaimManager lotteryClaimManager, LotteryManager lotteryManager, LotteryConfigManager lotteryConfigManager) {
        this.lotteryPurchaseManager = lotteryPurchaseManager;
        this.lotteryGUI = lotteryGUI;
        this.lotteryClaimManager = lotteryClaimManager;
        this.lotteryManager = lotteryManager;
        this.lotteryConfigManager = lotteryConfigManager;

    }


    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {

        if (commandSourceStack.getExecutor() instanceof Player player) {

            if (!player.hasPermission("lottery.commands.lotto")) {
                player.sendMessage(Lottery.getMessage(
                        Component.text("あなたはこのコマンドを実行する権限がありません", NamedTextColor.RED)
                ));
                return;
            }

            if (args.length >= 1) {


                switch (args[0]) {

                    case "gui" -> {

                        if (!player.hasPermission("lottery.commands.lotto.gui")) {
                            player.sendMessage(Lottery.getMessage(
                                    Component.text("あなたは現在宝くじを購入することが出来ません！", NamedTextColor.RED)
                            ));
                            return;
                        }

                        lotteryGUI.openLottoGUI(player);

                    }
                    case "claim" -> {

                        if (!player.hasPermission("lottery.commands.lotto.claim")) {
                            player.sendMessage(Lottery.getMessage(
                                    Component.text("あなたは現在宝くじを交換することができません！", NamedTextColor.RED)
                            ));
                            return;
                        }

                        ItemStack item = player.getInventory().getItemInMainHand();

                        if (item.getType() != Material.AIR) {

                            lotteryClaimManager.claimTicket(player, item);

                        } else {
                            player.sendMessage(Lottery.getMessage(
                                    Component.text("換金アイテムを何か持ってください!", NamedTextColor.RED)
                            ));
                        }

                        return;

                    }
                    case "buy" -> {

                        if (args.length == 2) {

                            String ticketType = args[1];

                            LottoType lottoType = LottoType.valueOf(ticketType);

                            if(lottoType == null){
                                return;
                            }

                            LotteryConfig lotteryConfig = lotteryConfigManager.getLotteryConfig(lotteryManager.getActiveLotterySession().getLottoId());

                            int amount = 0;

                            switch (lottoType){
                                case RANDOM , CONSECUTIVE ->  amount = lotteryConfig.getBulkPurchaseAmount();
                                case SINGLE -> amount = 1;
                            }

                            if(amount == 0){
                                return;
                            }


                            lotteryPurchaseManager.buyLotto(
                                    lotteryManager.getActiveLotterySession().getSessionId(),
                                    lotteryConfig,
                                    player,
                                    amount,
                                    lottoType
                                    );

                        }

                        return;


                    }
                    case "info" -> {

                        if (!player.hasPermission("lottery.commands.lotto.info")) {
                            player.sendMessage(Lottery.getMessage(
                                    Component.text("あなたは現在宝くじの情報を取得することができません", NamedTextColor.RED)
                            ));
                            return;
                        }


                        if (!lotteryManager.isSessionActive()) {


                        }


                        ActiveLotterySession activeLottery = lotteryManager.getActiveLotterySession();
                        LotteryConfig lotteryConfig = lotteryConfigManager.getLotteryConfig(lotteryManager.getActiveLotterySession().getLottoId());


                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
                        String endTimeString = activeLottery.getEndTime().format(formatter);

                        // 残り時間の計算
                        Duration remaining = Duration.between(LocalDateTime.now(), activeLottery.getEndTime());
                        long hours = remaining.toHours();
                        long minutes = remaining.toMinutesPart();

                        String timeString = (hours > 0)
                                ? String.format("%d時間 %d分", hours, minutes)
                                : String.format("%d分", minutes);


                        player.sendMessage(Lottery.getMessage(
                                Component.text("========== 宝くじ開催情報！ ==========", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
                        ));


                        player.sendMessage(Lottery.getMessage(
                                Component.text("")
                        ));

                        player.sendMessage(Lottery.getMessage(
                                Component.text("現在の開催情報：" + lotteryConfig.getDisplayName())
                        ));

                        player.sendMessage(Lottery.getMessage(
                                Component.text("購入価格(単価)：" + lotteryConfig.getTicketPrice() + "円")
                        ));

                        player.sendMessage(Lottery.getMessage(
                                Component.text("残り時間：" + endTimeString + "( " + timeString + " )")
                        ));

                        player.sendMessage(Lottery.getMessage(
                                Component.text("残り時間：", NamedTextColor.GRAY)
                                        .append(Component.text("宝くじ屋村人を右クリック！", NamedTextColor.WHITE).decorate(TextDecoration.BOLD))
                        ));

                        player.sendMessage(Lottery.getMessage(
                                Component.text("")
                        ));


                        player.sendMessage(Lottery.getMessage(
                                Component.text("==============================", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
                        ));


                    }


                }


            } else {
                player.sendMessage(Lottery.getMessage(
                        Component.text("コマンドの入力方法を確認してください", NamedTextColor.RED)
                ));
            }


        } else {

            commandSourceStack.getSender().sendMessage(
                    Lottery.getMessage(Component.text("このコマンドはプレイヤーのみ実行可能です", NamedTextColor.RED))
            );

        }


    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        return BasicCommand.super.suggest(commandSourceStack, args);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return BasicCommand.super.canUse(sender);
    }

    @Override
    public @Nullable String permission() {
        return "lottery.commands.lotto";
    }
}
