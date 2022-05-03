
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannel;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.mqttmodel.ConfigJson;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 ChannelJoin クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdChannelWelcome extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdChannelWelcome instance = null;
    public static final AsyncChatTaskCmdChannelWelcome getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdChannelWelcome(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdChannelWelcome(PluginFrame plg_) {
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
        
        // 変更する値を判定
        String old = ch.welcomeMessage;
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : data.param) {
            if (!first) sb.append(" ");
            else first = false;
            sb.append(s);
        }
        ch.welcomeMessage = sb.toString();
        EcoChatDBChannel chdb = (EcoChatDBChannel) plg.getDB("channel");
        chdb.updateChannel(con, ch);
        Utl.sendPluginMessage(plg, data.player, "チャンネルの[welcomeMessage]指定を{0}から{1}に変更しました。", old, ch.welcomeMessage);

        // 他サーバにチャンネル変更を通知する
        notifyConfigReload(ConfigJson.Type.CHANNEL, 0, ch.id, 0);

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
