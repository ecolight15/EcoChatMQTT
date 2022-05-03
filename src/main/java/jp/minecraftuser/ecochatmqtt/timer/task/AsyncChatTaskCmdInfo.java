
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import jp.minecraftuser.ecochatmqtt.EcoChatMQTT;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 Info クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdInfo extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdInfo instance = null;
    public static final AsyncChatTaskCmdInfo getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdInfo(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdInfo(PluginFrame plg_) {
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
        
        // チャンネル設定取得
        ChannelConf chconf = conf.getChannelConf(ch.tag);
        
        // 必要な情報の収集
        boolean isPass = conf.isExistChannelPassword(ch.id);                            // パスワード有無
        Date date = new Date(ch.since);                                                 // 作成日
        EcoChatDBChannelUser chusdb = (EcoChatDBChannelUser) plg.getDB("channel_user"); // チャンネル管理者グループ
        ConcurrentHashMap<Integer, ChannelUser> owners = chusdb.loadOwnerChannelUsers(con, ch.id);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (ChannelUser owner : owners.values()) {
            if (!first) sb.append(",");
            else first = false;
            String name = null;
            if (((EcoChatMQTT) plg).slog == null) {
                if (((EcoChatMQTT) plg).slog != null) {
                    sb.append(((EcoChatMQTT) plg).slog.latestName(conf.getUser(owner.userid).uuid));
                }
            }
        }
        
        // 出力
        ch.sendChannelMessage(plg, data.player, "============ ["+ch.tag+":"+ch.name+"]チャンネル情報 =============");
        ch.sendChannelMessage(plg, data.player, "管理者グループ:");
        SimpleDateFormat sdf = new SimpleDateFormat("[YYYY-MM-dd HH:mm:ss] ");
        ch.sendChannelMessage(plg, data.player, "作成日:"+sdf.format(date));
        ch.sendChannelMessage(plg, data.player, "チャットタイプ:"+ch.type.getName());
        ch.sendChannelMessage(plg, data.player, "デフォルト加入:"+String.valueOf(ch.autoJoin));
        ch.sendChannelMessage(plg, data.player, "リスト表示:"+String.valueOf(ch.listed));
        ch.sendChannelMessage(plg, data.player, "パスワード:"+String.valueOf(isPass));
        ch.sendChannelMessage(plg, data.player, "参加時アナウンス:"+ch.enterMessage);
        ch.sendChannelMessage(plg, data.player, "離脱時アナウンス:"+ch.leaveMessage);
        ch.sendChannelMessage(plg, data.player, "参加時メッセージ:"+ch.welcomeMessage);
        ch.sendChannelMessage(plg, data.player, "離脱時メッセージ:"+ch.goodbyeMessage);
        
        if (chconf != null) {
            ch.sendChannelMessage(plg, data.player, "チャンネルカラー:"+chconf.getColor()+chconf.color);
            ch.sendChannelMessage(plg, data.player, "太字:"+String.valueOf(chconf.bold)+" 斜体:"+String.valueOf(chconf.italic)+" 下線:"+String.valueOf(chconf.line)+" 抹消線:"+String.valueOf(chconf.strike));
        }
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
