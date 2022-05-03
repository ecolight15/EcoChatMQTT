
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannel;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;

/**
 * タスク別処理分割用 Create クラス
 * @author ecolight
 */
public class AsyncChatTaskCreate extends AsyncChatTaskBase {

    // シングルトン実装
    private static AsyncChatTaskCreate instance = null;
    public static final AsyncChatTaskCreate getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCreate(plg_);
        }
        return instance;
    }

    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCreate(PluginFrame plg_) {
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
        // 処理依頼されたチャンネルを作成する
        EcoChatDBChannel chdb = (EcoChatDBChannel) plg.getDB("channel");
        chdb.insertChannel(con, data.channel);
    }

    /**
     * 応答後メインスレッド側で実施する処理
     * Bukkit/Spigotインスタンス直接操作可
     * @param thread
     * @param data 
     */
    @Override
    public void mainThread(AsyncWorker thread, ChatPayload data) {
        if (data.result) {
            log.info("[" + data.channel.tag + "] channel created.");
        } else {
            log.warning("[" + data.channel.tag + "] channel create failed.");
        }
    }

}
