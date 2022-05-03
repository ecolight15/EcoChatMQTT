
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;

/**
 * タスク別処理分割用 PassJoin クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdPassJoin extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdPassJoin instance = null;
    public static final AsyncChatTaskCmdPassJoin getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdPassJoin(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdPassJoin(PluginFrame plg_) {
        super(plg_);
    }

    /**
     * 非同期で実施する処理
     * Bukkit/Spigotインスタンス直接操作不可
     * @param thread
     * @param db
     * @param con
     * @param data 
     */
    @Override
    public void asyncThread(AsyncWorker thread, EcoChatDB db, Connection con, ChatPayload data) {
        // 処理なし
    }

    /**
     * 応答後メインスレッド側で実施する処理
     * Bukkit/Spigotインスタンス直接操作可
     * @param thread
     * @param data 
     */
    @Override
    public void mainThread(AsyncWorker thread, ChatPayload data) {
        // 未実装
        Utl.sendPluginMessage(plg, data.player, "コマンド未実装");
    }

    

}
