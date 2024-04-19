
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import jp.minecraftuser.ecochatmqtt.EcoChatMQTT;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import jp.minecraftuser.ecomqttserverlog.EcoMQTTServerLog;
import net.md_5.bungee.api.ChatColor;

/**
 * タスク別処理分割用 Who クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdWho extends AsyncChatTaskBase {

    // シングルトン実装
    private static AsyncChatTaskCmdWho instance = null;
    public static final AsyncChatTaskCmdWho getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdWho(plg_);
        }
        return instance;
    }

    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdWho(PluginFrame plg_) {
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
        Channel ch;
        if (data.param.length == 0) {
            User us = conf.getUser((Player) data.player);
            ch = conf.getChannel(us.activeChannel);
            if (ch == null) {
                Utl.sendPluginMessage(plg, data.player, "アクティブチャンネルが存在しません");
                data.result = false;
                return;
            }
        } else {
            ch = conf.getChannel(data.param[0]);
            if (ch == null) {
                Utl.sendPluginMessage(plg, data.player, "指定したチャンネル[{0}]が存在しません", data.param[0]);
                data.result = false;
                return;
            }
        }
        
        // 必要な情報の収集
        EcoChatDBChannelUser chusdb = (EcoChatDBChannelUser) plg.getDB("channel_user"); // チャンネル管理者グループ
        ConcurrentHashMap<Integer, ChannelUser> users = chusdb.loadChannelUsers(con, ch.id);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (ChannelUser user : users.values()) {
            if (!first) sb.append(",");
            else first = false;
            if (((EcoChatMQTT) plg).slog == null) {
                ((EcoChatMQTT) plg).slog = (EcoMQTTServerLog) plg.getPluginFrame("EcoMQTTServerLog");
            }
            if (((EcoChatMQTT) plg).slog != null) {
                UUID uuid = conf.getUser(user.userid).uuid;
                if (((EcoChatMQTT) plg).slog.onlinePlayers.containsKey(uuid)) {
                    // online
                    if (user.owner) {
                        sb.append(ChatColor.AQUA);
                    } else {
                        sb.append(ChatColor.WHITE);
                    }
                    sb.append(((EcoChatMQTT) plg).slog.latestName(uuid));
                    sb.append(ChatColor.WHITE);
                } else {
                    // offline
                    if (user.owner) {
                        sb.append(ChatColor.BLUE);
                    } else {
                        sb.append(ChatColor.GRAY);
                    }
                    sb.append(((EcoChatMQTT) plg).slog.latestName(uuid));
                    sb.append(ChatColor.WHITE);
                }
            }
        }

        // 出力
        ch.sendChannelMessage(plg, data.player, "=== ["+ch.tag+":"+ch.name+"]チャンネルユーザー情報 ===");
        ch.sendChannelMessage(plg, data.player, sb.toString());
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
//        // プレイヤーが現時点でオンラインであれば結果送信する。
//        if (!data.player.isOnline()) return;
//        if (data.result) {
//            ((SaveInventoryListener) plg.getPluginListener("inventory")).updatePlayerServer(data.player.getUniqueId(), data.serverName);
//            Utl.sendPluginMessage(plg, data.player, "サーバー間データ共有モードの有効化に成功しました");
//        } else {
//            Utl.sendPluginMessage(plg, data.player, "サーバー間データ共有モードの有効化に失敗しました");
//            if (data.msg != null) {
//                Utl.sendPluginMessage(plg, data.player, data.msg);
//            }
//        }
    }

}
