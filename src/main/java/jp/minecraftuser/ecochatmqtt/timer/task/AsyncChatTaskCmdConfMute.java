
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import jp.minecraftuser.ecochatmqtt.EcoChatMQTT;
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
 * タスク別処理分割用 Mute クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdConfMute extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdConfMute instance = null;
    public static final AsyncChatTaskCmdConfMute getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdConfMute(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdConfMute(PluginFrame plg_) {
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
        // 実行者の権限を確認
        if (!data.player.isOp()) { // OPのみ
            Utl.sendPluginMessage(plg, data.player, "mute設定を変更する権限がありません");
            data.result = false;
            return;
        }
        UUID uid = null;
        if (((EcoChatMQTT) plg).slog == null) {
            if (((EcoChatMQTT) plg).slog != null) {
                uid = ((EcoChatMQTT) plg).slog.latestUUID(data.param[0]);
            }
        }
        if (uid == null) {
            for (Player p : plg.getServer().getOnlinePlayers()) {
                if (p.getName().equalsIgnoreCase(data.param[0])) {
                    uid = p.getUniqueId();
                    break;
                }
            }
        }
        if (uid == null) {
            Utl.sendPluginMessage(plg, data.player, "指定ユーザーのUUID検索に失敗しました");
            data.result = false;
            return;
        }
        User target = conf.getUser(uid);
        if (target == null) {
            Utl.sendPluginMessage(plg, data.player, "指定ユーザーのチャンネルチャット情報が見つかりませんでした");
            data.result = false;
            return;
        }
        
        // usngconfを取得
        EcoChatDBUser usdb = (EcoChatDBUser) plg.getDB("user");
        
        // 変更する値を判定
        boolean old = target.mute;
        target.mute = !target.mute;
        Utl.sendPluginMessage(plg, data.player, "プレイヤー({0})のmute設定を{1}から{2}に変更しました。", data.param[0], String.valueOf(old), String.valueOf(target.mute));

        // 他サーバに対ユーザーNG設定変更を通知する
        notifyConfigReload(ConfigJson.Type.USER, target.id, 0, 0);

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
            Utl.sendPluginMessage(plg, data.player, "プレイヤーのmute設定の変更に失敗しました");
        }
    }

}
