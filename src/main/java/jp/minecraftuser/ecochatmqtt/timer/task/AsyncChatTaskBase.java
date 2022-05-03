
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import jp.minecraftuser.ecochatmqtt.config.EcoChatMQTTConfig;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.mqttmodel.ConfigJson;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;

/**
 * タスク別処理分割用ベースクラス
 * @author ecolight
 */
public abstract class AsyncChatTaskBase {
    protected final PluginFrame plg;
    protected final EcoChatMQTTConfig conf;
    protected final Logger log;
    
    // 継承先でシングルトン実装する (基底クラス側でできないものだろうか)
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskBase(PluginFrame plg_) {
        plg = plg_;
        log = plg.getLogger();
        conf = (EcoChatMQTTConfig) plg.getDefaultConfig();
    }

    /**
     * 他サーバーにconfigへのDBリロード依頼を送信する
     * @param type_ 更新する情報の種別
     * @param usid_ 更新対象のユーザーID
     * @param chid_ 更新対象のチャンネルID
     * @param targetid_ 更新対象の対ユーザーID
     */
    public void notifyConfigReload(ConfigJson.Type type_, int usid_, int chid_, int targetid_) {
        AsyncWorker worker = (AsyncWorker) plg.getPluginTimer("worker");
        ChatPayload data = new ChatPayload(plg, ChatPayload.Type.CONFIG);
        data.configType = type_;
        data.usid = usid_;
        data.chid = chid_;
        data.targetid = targetid_;
        worker.sendData(data);
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
    abstract public void asyncThread(AsyncWorker thread, EcoChatDB db, Connection con, ChatPayload data) throws SQLException;
    
    /**
     * 応答後メインスレッド側で実施する処理
     * Bukkit/Spigotインスタンス直接操作可
     * @param thread
     * @param data 
     */
    abstract public void mainThread(AsyncWorker thread, ChatPayload data);
}
