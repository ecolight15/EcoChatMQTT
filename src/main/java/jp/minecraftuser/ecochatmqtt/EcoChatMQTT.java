
package jp.minecraftuser.ecochatmqtt;

import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecochatmqtt.commands.AddCommand;
import jp.minecraftuser.ecochatmqtt.commands.ConfMuteCommand;
import jp.minecraftuser.ecochatmqtt.commands.AdmSpychatCommand;
import jp.minecraftuser.ecochatmqtt.commands.AdmSpypmCommand;
import jp.minecraftuser.ecochatmqtt.commands.CcCommand;
import jp.minecraftuser.ecochatmqtt.commands.ChannelColorCommand;
import jp.minecraftuser.ecochatmqtt.commands.ChannelCommand;
import jp.minecraftuser.ecochatmqtt.commands.ChannelDefCommand;
import jp.minecraftuser.ecochatmqtt.commands.ChannelGoodbyeCommand;
import jp.minecraftuser.ecochatmqtt.commands.ChannelJoinCommand;
import jp.minecraftuser.ecochatmqtt.commands.ChannelLeaveCommand;
import jp.minecraftuser.ecochatmqtt.commands.ChannelListCommand;
import jp.minecraftuser.ecochatmqtt.commands.ChannelNameCommand;
import jp.minecraftuser.ecochatmqtt.commands.ChannelOwnerCommand;
import jp.minecraftuser.ecochatmqtt.commands.ChannelPassEnableCommand;
import jp.minecraftuser.ecochatmqtt.commands.ChannelPermCommand;
import jp.minecraftuser.ecochatmqtt.commands.ChannelTagCommand;
import jp.minecraftuser.ecochatmqtt.commands.ChannelTypeCommand;
import jp.minecraftuser.ecochatmqtt.commands.ChannelWelcomeCommand;
import jp.minecraftuser.ecochatmqtt.commands.ConfChannelCommand;
import jp.minecraftuser.ecochatmqtt.commands.ConfCommand;
import jp.minecraftuser.ecochatmqtt.commands.ConfFlagCommand;
import jp.minecraftuser.ecochatmqtt.commands.ConfFlagInfoCommand;
import jp.minecraftuser.ecochatmqtt.commands.ConfFlagNguserCommand;
import jp.minecraftuser.ecochatmqtt.commands.ConfFlagRangeCommand;
import jp.minecraftuser.ecochatmqtt.commands.ConfFlagRsCommand;
import jp.minecraftuser.ecochatmqtt.commands.ConfNgPlayerCommand;
import jp.minecraftuser.ecochatmqtt.commands.ConfPlayerCommand;
import jp.minecraftuser.ecochatmqtt.commands.ConfRangeCommand;
import jp.minecraftuser.ecochatmqtt.commands.ConfRangeLocalCommand;
import jp.minecraftuser.ecochatmqtt.commands.DeleteCommand;
import jp.minecraftuser.ecochatmqtt.commands.DiceCommand;
import jp.minecraftuser.ecochatmqtt.commands.EcoChatMQTTCommand;
import jp.minecraftuser.ecochatmqtt.commands.EcoChatMQTTReloadCommand;
import jp.minecraftuser.ecochatmqtt.commands.HistoryCommand;
import jp.minecraftuser.ecochatmqtt.commands.InfoCommand;
import jp.minecraftuser.ecochatmqtt.commands.JoinCommand;
import jp.minecraftuser.ecochatmqtt.commands.KickCommand;
import jp.minecraftuser.ecochatmqtt.commands.LeaveCommand;
import jp.minecraftuser.ecochatmqtt.commands.ListCommand;
import jp.minecraftuser.ecochatmqtt.commands.PassJoinCommand;
import jp.minecraftuser.ecochatmqtt.commands.PmCommand;
import jp.minecraftuser.ecochatmqtt.commands.PmHistoryCommand;
import jp.minecraftuser.ecochatmqtt.commands.RsCommand;
import jp.minecraftuser.ecochatmqtt.commands.SetCommand;
import jp.minecraftuser.ecochatmqtt.commands.WhoCommand;
import jp.minecraftuser.ecochatmqtt.config.EcoChatMQTTConfig;
import jp.minecraftuser.ecochatmqtt.config.EcoChatMQTTDefaultChannelConfig;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannel;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelConf;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelPassword;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelUser;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUser;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUserChannelConf;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUserNgConf;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUserUserConf;
import jp.minecraftuser.ecochatmqtt.listener.ChatListener;
import jp.minecraftuser.ecochatmqtt.listener.LoginLogoutListener;
import jp.minecraftuser.ecochatmqtt.receiver.ChatReceiver;
import jp.minecraftuser.ecochatmqtt.receiver.ConfigReceiver;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTManagerNotFoundException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTPluginNotFoundException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTRegisterFailException;
import jp.minecraftuser.ecomqtt.worker.MQTTManager;
import jp.minecraftuser.ecomqttserverlog.EcoMQTTServerLog;
import org.bukkit.entity.Player;

