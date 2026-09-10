package com.example.minicash.lottery.commands;

import com.example.minicash.lottery.commands.handler.LotteryAdminCommandHandler;
import com.example.minicash.lottery.manager.config.LotteryConfigManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;

public class LotteryAdminCommand {

    private final LotteryAdminCommandHandler lotteryAdminCommandHandler;
    private final LotteryConfigManager lotteryConfigManager;

    public LotteryAdminCommand(LotteryAdminCommandHandler lotteryAdminCommandHandler, LotteryConfigManager lotteryConfigManager) {
        this.lotteryAdminCommandHandler = lotteryAdminCommandHandler;
        this.lotteryConfigManager = lotteryConfigManager;
    }


    public LiteralCommandNode<CommandSourceStack> createAdminCommand(){

        return Commands.literal("lotteryadmin")

                .requires(source -> source.getSender().hasPermission("lottery.commands.admin"))

                .then(Commands.literal("start")

                        .then(Commands.argument("lottoID", StringArgumentType.word())

                                .suggests((ctx, builder) -> {

                                    for (String lottoId : lotteryConfigManager.getLotteryConfigs().keySet()) {
                                        builder.suggest(lottoId);
                                    }

                                    return builder.buildFuture();
                                })
                                .executes(lotteryAdminCommandHandler::handleStart)

                        )

                )
                .then(Commands.literal("stop")
                        .executes(lotteryAdminCommandHandler::handleStop)
                )
                .then(Commands.literal("info")
                        .executes(lotteryAdminCommandHandler::handleInfo)
                )
                .then(Commands.literal("player")

                        .then(Commands.argument("target", ArgumentTypes.playerProfiles())
                                .executes(lotteryAdminCommandHandler::handlePlayerInfo)
                        )

                )
                .then(Commands.literal("spawn")

                        .then(Commands.argument("shoptype",StringArgumentType.word())

                                .executes(lotteryAdminCommandHandler::handleSpawnShopVillager)
                        )
                )

                .build();

    }



}
