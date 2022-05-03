
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import jp.minecraftuser.ecochatmqtt.EcoChatMQTT;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelUser;
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
 * タスク別処理分割用 Add クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdAdd extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdAdd instance = null;
    public static final AsyncChatTaskCmdAdd getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdAdd(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdAdd(PluginFrame plg_) {
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
        if ((!data.player.isOp()) && (!chus.owner) && (ch.addPerm)) { // チャンネル管理者じゃないかつ権限が必要
            Utl.sendPluginMessage(plg, data.player, "アクティブチャンネルにユーザを追加する権限がありません");
            data.result = false;
            return;
        }
        // 対象者の所属を確認
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
            Utl.sendPluginMessage(plg, data.player, "指定ユーザのUUID検索に失敗しました");
            data.result = false;
            return;
        }
        User target = conf.getUser(uid);
        if (target == null) {
            Utl.sendPluginMessage(plg, data.player, "指定ユーザのチャンネルチャット情報が見つかりませんでした");
            data.result = false;
            return;
        }
        ChannelUser chus_target = conf.getChannelUser(uid, ch.tag);
        if (chus_target != null) {
            Utl.sendPluginMessage(plg, data.player, "指定ユーザは既に指定チャンネルに参加しています");
            data.result = false;
            return;
        }
        
        // 追加
        EcoChatDBChannelUser chusdb = (EcoChatDBChannelUser) plg.getDB("channel_user");

        // チャンネルに加入する
        chusdb.insertChannelUser(con, ch.id, target.id);

        // 他サーバにチャンネルユーザ変更を通知する
        notifyConfigReload(ConfigJson.Type.CHANNEL_USER, target.id, ch.id, 0);

        // Welcomeメッセージの送信
        ch.sendPlayerBroadcast(plg, target.uuid, ch.getWelcomeMessage(data.player));

        // チャンネルの他のメンバーにブロードキャスト通知
        ch.sendChannelBroadcast(plg, data.player, ch.getEnterMessage(data.player));
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
        if (data.result) {
            Utl.sendPluginMessage(plg, data.player, "指定ユーザの追加に成功しました");
        } else {
            Utl.sendPluginMessage(plg, data.player, "指定ユーザの追加に失敗しました");
        }
    }

}
