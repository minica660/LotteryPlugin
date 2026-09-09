package com.example.minicash.lottery;

import com.example.minicash.lottery.commands.LotteryAdminCommand;
import com.example.minicash.lottery.commands.LotteryAdminCommandHandler;
import com.example.minicash.lottery.database.*;
import com.example.minicash.lottery.listener.ItemClickEvent;
import com.example.minicash.lottery.manager.LotteryClaimManager;
import com.example.minicash.lottery.manager.LotteryManager;
import com.example.minicash.lottery.manager.LotteryPurchaseManager;
import com.example.minicash.lottery.manager.LotteryTicketGenerator;
import com.example.minicash.lottery.manager.config.LotteryConfigManager;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Lottery extends JavaPlugin {

    private static Economy economy;

    private LotteryConfigManager lotteryConfigManager;

    private Setup dbSetup;
    private ActiveDatabase activeDatabase;
    private ClaimeTicketDatabase claimeTicketDatabase;
    private LotteryResultDatabase lotteryResultDatabase;
    private PlayerDatabase playerDatabase;

    private LotteryManager lotteryManager;
    private LotteryClaimManager lotteryClaimManager;
    private LotteryPurchaseManager lotteryPurchaseManager;


    // コマンド関連
    private LotteryAdminCommand lotteryAdminCommand;
    private LotteryAdminCommandHandler adminCommandHandler;


    @Override
    public void onEnable() {
        // Plugin startup logic
        if (!setupEconomy()) {
            getLogger().severe("Vault API not found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


        lotteryConfigManager = new LotteryConfigManager(this);
        lotteryConfigManager.loadAllRaidConfig();


        this.dbSetup = new Setup(this);
        this.activeDatabase = new ActiveDatabase(dbSetup.getHikariSource());
        this.claimeTicketDatabase = new ClaimeTicketDatabase(this,dbSetup.getHikariSource());
        this.lotteryResultDatabase = new LotteryResultDatabase(dbSetup.getHikariSource());
        this.playerDatabase = new PlayerDatabase(dbSetup.getHikariSource());


        this.lotteryManager = new LotteryManager(this, activeDatabase , lotteryResultDatabase , lotteryConfigManager);
        this.lotteryClaimManager = new LotteryClaimManager(this,economy ,claimeTicketDatabase);
        this.lotteryPurchaseManager = new LotteryPurchaseManager(economy , this , activeDatabase , playerDatabase , lotteryManager);


        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new ItemClickEvent(lotteryConfigManager),this);


        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            // register your commands here ...

            commands.registrar().register(
                    lotteryAdminCommand.createAdminCommand(),
                    "宝くじプラグイン管理者用コマンド！",
                    List.of("lottoadmin")
            );

        });



    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        dbSetup.disConnect();
    }



    //　定義してみた？
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();

        return true;
    }

    public static Economy getEconomy() {
        return economy;
    }


    public static Component getMessage(Component message){
        return Component.text("[").color(NamedTextColor.DARK_AQUA).append(Component.text("MLottery").color(NamedTextColor.GREEN).append(Component.text("]").color(NamedTextColor.DARK_AQUA)
                .append(message)
        ));
    }

}
