
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;

/**
 * タスク別処理分割用 History クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdHistory extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdHistory instance = null;
    public static final AsyncChatTaskCmdHistory getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdHistory(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdHistory(PluginFrame plg_) {
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
//        String enabled = db.getSetting(con, data.player.getUniqueId(), "enabled");
//        if (enabled.equalsIgnoreCase("true")) {
//            // 設定は既に有効
//            data.result = false;
//            data.msg = "既に設定は有効です";
//        } else {
//            // 無効であれば有効化する
//            db.updateSetting(con, data.player.getUniqueId(), "enabled", "true");
//
//            // ログインサーバを現在のサーバに設定
//            data.serverName = conf.getString("server.name");
//            db.updateSetting(con, data.player.getUniqueId(), "login-server", data.serverName);
//            log.log(Level.INFO, "Join data bridge network.[{0}]", data.player.getName());
//
//            data.result = true;
//        }
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
//        // プレイヤーが現時点でオンラインであれば結果送信する。
//        if (!data.player.isOnline()) return;
//        if (data.result) {
//            ((SaveInventoryListener) plg.getPluginListener("inventory")).updatePlayerServer(data.player.getUniqueId(), data.serverName);
//            Utl.sendPluginMessage(plg, data.player, "サーバー間データ共有モードの有効化に成功しました");
//        } else {
//            Utl.sendPluginMessage(plg, data.player, "サーバー間データ共有モードの有効化に失敗しました");
//            if (data.msg != null) {
//                Utl.sendPluginMessage(plg, data.player, data.msg);
//            }
//        }
    }

    

}
