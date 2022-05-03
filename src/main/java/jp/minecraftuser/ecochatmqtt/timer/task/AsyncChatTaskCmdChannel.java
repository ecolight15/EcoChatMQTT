
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.mqttmodel.ConfigJson;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 Channel クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdChannel extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdChannel instance = null;
    public static final AsyncChatTaskCmdChannel getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdChannel(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdChannel(PluginFrame plg_) {
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
        Channel ch = conf.getChannel(us.activeChannel);
        if (ch == null) {
            Utl.sendPluginMessage(plg, data.player, "アクティブチャンネルが存在しません");
            data.result = false;
            return;
        }
        // 実行者の権限を確認
        ChannelUser chus = conf.getChannelUser((Player) data.player, ch.tag);
        if ((!data.player.isOp()) && (!chus.owner)) { // チャンネル管理者じゃない
            Utl.sendPluginMessage(plg, data.player, "アクティブチャンネルの設定を変更する権限がありません");
            data.result = false;
            return;
        }
        
        // chconfを取得
        EcoChatDBChannelConf chconfdb = (EcoChatDBChannelConf) plg.getDB("channel_conf");
        ChannelConf chconf = conf.getChannelConf(ch.tag);
        if (chconf == null) {
            chconfdb.insertChannelConf(con, ch.id);
            con.commit();
            chconf = chconfdb.reloadChanelConf(con, ch.id);
        }
        
        // 変更する値を判定
        boolean old = false;
        boolean bnew = false;
        if (data.param[0].equalsIgnoreCase("bold")) {
            old = chconf.bold;
            chconf.bold = !chconf.bold;
            bnew = chconf.bold;
        } else if (data.param[0].equalsIgnoreCase("italic")) {
            old = chconf.italic;
            chconf.italic = !chconf.italic;
            bnew = chconf.italic;
        } else if (data.param[0].equalsIgnoreCase("line")) {
            old = chconf.line;
            chconf.line = !chconf.line;
            bnew = chconf.line;
        } else if (data.param[0].equalsIgnoreCase("strike")) {
            old = chconf.strike;
            chconf.strike = !chconf.strike;
            bnew = chconf.strike;
        }
        chconfdb.updateChannelConf(con, chconf);
        Utl.sendPluginMessage(plg, data.player, "チャンネルタグの[{0}]指定を{1}から{2}に変更しました。", data.param[0], String.valueOf(old), String.valueOf(bnew));

        // 他サーバにチャンネル設定変更を通知する
        notifyConfigReload(ConfigJson.Type.CHANNEL_CONF, 0, ch.id, 0);

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
            Utl.sendPluginMessage(plg, data.player, "チャンネルタグの設定変更に失敗しました");
        }
    }

}
