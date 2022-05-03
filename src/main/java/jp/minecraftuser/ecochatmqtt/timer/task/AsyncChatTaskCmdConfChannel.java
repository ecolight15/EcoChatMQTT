
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUserChannelConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserChannelConf;
import jp.minecraftuser.ecochatmqtt.mqttmodel.ConfigJson;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 ConfChannel クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdConfChannel extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdConfChannel instance = null;
    public static final AsyncChatTaskCmdConfChannel getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdConfChannel(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdConfChannel(PluginFrame plg_) {
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
        // 指定チャンネル
        User us = conf.getUser((Player) data.player);
        Channel ch = conf.getChannel(data.param[0]);
        if (ch == null) {
            Utl.sendPluginMessage(plg, data.player, "指定チャンネル[{0}]が存在しません", data.param[0]);
            data.result = false;
            return;
        }
        
        // uschconfを取得
        EcoChatDBUserChannelConf uschconfdb = (EcoChatDBUserChannelConf) plg.getDB("user_channel_conf");
        UserChannelConf uschconf = conf.getUserChannelConf(us.uuid, ch.tag);
        if (uschconf == null) {
            uschconfdb.insertUserChannelConf(con, us.id, ch.id);
            con.commit();
            uschconf = uschconfdb.reloadUserChannelConf(con, us.id, ch.id);
        }
        
        // 変更する値を判定
        boolean old = false;
        boolean bnew = false;
        String oldCol = "";
        if (data.param[0].equalsIgnoreCase("color")) {
            oldCol = uschconf.color;
            // 変更する値を判定
            ChatColor col = ChatColor.valueOf(data.param[0]);
            if (col == null) {
                Utl.sendPluginMessage(plg, data.player, "チャンネルメッセージ色(個人設定)に指定した文字列が判別できませんでした");
                data.result = false;
                return;
            }
            uschconf.color = col.name();
        } else if (data.param[0].equalsIgnoreCase("bold")) {
            old = uschconf.bold;
            uschconf.bold = !uschconf.bold;
            bnew = uschconf.bold;
        } else if (data.param[0].equalsIgnoreCase("italic")) {
            old = uschconf.italic;
            uschconf.italic = !uschconf.italic;
            bnew = uschconf.italic;
        } else if (data.param[0].equalsIgnoreCase("line")) {
            old = uschconf.line;
            uschconf.line = !uschconf.line;
            bnew = uschconf.line;
        } else if (data.param[0].equalsIgnoreCase("strike")) {
            old = uschconf.strike;
            uschconf.strike = !uschconf.strike;
            bnew = uschconf.strike;
        } else {
            Utl.sendPluginMessage(plg, data.player, "指定チャンネル個人設定へのモード指定[{0}]が不正です", data.param[0]);
            data.result = false;
            return;
        }
        uschconfdb.updateUserChannelConf(con, uschconf);
        if (data.param[0].equalsIgnoreCase("color")) {
            Utl.sendPluginMessage(plg, data.player, "チャンネル個人設定の[{0}]指定を{1}から{2}に変更しました。", data.param[0], oldCol, uschconf.color);
        } else {
            Utl.sendPluginMessage(plg, data.player, "チャンネル個人設定の[{0}]指定を{1}から{2}に変更しました。", data.param[0], String.valueOf(old), String.valueOf(bnew));
        }

        // 他サーバにユーザーチャンネル設定変更を通知する
        notifyConfigReload(ConfigJson.Type.USER_CHANNEL_CONF, us.id, ch.id, 0);

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
            Utl.sendPluginMessage(plg, data.player, "チャンネル個人設定の変更に失敗しました");
        }
    }

}
