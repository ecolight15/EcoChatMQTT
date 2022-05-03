
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import jp.minecraftuser.ecochatmqtt.commands.LeaveCommand;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannel;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 Leave クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdLeave extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdLeave instance = null;
    public static final AsyncChatTaskCmdLeave getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdLeave(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdLeave(PluginFrame plg_) {
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
        ArrayList<Channel> list;
        User us = conf.getUser((Player) data.player);
        
        // 第一パラメタがALLの場合はすべてを対象、それ以外はパラメタごとにチャンネル名と判断して離脱する。
        if (data.param[0].equalsIgnoreCase("ALL")) {
            if (data.confirm == false) {
                // 全チャンネル指定なのでconfirmで確認する
                Utl.sendPluginMessage(plg, data.player, "全てのチャンネルから離脱しますがよろしいですか？");
                data.execute_confirm = true;
                data.result = true;
                return;
            } else {
                // 全ての参加チャンネルを対象とする
                // プレイヤーのチャンネルは読み込み済みなのでChannelUserから取得する
                // プレイヤーの参加チャンネル
                ConcurrentHashMap<String, ChannelUser> map = conf.getChannelUser((Player) data.player);
                list = new ArrayList<>();
                for (ChannelUser chus : map.values()) {
                    // チャンネルは必ず存在しているものとして処理する
                    list.add(conf.getChannel(chus.id));
                }
            }
            data.confirm = false;
        } else {
            // ALL指定でないのでチャンネルを検索してリストアップする
            list = new ArrayList<>();
            for (String s : data.param) {
                Channel ch = conf.getChannel(s);
                if (ch != null) {
                    list.add(ch);
                } else {
                    Utl.sendPluginMessage(plg, data.player, "指定したチャンネル[{0}]は存在しません", s);
                }
            }
        }

        // 離脱チャンネル件数は0件
        if (list.isEmpty()) {
            Utl.sendPluginMessage(plg, data.player, "離脱件数は 0 でした");
            return;
        }
        
        // 離脱チャンネル一覧作成完了
        // 離脱チャンネルごとに所属と人数をチェック（全件のChannelUserは保持していないのでDBに問い合わせ）
        EcoChatDBChannelUser chusdb = (EcoChatDBChannelUser) plg.getDB("channel_user");
        EcoChatDBChannel chdb = (EcoChatDBChannel) plg.getDB("channel");
        ArrayList<String> suspend = new ArrayList<>();
        for (Channel ch : list) {
            // 所属チェック
            if (!conf.isExistChannelUserByID(us.id, ch.id)) {
                Utl.sendPluginMessage(plg, data.player, "指定したチャンネル[{0}]に所属していません", ch.tag);
                continue;
            }
            // 人数チェック
            if (chusdb.countChannelUser(con, ch.id) <= 1) {
                if (data.confirm) {
                    // confirm済みなので削除を実施する(関連テーブルのレコード削除は DELETE CASCADE で実施される)
                    // チャンネルの削除
                    chdb.deleteChannel(con, ch.id);
                    con.commit();

                    // 削除によって更新されているテーブル
                    // Channel
                    // ChannelConf
                    // ChannelPassword
                    // ChannelUser
                    // UserChannelConf
                    // アクティブは自分で変更する
                    conf.deleteChannel(ch);
                    conf.deleteChannelConf(ch);
                    conf.deleteChannelPassword(ch);
                    conf.deleteChannelUser_Channel(ch, us);
                    conf.deleteUserChannelConf(us, ch);

                    Utl.sendPluginMessage(plg, data.player, "チャンネル[{0}]を削除しました", ch.tag);
                } else {
                    // 削除されるとみられるのでconfirmで確認する(後回し)
                    suspend.add(ch.tag);
                    continue;
                }
            }
            // 離脱可能
            // DBからそれぞれ削除
            chusdb.deleteChannelUser(con, ch.id, us.id);
            con.commit();
            conf.deleteChannelUser_User(ch, us);
            log.log(Level.INFO, "Remove channel[{0}] user({1}) data", new Object[]{ch.tag, data.player.getName()});
            Utl.sendPluginMessage(plg, data.player, "指定したチャンネル[{0}]から離脱しました", ch.tag);
        }

        // 削除保留中のチャンネルがあれば確認する
        if (!suspend.isEmpty()) {
            // 全チャンネル指定なのでconfirmで確認する
            Utl.sendPluginMessage(plg, data.player, "以下のチャンネルは所属が1名のため削除されますが処理を継続しますか？");
            StringBuilder sb = new StringBuilder();
            for (String s : suspend) {
                if (sb.length() > 0) sb.append(" ");
                sb.append("[");
                sb.append(s);
                sb.append("]");
            }
            Utl.sendPluginMessage(plg, data.player, sb.toString());
            // パラメータは再編集する(削除チャンネルだけで再度Leaveを呼び出させる)
            data.param = suspend.toArray(new String[0]);
            data.execute_confirm = true;
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
        if (data.result) {
            if (data.execute_confirm) {
                LeaveCommand cmd = (LeaveCommand) plg.getPluginCommand("leave");
                cmd.call_confirm(data.player, data.param);
            }
        }
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
