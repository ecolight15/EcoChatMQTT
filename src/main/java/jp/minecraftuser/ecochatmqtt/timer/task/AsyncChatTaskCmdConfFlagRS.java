
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.mqttmodel.ConfigJson;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 ConfFlagRS クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdConfFlagRS extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdConfFlagRS instance = null;
    public static final AsyncChatTaskCmdConfFlagRS getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdConfFlagRS(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdConfFlagRS(PluginFrame plg_) {
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
        // 対象者を取得
        User us = conf.getUser((Player) data.player);
        
        // usdbを取得
        EcoChatDBUser usdb = (EcoChatDBUser) plg.getDB("user");
        
        // 変更する値を判定
        boolean old = us.rsWarn;
        us.rsWarn = !us.rsWarn;
        usdb.updateUser(con, us);
        Utl.sendPluginMessage(plg, data.player, "rsWarn設定を{0}から{1}に変更しました。", String.valueOf(old), String.valueOf(us.rsWarn));

        // 他サーバに対ユーザーNG設定変更を通知する
        notifyConfigReload(ConfigJson.Type.USER, us.id, 0, 0);

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
        if (!data.result) {
            Utl.sendPluginMessage(plg, data.player, "プレイヤーのrsWarn設定の変更に失敗しました");
        }
    }

}
