
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
 * タスク別処理分割用 AdmSpyPM クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdAdmSpyPM extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdAdmSpyPM instance = null;
    public static final AsyncChatTaskCmdAdmSpyPM getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdAdmSpyPM(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdAdmSpyPM(PluginFrame plg_) {
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
        
        boolean bold = us.spyPM;
        us.spyPM = !us.spyPM;
        // 変更
        EcoChatDBUser usdb = (EcoChatDBUser) plg.getDB("user");
        usdb.updateUserSpy(con, us.uuid, us.spyChat, us.spyPM);
        conf.updateUser(us);
        Utl.sendPluginMessage(plg, data.player, "SPYPMフラグを{0}から{1}に変更しました", String.valueOf(bold), String.valueOf(us.spyPM));
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
