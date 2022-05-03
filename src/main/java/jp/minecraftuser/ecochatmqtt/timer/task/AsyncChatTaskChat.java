
package jp.minecraftuser.ecochatmqtt.timer.task;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecochatmqtt.EcoChatMQTT;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.listener.ChatListener;
import jp.minecraftuser.ecochatmqtt.mqttmodel.ChatJson;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTManagerNotFoundException;
import jp.minecraftuser.ecomqtt.io.exception.EcoMQTTPluginNotFoundException;
import jp.minecraftuser.ecomqtt.worker.MQTTManager;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 チャット クラス
 * @author ecolight
 */
public class AsyncChatTaskChat extends AsyncChatTaskBase {
    Gson gson = new Gson();
    
    // シングルトン実装
    private static AsyncChatTaskChat instance = null;
    public static final AsyncChatTaskChat getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskChat(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskChat(PluginFrame plg_) {
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
        log.log(Level.INFO, "send json tag:{0}", data.message);
        ChatJson json = new ChatJson(
                data.channelTag,
                (Player) data.player,
                data.message,
                new SimpleDateFormat(conf.getString("DateFormat")).format(new Date()),
                data.broadcast,
                data.broadcast_target,
                data.broadcast_target_uuid,
                conf.getString("Topic.Chat.URL")
        );
        try {
            ((EcoChatMQTT)plg).getMQTTChatController().publish(
                    MQTTManager.cnv(conf.getString("Topic.Chat.Format"), plg.getName()),
                    gson.toJson(json).getBytes(),
                    true,
                    conf.getInt("Mqtt.Publish.QoS"));
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
