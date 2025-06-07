# EcoChatMQTT

EcoChatMQTTは、MQTTプロトコルを使用してサーバー間通信機能を提供する高度なチャット機能を持つMinecraftプラグインです。複数のMinecraftサーバー間でチャットメッセージ、プライベートメッセージ、その他のチャット関連データをリアルタイムで共有することができます。

## 機能

### コアチャットシステム
- **サーバー間チャット**: MQTTを介した複数のMinecraftサーバー間での通信
- **チャンネルシステム**: 複数のチャットチャンネル（グローバル、ワールド、ローカル）をサポート
- **プライベートメッセージ**: サーバー間でのプレイヤー間直接メッセージング
- **チャット履歴**: チャットメッセージの永続的な保存と取得
- **ダイスロール**: ゲーム用の組み込みダイスコマンド

### チャンネル管理
- **複数のチャンネルタイプ**:
  - **グローバル**: 全サーバーの全プレイヤーに表示されるメッセージ
  - **ワールド**: 同じワールド内のプレイヤーに表示されるメッセージ
  - **ローカル**: 定義された範囲内のプレイヤーに表示されるメッセージ
- **チャンネル設定**: カスタマイズ可能な参加/退出メッセージ、ウェルカム/グッバイメッセージ
- **権限ベースのアクセス**: 特定のチャンネルに参加できるユーザーを制御
- **パスワード保護**: オプションのパスワード保護チャンネル
- **自動参加**: プレイヤーログイン時の自動チャンネル参加

### 高度な機能
- **メッセージフィルタリング**: 不要なユーザーとコンテンツをブロック
- **管理者スパイモード**: プライベートメッセージとチャンネル活動の監視
- **範囲ベースのローカルチャット**: ローカルチャットの設定可能な距離
- **データベースストレージ**: 永続データのためのSQLiteまたはMySQLサポート
- **リアルタイム同期**: サーバー間での即座のメッセージ配信

## 依存関係

このプラグインには以下の依存関係のインストールが必要です：

- **EcoFramework** (v0.28以上)
- **EcoMQTT** (v0.10以上)
- **EcoMQTTServerLog** (v0.7以上) - オプション
- **Spigot/Paper** (1.18.2以上対応)

## インストール

1. EcoChatMQTTプラグインのJARファイルをダウンロード
2. 必要な全ての依存関係をインストール（EcoFramework、EcoMQTT）
3. プラグインJARをサーバーの`plugins/`ディレクトリに配置
4. EcoMQTTプラグインでMQTTブローカー設定を構成
5. サーバーを起動してデフォルト設定ファイルを生成
6. 以下に説明するようにプラグインを設定
7. サーバーを再起動するか`/ecms reload`を使用

## 設定

### メイン設定 (config.yml)

```yaml
# プラグインの有効/無効
Enabled: false

# タイムスタンプの日付形式
DateFormat: "yyyy/MM/dd HH:mm:ss.SSS"

# MQTTトピック設定
Topic:
  Chat:
    Enable: true
    Format: "{server}/p/{plugin}/chat"
    URL: ""
  Config:
    Enable: true
    Format: "{server}/p/{plugin}/config"
    URL: ""

# MQTT Quality of Service設定
Mqtt:
  Publish:
    QoS: 1
  Subscribe:
    QoS: 1

# データベース設定
Database:
  type: "sqlite"
  name: "chat.db"
  server: "localhost:port"
  user: "user"
  pass: "pass"

# デフォルトチャンネル設定
ChannelDefault:
  Type: "global"
  EnterMessage: "{PLAYER} join the {NAME} channel."
  LeaveMessage: "{PLAYER} leave the {NAME} channel."
  WelcomeMessage: "Welcome, {TAG} Channel."
  GoodbyeMessage: "Goodbye, {TAG} Channel."
  AutoJoin: false
  ListEnabled: false
  AddReqPerm: false
  Activate: true
```

### デフォルトチャンネル (default.yml)

プラグインには3つの事前定義されたチャンネルが付属しています：

- **G (General)**: 全サーバーの全プレイヤーに表示されるグローバルチャット
- **W (World)**: 同じワールドのプレイヤー向けのワールド固有チャット
- **L (Local)**: 特定の範囲内のプレイヤー向けのローカルチャット

## コマンド

### 基本チャットコマンド

| コマンド | 説明 | 権限 |
|---------|-------------|------------|
| `/join <channel>` | チャットチャンネルに参加 | `ecochatmqtt.chat.join` |
| `/leave <channel>` | チャットチャンネルから退出 | `ecochatmqtt.chat.leave` |
| `/passjoin <channel> <password>` | パスワード保護チャンネルに参加 | `ecochatmqtt.chat.passjoin` |
| `/pm <player> <message>` | プライベートメッセージを送信 | `ecochatmqtt.chat.pm` |
| `/rs <message>` | 最後のプライベートメッセージに返信 | `ecochatmqtt.chat.rs` |
| `/cc <channel>` | アクティブチャンネルを切り替え | `ecochatmqtt.chat.cc` |
| `/ecc` | 現在のチャンネル情報を表示 | `ecochatmqtt.chat.ecc` |

