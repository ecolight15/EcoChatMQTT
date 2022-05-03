
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannel;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.mqttmodel.ConfigJson;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 ChannelDef クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdChannelDef extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdChannelDef instance = null;
    public static final AsyncChatTaskCmdChannelDef getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdChannelDef(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdChannelDef(PluginFrame plg_) {
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
        if (!data.player.isOp()) { // OPのみ
            Utl.sendPluginMessage(plg, data.player, "アクティブチャンネルの設定を変更する権限がありません");
            data.result = false;
            return;
        }
        
        // 変更する値を判定
        boolean old = ch.autoJoin;
        ch.autoJoin = !ch.autoJoin;
        EcoChatDBChannel chdb = (EcoChatDBChannel) plg.getDB("channel");
        chdb.updateChannel(con, ch);
        Utl.sendPluginMessage(plg, data.player, "チャンネルの[def]指定を{0}から{1}に変更しました。", String.valueOf(old), String.valueOf(ch.autoJoin));

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
