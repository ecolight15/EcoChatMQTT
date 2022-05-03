
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelPassword;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelPassword;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.mqttmodel.ConfigJson;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 PassEnable クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdChannelPassEnable extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdChannelPassEnable instance = null;
    public static final AsyncChatTaskCmdChannelPassEnable getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdChannelPassEnable(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdChannelPassEnable(PluginFrame plg_) {
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
        EcoChatDBChannelPassword chpassdb = (EcoChatDBChannelPassword) plg.getDB("channel_password");
        ChannelPassword chpass = conf.getChannelPassword(ch.tag);
        if (chpass == null) {
            chpassdb.insertChannelPassword(con, ch.id, "");
            con.commit();
            chpass = chpassdb.reloadChannelPassword(con, ch.id);
        }

        String old = chpass.pass;
        if (data.param.length == 0) {
            // 削除指定
            chpass.pass = "";
        } else {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String s : data.param) {
                if (!first) sb.append(" ");
                else first = false;
                sb.append(s);
            }
            try {
                MessageDigest sha3_512 = MessageDigest.getInstance("SHA3-512");
                byte[] sha3_512_result = sha3_512.digest(sb.toString().getBytes());
                chpass.pass = String.format("%040x", new BigInteger(1, sha3_512_result));
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(AsyncChatTaskCmdChannelPassEnable.class.getName()).log(Level.SEVERE, null, ex);
                Utl.sendPluginMessage(plg, data.player, "パスワードのハッシュ変換に失敗しました");
                data.result = false;
                return;
            }
        }
        
        // 変更する値を判定
        chpassdb.updateChannelPassword(con, chpass);
        Utl.sendPluginMessage(plg, data.player, "チャンネルのパスワード指定を{0}から{1}に変更しました。", old, chpass.pass);

        // 他サーバにチャンネルパスワード変更を通知する
        notifyConfigReload(ConfigJson.Type.CHANNEL_PASSWORD, 0, ch.id, 0);

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
            Utl.sendPluginMessage(plg, data.player, "チャンネルの設定変更に失敗しました");
        }
    }

}
