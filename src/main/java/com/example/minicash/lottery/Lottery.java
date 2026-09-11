package com.example.minicash.lottery;

import com.example.minicash.lottery.commands.LotteryAdminCommand;
import com.example.minicash.lottery.commands.LottoCommand;
import com.example.minicash.lottery.commands.handler.LotteryAdminCommandHandler;
import com.example.minicash.lottery.database.*;
import com.example.minicash.lottery.listener.ItemClickEvent;
import com.example.minicash.lottery.listener.LotteryGUIEvent;
import com.example.minicash.lottery.listener.PlayerConnectEvent;
import com.example.minicash.lottery.listener.ShopEvent;
import com.example.minicash.lottery.manager.LotteryClaimManager;
import com.example.minicash.lottery.manager.LotteryManager;
import com.example.minicash.lottery.manager.LotteryPurchaseManager;
import com.example.minicash.lottery.manager.config.LotteryConfigManager;
import com.example.minicash.lottery.manager.gui.LotteryGUI;
import com.example.minicash.lottery.manager.shop.VillagerShop;
import com.example.minicash.lottery.util.LotteryKeys;
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

    private LotteryGUI lotteryGUI;

    private VillagerShop villagerShop;

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

        saveDefaultConfig();


        lotteryConfigManager = new LotteryConfigManager(this);
        lotteryConfigManager.loadAllLotteryConfig();


        this.dbSetup = new Setup(this);

        dbSetup.connect();

        this.activeDatabase = new ActiveDatabase(dbSetup.getHikariSource());
        this.claimeTicketDatabase = new ClaimeTicketDatabase(this,dbSetup.getHikariSource());
        this.lotteryResultDatabase = new LotteryResultDatabase(dbSetup.getHikariSource());
        this.playerDatabase = new PlayerDatabase(dbSetup.getHikariSource());


        this.lotteryManager = new LotteryManager(this, activeDatabase , lotteryResultDatabase , lotteryConfigManager);
        this.lotteryClaimManager = new LotteryClaimManager(this,economy ,claimeTicketDatabase,lotteryConfigManager);
        this.lotteryPurchaseManager = new LotteryPurchaseManager(economy , this , activeDatabase , playerDatabase , lotteryManager);

        this.lotteryGUI = new LotteryGUI(lotteryManager,lotteryConfigManager);

        this.villagerShop = new VillagerShop(this);


        this.adminCommandHandler = new LotteryAdminCommandHandler(this,lotteryManager,activeDatabase,playerDatabase,lotteryConfigManager,villagerShop);
        this.lotteryAdminCommand = new LotteryAdminCommand(adminCommandHandler,lotteryConfigManager);

        new LotteryKeys(this);

        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new ItemClickEvent(lotteryConfigManager),this);
        pluginManager.registerEvents(new LotteryGUIEvent(),this);
        pluginManager.registerEvents(new PlayerConnectEvent(lotteryManager),this);
        pluginManager.registerEvents(new ShopEvent(this,villagerShop),this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            // register your commands here ...

            commands.registrar().register(
                    lotteryAdminCommand.createAdminCommand(),
                    "宝くじプラグイン管理者用コマンド！",
                    List.of("lottoadmin")
            );

        });

        registerCommand("lotto","コマンド",new LottoCommand(lotteryPurchaseManager,lotteryGUI,lotteryClaimManager,lotteryManager,lotteryConfigManager));

        lotteryManager.loadActiveLottery();


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
