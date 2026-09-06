package com.example.minicash.lottery;

import com.example.minicash.lottery.manager.config.LotteryConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Lottery extends JavaPlugin {

    private static Economy economy;

    private LotteryConfigManager lotteryConfigManager;

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




    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }



    //　定義してみた？
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        // ServicesManagerからEconomyプロバイダーを取得
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
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
