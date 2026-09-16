# LotteryPlugin
宝くじプラグイン

開催期間や賞金額を自由にカスタマイズできる、Minecraftサーバー向けの多機能な宝くじプラグインです！

## 📌 特徴
- **自由なカスタマイズ**: `ymlファイル` から複数の宝くじを自由に設定・開催可能。
- **データ永続化**: MySQLと連携しているため、サーバーが再起動しても開催中のデータや購入履歴は安全に復元されます。
- **Vaultを入れると購入や宝くじの交換が行われます**

**宝くじ開催中**

<img width="1920" height="991" alt="2026-09-16_21 29 39" src="https://github.com/user-attachments/assets/3b2b158f-ff45-45b2-9801-a49a102e4cbe" />



**販売ショップ**

<img width="1917" height="979" alt="image shop1" src="https://github.com/user-attachments/assets/5d2f9a9c-be5e-4d98-828f-c0d3660d42c8" />


**換金ショップ**

<img width="1917" height="976" alt="image shop2" src="https://github.com/user-attachments/assets/d855c97a-c592-4fc0-a6a3-9c2ea5b4fd48" />


**宝くじ情報表示村人**

<img width="1917" height="1010" alt="image" src="https://github.com/user-attachments/assets/aa0c1202-51e0-434c-9437-6da583ff2fc2" />
<img width="1917" height="974" alt="image shop3" src="https://github.com/user-attachments/assets/602330d1-6884-4489-8e9e-49e58fae57f3" />





** 宝くじの情報を表示させた場合 **

<img width="1920" height="991" alt="2026-09-16_21 35 47" src="https://github.com/user-attachments/assets/b074b3ee-7c15-4476-8f47-6157f0d31814" />



## ⚙️ 動作環境
- **Java**: `Java 21` 以上
- **Minecraft Version**: `1.21.1` 以上
- **データベース**: `MySQL`


## 設定方法 (config.yml)
宝くじの開催設定はすべて `plugins/Lottery/lotteries/..yml` から行います。
宝くじ設定の例は以下の通りです。

```yaml

# 基本設定
display-name: "§3§lサンプルジャンボ宝くじ！"
resourcepack-key: lotto   # リソースパックキー
single-ticket: single-ticket  # チケットでのテクスチャ(item_model)
pack-ticket: pack-ticket  # 封がされたパック状態でのテクスチャ(item_model)

# 購入に関する設定
ticket-price: 500   # 1枚当たりの価格
bulk-purchase-amount: 10    # 連番・バラで購入するときの基本セット枚数
max-tickets-per-player: 30    # 1人が買える最大枚数

# チケットの抽選範囲
max-group: 10   # 組の最大数(例：1組～10組)
max-number: 9999    # 番号の最大値

# 期間・演出に関する設定
duration-minutes: 10080 # 7日間
claim-expiry-days: 7    # 抽選から何日間その宝くじの交換が可能か(日)
bossbar:
  title: "§6§l[サンプルジャンボ宝くじ]   抽選まで: %time% "
  color: "BLUE"
  style: "NOTCHED_12"


# 賞金配分と当選モードの設定
prize-settings:
  type: NORMAL
  fixed-total-return-money: -1  # 売り上げ額からではなく指定した合計額から報酬を渡しましす
  total-return-rate: 0.70   # 売上額の何%をプレイヤーに還元するかどうか

  distribution:
    - display-name: "§e§l1等!"
      pool-share: 0.60    # 還元される賞金のうち60%が１等の(１枚当たり)賞金になる
      mode: "UNIQUE"    # 完全に１つの「組」と「番号」を生成して抽選
      reward-type: "MONEY"    # 報酬の種類

    - display-name: "§7２等"
      pool-share: 0.30    # 還元される賞金のうち30%
      mode: "NUMBER_ONLY"   # 組は関係なく「番号」だけ一致で当選
      reward-type: "MONEY"

    - display-name: "§c３等"
      pool-share: 0.10    # 還元される賞金のうち10%（バラ10枚買えば必ず1枚当たる）
      mode: "LAST_1_DIGIT"    # 下1桁が一致で当選
      reward-type: "MONEY"
      # reward-typeがITEMの場合はitem-value項目を追加し、itemBase64に変換された文字列を入れる


```

##  コマンド・権限
*※書き換わる可能性の高い箇所)*

| コマンド | 説明 | 権限 |
| :--- | :--- | :--- |
| `/lottoadmin start <lottoID>` | 指定した宝くじを開催します | `lottery.commands.admin` |
| `/lottoadmin info` | 開催中の宝くじの売り上げ総額などの情報を表示させます | `lottery.commands.admin` |





## トラブルシューティング






## API情報/開発者向け情報（ Developer API ）

### LotteryChargeEvent

  プレイヤーが宝くじを購入した際に呼び出されるイベントです
  
  ```java
@EventHandler
public void lotteryCharge(LotteryChargeEvent event) {
    Player player = event.getPlayer();

    // 購入にかかった金額を変数moneyで取得可能です
    int money = event.getTotalMoney();

    // 自作Bankプラグインなどの処理を呼び出したり
    // プレイヤーに独自の通知を行うことが可能です

    player.sendMessage(Component.text("宝くじを購入！ 金額：" + money + "円"))
    
}
  




---

