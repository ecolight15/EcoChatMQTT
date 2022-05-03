
package jp.minecraftuser.ecochatmqtt.timer.task;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannel;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelConf;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelPassword;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelUser;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUser;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUserChannelConf;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUserNgConf;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUserUserConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelPassword;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserChannelConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserNgConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserUserConf;
import jp.minecraftuser.ecochatmqtt.mqttmodel.ConfigJson;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;

/**
 * タスク別処理分割用 チャット クラス
 * @author ecolight
 */
public class AsyncChatTaskConfigReceive extends AsyncChatTaskBase {
    Gson gson = new Gson();
    
    // シングルトン実装
    private static AsyncChatTaskConfigReceive instance = null;
    public static final AsyncChatTaskConfigReceive getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskConfigReceive(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskConfigReceive(PluginFrame plg_) {
        super(plg_);
    }

    /**
     * 非同期で実施する処理
     * Bukkit/Spigotインスタンス直接操作不可
     * @param thread
     * @param db
     * @param con
     * @param data 
     * @throws java.sql.SQLException 
     */
    @Override
    public void asyncThread(AsyncWorker thread, EcoChatDB db, Connection con, ChatPayload data) throws SQLException {
        // jsonのデコード
        ConfigJson json;
        try {
            json = gson.fromJson(data.message, ConfigJson.class);
            json.check();   // メンバのnullチェック
        } catch(Exception e) {
            plg.getLogger().log(Level.WARNING,
                    "gson convert failed. payload[{1}]",
                    new Object[]{data.message});
            plg.getLogger().log(Level.WARNING, null, e);
            data.result = false;
            return;
        }
        
        // DBの用意
        EcoChatDBChannel chdb = (EcoChatDBChannel) plg.getDB("channel");
        EcoChatDBChannelConf chconfdb = (EcoChatDBChannelConf) plg.getDB("channel_conf");
        EcoChatDBChannelPassword chpsdb = (EcoChatDBChannelPassword) plg.getDB("channel_password");
        EcoChatDBChannelUser chusdb = (EcoChatDBChannelUser) plg.getDB("channel_user");
        EcoChatDBUser usdb = (EcoChatDBUser) plg.getDB("user");
        EcoChatDBUserChannelConf uschdb = (EcoChatDBUserChannelConf) plg.getDB("user_channel_conf");
        EcoChatDBUserNgConf usngdb = (EcoChatDBUserNgConf) plg.getDB("user_ng_conf");
        EcoChatDBUserUserConf ususdb = (EcoChatDBUserUserConf) plg.getDB("user_user_conf");
    
        // コンフィグ更新種別
        ConfigJson.Type type = ConfigJson.Type.getByName(json.type);

        switch (type) {
            case CHANNEL:
                try {
                    Channel ch = chdb.reloadChanel(con, json.ch);
                    conf.updateChannel(ch);
                } catch (SQLException e) {
                    conf.deleteChannel(conf.getChannel(json.ch));
                }
                break;
            case CHANNEL_PASSWORD:
                try {
                    ChannelPassword chps = chpsdb.reloadChannelPassword(con, json.ch);
                    conf.updateChannelPassword(chps);
                } catch (SQLException e) {
                    conf.deleteChannelPassword(conf.getChannel(json.ch));
                }
                break;
            case CHANNEL_USER:
                try {
                    ChannelUser chus = chusdb.reloadChannelUser(con, json.ch, json.us);
                    conf.updateChannelUser(chus);
                } catch(SQLException e) {
                    conf.deleteChannelUser_User(conf.getChannel(json.ch), conf.getUser(json.us));
                }
                break;
            case CHANNEL_CONF:
                try {
                    ChannelConf chconf = chconfdb.reloadChanelConf(con, json.ch);
                    conf.updateChannelConf(chconf);
                } catch (SQLException e) {
                    conf.deleteChannelConf(conf.getChannel(json.ch));
                }
                break;
            case USER:
                try {
                    User us = usdb.reloadUser(con, json.us);
                    conf.updateUser(us);
                } catch (SQLException e) {
                    conf.deleteUser(conf.getUser(json.us));
                }
                break;
            case USER_CHANNEL_CONF:
                try {
                    UserChannelConf uschconf = uschdb.reloadUserChannelConf(con, json.us, json.ch);
                    conf.updateUserChannelConf(uschconf);
                } catch (SQLException e) {
                    conf.deleteUserChannelConf(conf.getUser(json.us), conf.getChannel(json.ch));
                }
                break;
            case USER_NG_CONF:
                try {
                    UserNgConf usng = usngdb.reloadUserNgConf(con, json.us, json.target);
                    conf.updateUserNgConf(usng);
                } catch (SQLException e) {
                    conf.deleteUserNgConf(conf.getUser(json.ch), conf.getUser(json.target));
                }
                break;
            case USER_USER_CONF:
                try {
                    UserUserConf ususconf = ususdb.reloadUserUserConf(con, json.us, json.target);
                    conf.updateUserUserConf(ususconf);
                } catch (SQLException e) {
                    conf.deleteUserUserConf(conf.getUser(json.us), conf.getUser(json.target));
                }
                break;
            default:
                log.log(Level.WARNING, "Unknown config reload type [{0}]", json.type);
                break;
        }
        data.result = true;
    }

    /**
     * 応答後メインスレッド側で実施する処理
     * Bukkit/Spigotインスタンス直接操作可
     * @param thread
     * @param data 
     */
    @Override
    public void mainThread(AsyncWorker thread, ChatPayload data) {
        //
    }

}