### チャンネル管理

| コマンド | 説明 | 権限 |
|---------|-------------|------------|
| `/channel name <channel> <name>` | チャンネル表示名を設定 | `ecochatmqtt.chat.channel.name` |
| `/channel type <channel> <type>` | チャンネルタイプを設定 | `ecochatmqtt.chat.channel.type` |
| `/channel color <channel> <color>` | チャンネルカラーを設定 | `ecochatmqtt.chat.channel.color` |
| `/channel list` | 利用可能なチャンネルをリスト表示 | `ecochatmqtt.chat.channel.list` |
| `/channel welcome <channel> <message>` | ウェルカムメッセージを設定 | `ecochatmqtt.chat.channel.welcome` |
| `/channel goodbye <channel> <message>` | グッバイメッセージを設定 | `ecochatmqtt.chat.channel.goodbye` |
| `/channel owner <channel> <player>` | チャンネル所有者を設定 | `ecochatmqtt.chat.channel.owner` |
| `/channel perm <channel> <permission>` | チャンネル権限を設定 | `ecochatmqtt.chat.channel.perm` |

### 履歴と情報

| コマンド | 説明 | 権限 |
|---------|-------------|------------|
| `/history [page/date]` | チャット履歴を表示 | `ecochatmqtt.chat.history` |
| `/pmhistory <player> [page]` | プライベートメッセージ履歴を表示 | `ecochatmqtt.chat.pmhistory` |
| `/delete <message_id>` | メッセージを削除 | `ecochatmqtt.chat.delete` |
| `/dice [sides]` | ダイスを振る | `ecochatmqtt.chat.dice` |

### 設定コマンド

| コマンド | 説明 | 権限 |
|---------|-------------|------------|
| `/ecms reload` | プラグイン設定をリロード | `ecochatmqtt.reload` |
| `/conf channel <settings>` | チャンネル設定を構成 | `ecochatmqtt.chat.conf.channel` |
| `/conf player <settings>` | プレイヤー設定を構成 | `ecochatmqtt.chat.conf.player` |
| `/conf flag <settings>` | フラグ設定を構成 | `ecochatmqtt.chat.conf.flag` |

### 管理者コマンド

| コマンド | 説明 | 権限 |
|---------|-------------|------------|
| `/add <player> <channel>` | プレイヤーをチャンネルに追加 | `ecochatmqtt.chat.add` |
| `/set <player> <channel>` | プレイヤーのアクティブチャンネルを設定 | `ecochatmqtt.chat.set` |

## MQTT通信

プラグインはサーバー間通信にMQTTトピックを使用します：

- **チャットトピック**: `{server}/p/{plugin}/chat` - チャットメッセージに使用
- **設定トピック**: `{server}/p/{plugin}/config` - 設定更新に使用

### トピック形式変数
- `{server}`: サーバー名識別子
- `{plugin}`: プラグイン名（EcoChatMQTT）

## チャンネルタイプ

### グローバルチャンネル
メッセージは接続されている全サーバーの全プレイヤーに表示されます。

### ワールドチャンネル  
メッセージは同じワールド内のプレイヤーにのみ表示されます。

### ローカルチャンネル
メッセージは設定可能な距離範囲内のプレイヤーにのみ表示されます。

## データベース

プラグインは以下の保存用にSQLiteとMySQLの両方のデータベースをサポートします：
- チャットメッセージと履歴
- チャンネル設定
- ユーザー設定
- プライベートメッセージログ
- チャンネルメンバーシップ

### SQLite（デフォルト）
追加のセットアップは不要です。データベースファイルは自動的に作成されます。

### MySQL
`config.yml`でデータベース接続詳細を設定：
```yaml
Database:
  type: "mysql"
  name: "ecochat"
  server: "localhost:3306"
  user: "username"
  pass: "password"
```

## 権限

### 基本権限
- `ecochatmqtt.chat.*` - 全チャットコマンドへのアクセス
- `ecochatmqtt.chat.join` - チャンネル参加
- `ecochatmqtt.chat.leave` - チャンネル退出
- `ecochatmqtt.chat.pm` - プライベートメッセージ送信
- `ecochatmqtt.chat.history` - チャット履歴表示

### 管理者権限
- `ecochatmqtt.reload` - プラグイン設定リロード
- `ecochatmqtt.chat.channel.*` - 完全なチャンネル管理
- `ecochatmqtt.chat.conf.*` - 設定アクセス
- `ecochatmqtt.chat.add` - プレイヤーをチャンネルに追加

## ライセンス

このプロジェクトは GNU Lesser General Public License v3.0 (LGPL-3.0) の下でライセンスされています。詳細については [LICENSE](LICENSE) ファイルを参照してください。

## 作者

**ecolight** - プラグイン開発者・メンテナー

## バージョン

現在のバージョン: 0.7

## サポート

問題、機能リクエスト、またはサポートについては、プロジェクトリポジトリを参照するか、プラグイン作者に連絡してください。
