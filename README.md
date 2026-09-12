# LotteryPlugin
宝くじプラグイン

開催期間や賞金額を自由にカスタマイズできる、Minecraftサーバー向けの多機能な宝くじプラグインです！

## 📌 特徴
- **自由なカスタマイズ**: `ymlファイル` から複数の宝くじを自由に設定・開催可能。
- **データ永続化**: MySQLと連携しているため、サーバーが再起動しても開催中のデータや購入履歴は安全に復元されます。
- **Vaultを入れると購入や宝くじの交換が行われます**

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
| `/lottoadmin start <lottoID>` | 指定した宝くじを開催します | `lottery.commands..` |
| `/lottoadmin info` | 開催中の宝くじの売り上げ総額などの情報を表示させます | `lottery.use` |





## トラブルシューティング






## API情報/開発者向け情報（ Developer API ）

---

