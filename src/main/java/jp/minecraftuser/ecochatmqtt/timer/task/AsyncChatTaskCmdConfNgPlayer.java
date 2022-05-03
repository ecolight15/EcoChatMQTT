
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import jp.minecraftuser.ecochatmqtt.EcoChatMQTT;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUserNgConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserNgConf;
import jp.minecraftuser.ecochatmqtt.mqttmodel.ConfigJson;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 ConfNgPlayer クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdConfNgPlayer extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdConfNgPlayer instance = null;
    public static final AsyncChatTaskCmdConfNgPlayer getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdConfNgPlayer(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdConfNgPlayer(PluginFrame plg_) {
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
        EcoChatDBUserNgConf usngconfdb = (EcoChatDBUserNgConf) plg.getDB("user_ng_conf");
        UserNgConf usngconf = conf.getUserNgConf(us.uuid, target.uuid);
        if (usngconf == null) {
            usngconfdb.insertUserNgConf(con, us.id, target.id);
            con.commit();
            usngconf = usngconfdb.reloadUserNgConf(con, us.id, target.id);
        }
        
        // 変更する値を判定
        boolean old = usngconf.ng;
        usngconf.ng = !usngconf.ng;
        Utl.sendPluginMessage(plg, data.player, "対プレイヤー({0})NG設定を{1}から{2}に変更しました。", data.param[0], String.valueOf(old), String.valueOf(usngconf.ng));

        // 他サーバに対ユーザーNG設定変更を通知する
        notifyConfigReload(ConfigJson.Type.USER_NG_CONF, us.id, 0, target.id);

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
            Utl.sendPluginMessage(plg, data.player, "対プレイヤーNG設定の変更に失敗しました");
        }
    }

}
