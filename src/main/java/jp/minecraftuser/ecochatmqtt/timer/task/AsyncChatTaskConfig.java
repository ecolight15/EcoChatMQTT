
package jp.minecraftuser.ecochatmqtt.timer.task;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecochatmqtt.EcoChatMQTT;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.listener.ChatListener;
import jp.minecraftuser.ecochatmqtt.mqttmodel.ConfigJson;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTManagerNotFoundException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTPluginNotFoundException;
import jp.minecraftuser.ecomqtt.worker.MQTTManager;

/**
 * タスク別処理分割用 チャット クラス
 * @author ecolight
 */
public class AsyncChatTaskConfig extends AsyncChatTaskBase {
    Gson gson = new Gson();
    
    // シングルトン実装
    private static AsyncChatTaskConfig instance = null;
    public static final AsyncChatTaskConfig getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskConfig(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskConfig(PluginFrame plg_) {
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
        // チャット情報を送信
        ConfigJson json = new ConfigJson(
                data.configType,
                data.usid,
                data.chid,
                data.targetid
        );
        try {
            ((EcoChatMQTT)plg).getMQTTConfigController().publish(
                    MQTTManager.cnv(conf.getString("Topic.Config.Format"), plg.getName()),
                    gson.toJson(json).getBytes(),
                    true,
                    conf.getInt("Mqtt.Subscribe.QoS"));
        } catch (EcoMQTTManagerNotFoundException | EcoMQTTPluginNotFoundException ex) {
            Logger.getLogger(ChatListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 応答後メインスレッド側で実施する処理
     * Bukkit/Spigotインスタンス直接操作可
     * @param thread
     * @param data 
     */
    @Override
    public void mainThread(AsyncWorker thread, ChatPayload data) {

    }
}
