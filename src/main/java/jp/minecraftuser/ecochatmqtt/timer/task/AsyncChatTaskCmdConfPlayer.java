
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import jp.minecraftuser.ecochatmqtt.EcoChatMQTT;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUserUserConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserUserConf;
import jp.minecraftuser.ecochatmqtt.mqttmodel.ConfigJson;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 ConfPlayer クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdConfPlayer extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdConfPlayer instance = null;
    public static final AsyncChatTaskCmdConfPlayer getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdConfPlayer(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdConfPlayer(PluginFrame plg_) {
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
        
        // ususconfを取得
        EcoChatDBUserUserConf ususconfdb = (EcoChatDBUserUserConf) plg.getDB("user_user_conf");
        UserUserConf ususconf = conf.getUserUserConf(us.uuid, target.uuid);
        if (ususconf == null) {
            ususconfdb.insertUserUserConf(con, us.id, target.id);
            con.commit();
            ususconf = ususconfdb.reloadUserUserConf(con, us.id, target.id);
        }
        
        // 変更する値を判定
        boolean old = false;
        boolean bnew = false;
        String oldCol = "";
        if (data.param[0].equalsIgnoreCase("color")) {
            oldCol = ususconf.color;
            // 変更する値を判定
            ChatColor col = ChatColor.valueOf(data.param[1]);
            if (col == null) {
                Utl.sendPluginMessage(plg, data.player, "対プレイヤー色(個人設定)に指定した文字列が判別できませんでした");
                data.result = false;
                return;
            }
            ususconf.color = col.name();
        } else if (data.param[1].equalsIgnoreCase("bold")) {
            old = ususconf.bold;
            ususconf.bold = !ususconf.bold;
            bnew = ususconf.bold;
        } else if (data.param[1].equalsIgnoreCase("italic")) {
            old = ususconf.italic;
            ususconf.italic = !ususconf.italic;
            bnew = ususconf.italic;
        } else if (data.param[1].equalsIgnoreCase("line")) {
            old = ususconf.line;
            ususconf.line = !ususconf.line;
            bnew = ususconf.line;
        } else if (data.param[1].equalsIgnoreCase("strike")) {
            old = ususconf.strike;
            ususconf.strike = !ususconf.strike;
            bnew = ususconf.strike;
        } else {
            Utl.sendPluginMessage(plg, data.player, "指定対プレイヤー({0})個人設定へのモード指定[{1}]が不正です", data.param[0], data.param[1]);
            data.result = false;
            return;
        }
        ususconfdb.updateUserUserConf(con, ususconf);
        if (data.param[1].equalsIgnoreCase("color")) {
            Utl.sendPluginMessage(plg, data.player, "対プレイヤー({0})個人設定の[{1}]指定を{2}から{3}に変更しました。", data.param[0], data.param[1], oldCol, ususconf.color);
        } else {
            Utl.sendPluginMessage(plg, data.player, "対プレイヤー({0})個人設定の[{1}]指定を{2}から{3}に変更しました。", data.param[0], data.param[1], String.valueOf(old), String.valueOf(bnew));
        }

        // 他サーバにユーザーチャンネル設定変更を通知する
        notifyConfigReload(ConfigJson.Type.USER_USER_CONF, us.id, 0, target.id);

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
            Utl.sendPluginMessage(plg, data.player, "対プレイヤー個人設定の変更に失敗しました");
        }
    }

}
