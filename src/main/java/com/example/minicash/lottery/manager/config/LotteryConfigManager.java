package com.example.minicash.lottery.manager.config;

import com.example.minicash.lottery.data.LotteryConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LotteryConfigManager {


    private final JavaPlugin plugin;


    private final Map<String, LotteryConfig> lotteryConfigs = new HashMap<>();


    public LotteryConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    /**
     * raidsフォルダ内の全ファイルからデータを読み取ります
     */
    public void loadAllRaidConfig() {

        lotteryConfigs.clear();

        File lottoFolder = new File(plugin.getDataFolder(), "lotteries");

        // フォルダが存在しない場合は作成し、サンプルファイルを保存
        if (!lottoFolder.exists()) {
            lottoFolder.mkdirs();
            plugin.saveResource("lotteries/sample.yml", false);
        }

        File[] files = lottoFolder.listFiles((directory, name) -> name.endsWith(".yml"));

        if (files == null){
            return;
        }

        for (File file : files) {

            String raidId = file.getName().replace(".yml", "");

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            LotteryConfig lotteryConfig = new LotteryConfig(raidId, config);
            lotteryConfigs.put(raidId, lotteryConfig);

            plugin.getLogger().info("レイド設定を読み込みました: " + raidId);
        }

    }


    /**
     * 指定された宝くじIDの設定データを取得します
     * @param lotteryId 検索する宝くじID
     * @return LotteryConfig
     */
    public LotteryConfig getLotteryConfig(String lotteryId) {
        return lotteryConfigs.get(lotteryId);
    }

    /**
     * 読み込まれている全宝くじ設定のマップを取得します
     * @return 全ての宝くじの設定取得
     */
    public Map<String, LotteryConfig> getLotteryConfigs() {
        return lotteryConfigs;
    }


}