/**
 * EcoMQTTプラグインを利用したサーバー関連ログのpublishプラグイン
 * @author ecolight
 */
public class EcoChatMQTT extends PluginFrame {
    //private EnableDisableListener listener;
    static ChatReceiver mqttChat;
    static ConfigReceiver mqttConfig;
    public EcoMQTTServerLog slog;

    /**
     * 起動時処理
     */
    @Override
    public void onEnable() {
        initialize();
        if (!getDefaultConfig().getBoolean("Enabled")) {
            Utl.sendPluginMessage(this, null, "チャット機能は無効化されています。設定を確認してください。");
            log.warning("EcoChatMQTT plugin is disabled. please check settings.");
            return;
        }
        try {
            // チャットデータ通信用 QoS 1 で受信設定
            ChatReceiver chat_ = getMQTTChatController();
            chat_.registerReceiver(
                    MQTTManager.cnv(
                        getDefaultConfig().getString("Topic.Chat.Format"),
                        getName(),                                          // 
                        "{server}"                                          // 全サーバから受信するため、サーバ名をシングルレベルワイルドカード指定で受信登録する
                    ),
                    chat_,                                                   // レシーブハンドラの指定
                    true,                                                   // 余計なprefixを付けない指定
                    1);                                                     // QoS1(必ず1回は受信)
            // チャットデータ通信用 QoS 1 で受信設定
            ConfigReceiver config_ = getMQTTConfigController();
            config_.registerReceiver(
                    MQTTManager.cnv(
                        getDefaultConfig().getString("Topic.Config.Format"),
                        getName(),                                          // 
                        "{server}"                                          // 全サーバから受信するため、サーバ名をシングルレベルワイルドカード指定で受信登録する
                    ),
                    config_,                                                // レシーブハンドラの指定
                    true,                                                   // 余計なprefixを付けない指定
                    1);                                                     // QoS1(必ず1回は受信)
        } catch (EcoMQTTPluginNotFoundException ex) {
            Logger.getLogger(EcoChatMQTT.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EcoMQTTManagerNotFoundException ex) {
            Logger.getLogger(EcoChatMQTT.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EcoMQTTRegisterFailException ex) {
            Logger.getLogger(EcoChatMQTT.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // ログインユーザーの情報を読み直す
        for (Player pl : getServer().getOnlinePlayers()) {
            // ログイン時のUserロード　すでに読み込み済みでも読み直す
            AsyncWorker worker = (AsyncWorker) getPluginTimer("worker");
            ChatPayload data = new ChatPayload(this, pl, ChatPayload.Type.LOAD);
            worker.sendData(data);            
        }
        
        // サーバログプラグイン
        slog = (EcoMQTTServerLog) getServer().getPluginManager().getPlugin("EcoMQTTServerLog");
    }

    /**
     * 終了時処理
     */
    @Override
    public void onDisable() {
    //    listener.onDisable();
        disable();
    }
    

    /**
     * MQTTController(Chat) インスタンスを取得する
     * @return MQTTController インスタンス
     */
    public ChatReceiver getMQTTChatController() {
        if (mqttChat == null) { mqttChat = new ChatReceiver(this); }
        return mqttChat;
    }

    /**
     * MQTTController（Config) インスタンスを取得する
     * @return MQTTController インスタンス
     */
    public ConfigReceiver getMQTTConfigController() {
        if (mqttConfig == null) { mqttConfig = new ConfigReceiver(this); }
        return mqttConfig;
    }

    /**
     * 設定初期化
     */
    @Override
    public void initializeConfig() {
        EcoChatMQTTConfig conf = new EcoChatMQTTConfig(this);

        conf.registerBoolean("Enabled");
        
        // Other settings
        conf.registerString("DateFormat");

        // Topic settings
        conf.registerBoolean("Topic.Config.Enable");
        conf.registerString("Topic.Config.Format");
        conf.registerString("Topic.Config.URL");
        conf.registerBoolean("Topic.Chat.Enable");
        conf.registerString("Topic.Chat.Format");
        conf.registerString("Topic.Chat.URL");

        // MQTT settings
        conf.registerInt("Mqtt.Publish.QoS");
        conf.registerInt("Mqtt.Subscribe.QoS");

        // DB設定
        conf.registerBoolean("Database.user");
        conf.registerString("Database.type");
        conf.registerString("Database.name");
        conf.registerString("Database.server");
        conf.registerString("Database.user");
        conf.registerString("Database.pass");

        // 新規作成チャンネルのデフォルト設定（DBの新規作成時のみ）
        conf.registerString("ChannelDefault.Type");
        conf.registerString("ChannelDefault.EnterMessage");
        conf.registerString("ChannelDefault.LeaveMessage");
        conf.registerString("ChannelDefault.WelcomeMessage");
        conf.registerString("ChannelDefault.GoodbyeMessage");
        conf.registerBoolean("ChannelDefault.AutoJoin");
        conf.registerBoolean("ChannelDefault.ListEnabled");
        conf.registerBoolean("ChannelDefault.AddReqPerm");
        conf.registerBoolean("ChannelDefault.Activate");

        registerPluginConfig(conf);
        
        // デフォルトチャンネルは専用のコンフィグから読み込む
        // 複数サーバー連携する場合はいずれかのサーバーで1度実施すれば良い
        EcoChatMQTTDefaultChannelConfig defch = new EcoChatMQTTDefaultChannelConfig(this, "default.yml", "defch");
        defch.registerSectionString("DefaultChannels");
        log.log(Level.INFO, "DefaultChannels count = {0}", defch.getSectionList("DefaultChannels").size());
        for (String tag : defch.getSectionList("DefaultChannels")) {
            defch.registerString("DefaultChannels." + tag + ".Name");
            defch.registerString("DefaultChannels." + tag + ".Type");
            defch.registerString("DefaultChannels." + tag + ".EnterMessage");
            defch.registerString("DefaultChannels." + tag + ".LeaveMessage");
            defch.registerString("DefaultChannels." + tag + ".WelcomeMessage");
            defch.registerString("DefaultChannels." + tag + ".GoodbyeMessage");
            defch.registerBoolean("DefaultChannels." + tag + ".AutoJoin");
            defch.registerBoolean("DefaultChannels." + tag + ".ListEnabled");
            defch.registerBoolean("DefaultChannels." + tag + ".AddReqPerm");
            defch.registerBoolean("DefaultChannels." + tag + ".Activate");
        }
        defch.registerString("DefaultActive");
        log.log(Level.INFO, "DefaultActive = {0}", defch.getString("DefaultActive"));
        registerPluginConfig(defch);
    }

    /**
     * コマンド初期化
     */
    @Override
    public void initializeCommand() {
        if (!getDefaultConfig().getBoolean("Enabled")) {
            return;
        }
        // 単体実行可能なチャットコマンド
        registerPluginCommand(new AddCommand(this, "add"));
        CommandFrame ch = new ChannelCommand(this, "channel");
            ch.addCommand(new ChannelNameCommand(this, "name"));
            ch.addCommand(new ChannelTypeCommand(this, "type"));
            ch.addCommand(new ChannelColorCommand(this, "color"));
            ch.addCommand(new ChannelDefCommand(this, "def"));
            ch.addCommand(new ChannelListCommand(this, "list"));
            ch.addCommand(new ChannelPassEnableCommand(this, "passenable"));
            ch.addCommand(new ChannelJoinCommand(this, "join"));
            ch.addCommand(new ChannelLeaveCommand(this, "leave"));
            ch.addCommand(new ChannelWelcomeCommand(this, "welcome"));
            ch.addCommand(new ChannelGoodbyeCommand(this, "goodbye"));
            ch.addCommand(new ChannelOwnerCommand(this, "owner"));
            ch.addCommand(new ChannelTagCommand(this, "tag"));
            ch.addCommand(new ChannelPermCommand(this, "perm"));
        registerPluginCommand(ch);
        CommandFrame conf = new ConfCommand(this, "conf");
            conf.addCommand(new ConfChannelCommand(this, "channel"));
            conf.addCommand(new ConfPlayerCommand(this, "player"));
            conf.addCommand(new ConfNgPlayerCommand(this, "ngplayer"));
            CommandFrame flag = new ConfFlagCommand(this, "flag");
                flag.addCommand(new ConfFlagInfoCommand(this, "info"));
                flag.addCommand(new ConfFlagNguserCommand(this, "nguser"));
                flag.addCommand(new ConfFlagRangeCommand(this, "range"));
                flag.addCommand(new ConfFlagRsCommand(this, "rs"));
            conf.addCommand(flag);
            CommandFrame range = new ConfRangeCommand(this, "range");
                range.addCommand(new ConfRangeLocalCommand(this, "local"));
            conf.addCommand(range);
            conf.addCommand(new ConfMuteCommand(this, "mute"));
        registerPluginCommand(conf);
        registerPluginCommand(new DeleteCommand(this, "delete"));
        registerPluginCommand(new DiceCommand(this, "dice"));
        registerPluginCommand(new HistoryCommand(this, "history"));
        registerPluginCommand(new JoinCommand(this, "join"));
        registerPluginCommand(new LeaveCommand(this, "leave"));
        registerPluginCommand(new PassJoinCommand(this, "passjoin"));
        registerPluginCommand(new PmCommand(this, "pm"));
        registerPluginCommand(new PmHistoryCommand(this, "pmhistory"));
        registerPluginCommand(new RsCommand(this, "rs"));
        registerPluginCommand(new SetCommand(this, "set"));
        
        // 単体実行可能なチャットコマンドをccのサブコマンドに追加
        CommandFrame cc = new CcCommand(this, "cc");
        for (CommandFrame f : cmdMap.values()) {
            cc.addCommand(f);
        }
        
        // cc or ecc が頭に必要な管理系サブコマンド
        cc.addCommand(new AdmSpychatCommand(this, "spychat"));
        cc.addCommand(new AdmSpypmCommand(this, "spypm"));
        // cc or ecc が頭に必要なサブコマンド
        cc.addCommand(new InfoCommand(this, "info"));
        cc.addCommand(new KickCommand(this, "kick"));
        cc.addCommand(new ListCommand(this, "list"));
        cc.addCommand(new WhoCommand(this, "who"));
        
        // ccの全サブコマンドをeccにまるごと入れることでエイリアスとする
        CommandFrame ecc = new CcCommand(this, "ecc");
        for (CommandFrame f : cc.getCommandList()) {
            ecc.addCommand(f);
        }
        registerPluginCommand(cc);
        registerPluginCommand(ecc);

        // cc / ecc に含まれないコマンドの定義はここで実施する
        CommandFrame cmd = new EcoChatMQTTCommand(this, "eccm");
        cmd.addCommand(new EcoChatMQTTReloadCommand(this, "reload"));
        registerPluginCommand(cmd);
    }

    /**
     * イベントリスナー初期化
     */
    @Override
    public void initializeListener() {
        if (!getDefaultConfig().getBoolean("Enabled")) {
            return;
        }
        registerPluginListener(new LoginLogoutListener(this, "loginlogout"));
        registerPluginListener(new ChatListener(this, "chat"));
    }
    
    /**
     * 定期実行タイマー初期化
     */
    @Override
    public void initializeTimer() {
        if (!getDefaultConfig().getBoolean("Enabled")) {
            return;
        }
        AsyncWorker timer = AsyncWorker.getInstance(this, "worker");
        registerPluginTimer(timer);
        timer.runTaskTimer(this, 0, 20);
    }
    
    /**
     * データベース初期化
     */
    @Override
    public void initializeDB() {
        EcoChatMQTTConfig conf = (EcoChatMQTTConfig) getDefaultConfig();
        if (!conf.getBoolean("Enabled")) {
            return;
        }
        try {
            if (conf.getString("Database.type").equalsIgnoreCase("sqlite")) {
                registerPluginDB(new EcoChatDB(this, conf.getString("Database.name"), "chat"));
            } else if (conf.getString("Database.type").equalsIgnoreCase("mysql")) {
                registerPluginDB(new EcoChatDB(this,
                        conf.getString("Database.server"),
                        conf.getString("Database.user"),
                        conf.getString("Database.pass"),
                        conf.getString("Database.name"),
                        "chat"));
            }
            registerPluginDB(new EcoChatDBChannel(getDB("chat"), "channel"));
            registerPluginDB(new EcoChatDBChannelConf(getDB("chat"), "channel_conf"));
            registerPluginDB(new EcoChatDBChannelPassword(getDB("chat"), "channel_password"));
            registerPluginDB(new EcoChatDBChannelUser(getDB("chat"), "channel_user"));
            registerPluginDB(new EcoChatDBUser(getDB("chat"), "user"));
            registerPluginDB(new EcoChatDBUserChannelConf(getDB("chat"), "user_channel_conf"));
            registerPluginDB(new EcoChatDBUserNgConf(getDB("chat"), "user_ng_conf"));
            registerPluginDB(new EcoChatDBUserUserConf(getDB("chat"), "user_user_conf"));
            
            // configにデータ読み込み
            EcoChatMQTTConfig cnf = (EcoChatMQTTConfig) getDefaultConfig();
            cnf.loadDatabase();
        } catch (Exception ex) {
            Logger.getLogger(EcoChatMQTT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
