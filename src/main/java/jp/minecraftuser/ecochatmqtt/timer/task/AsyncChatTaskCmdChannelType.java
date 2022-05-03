
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
 * タスク別処理分割用 ChannelType クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdChannelType extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdChannelType instance = null;
    public static final AsyncChatTaskCmdChannelType getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdChannelType(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdChannelType(PluginFrame plg_) {
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
        Channel.Type old = ch.type;
        Channel.Type tnew = Channel.Type.getByName(data.param[0]);
        if (tnew == null) {
            Utl.sendPluginMessage(plg, data.player, "指定したチャンネルタイプが判別できませんでした");
            data.result = false;
            return;
        }
        ch.type = tnew;
        
        EcoChatDBChannel chdb = (EcoChatDBChannel) plg.getDB("channel");
        chdb.updateChannel(con, ch);
        Utl.sendPluginMessage(plg, data.player, "チャンネルの[tag]を{0}から{1}に変更しました。", old.getName(), ch.type.getName());

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
