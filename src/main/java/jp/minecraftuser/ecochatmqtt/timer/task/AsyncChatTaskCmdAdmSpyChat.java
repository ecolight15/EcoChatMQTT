
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 AdmSpyChat クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdAdmSpyChat extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdAdmSpyChat instance = null;
    public static final AsyncChatTaskCmdAdmSpyChat getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdAdmSpyChat(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdAdmSpyChat(PluginFrame plg_) {
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
        // アクティブチャンネル
        User us = conf.getUser((Player) data.player);
        if (us == null) {
            Utl.sendPluginMessage(plg, data.player, "ユーザ情報の取得に失敗しました");
            data.result = false;
            return;
        }
        
        boolean bold = us.spyChat;
        us.spyChat = !us.spyChat;
        // 変更
        EcoChatDBUser usdb = (EcoChatDBUser) plg.getDB("user");
        usdb.updateUserSpy(con, us.uuid, us.spyChat, us.spyPM);
        conf.updateUser(us);
        Utl.sendPluginMessage(plg, data.player, "SPYCHATフラグを{0}から{1}に変更しました", String.valueOf(bold), String.valueOf(us.spyChat));
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
    }

}
