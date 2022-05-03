
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
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
 * タスク別処理分割用 Kick クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdKick extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdKick instance = null;
    public static final AsyncChatTaskCmdKick getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdKick(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdKick(PluginFrame plg_) {
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
            Utl.sendPluginMessage(plg, data.player, "アクティブチャンネルからユーザをKICKする権限がありません");
            data.result = false;
            return;
        }
        // 対象者の所属を確認
        UUID uid = null;
        String name = null;
        if (((EcoChatMQTT) plg).slog == null) {
            if (((EcoChatMQTT) plg).slog != null) {
                uid = ((EcoChatMQTT) plg).slog.latestUUID(data.param[0]);
                name = ((EcoChatMQTT) plg).slog.latestName(uid);
            }
        }
        if (uid == null) {
            for (Player p : plg.getServer().getOnlinePlayers()) {
                if (p.getName().equalsIgnoreCase(data.param[0])) {
                    uid = p.getUniqueId();
                    name = p.getName();
                    break;
                }
            }
        }
        if (uid == null || name == null) {
            Utl.sendPluginMessage(plg, data.player, "指定ユーザのUUID/NAME検索に失敗しました");
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
        if (chus_target == null) {
            Utl.sendPluginMessage(plg, data.player, "指定ユーザは指定チャンネルに参加していません");
            data.result = false;
            return;
        }
        // 離脱可能
        // DBからそれぞれ削除
        EcoChatDBChannelUser chusdb = (EcoChatDBChannelUser) plg.getDB("channel_user");
        chusdb.deleteChannelUser(con, ch.id, chus_target.id);
        con.commit();
        conf.deleteChannelUser_User(ch, target);
        log.log(Level.INFO, "Remove channel[{0}] user({1}) data", new Object[]{ch.tag, data.param[0]});
        Utl.sendPluginMessage(plg, data.player, "指定したチャンネル[{0}]からプレイヤー[{1}]をKickしました", ch.tag, data.param[0]);

        // チャンネルの他のメンバーにブロードキャスト通知
        ch.sendChannelBroadcast(plg, data.player, data.player.getName() + " により " + name + "がチャンネルからkickされました");

        // 他サーバにチャンネルユーザ変更を通知する
        notifyConfigReload(ConfigJson.Type.CHANNEL_USER, target.id, ch.id, 0);

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
