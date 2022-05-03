
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
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
 * タスク別処理分割用 Delete クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdDelete extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdDelete instance = null;
    public static final AsyncChatTaskCmdDelete getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdDelete(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdDelete(PluginFrame plg_) {
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
        
        // 第一パラメタがALLの場合はすべてを対象、それ以外はパラメタごとにチャンネル名と判断して削除する。
        if (data.param[0].equalsIgnoreCase("ALL")) {
            // 全ての参加チャンネルを対象とする
            // プレイヤーのチャンネルは読み込み済みなのでChannelUserから取得する
            // プレイヤーの参加チャンネル
            ConcurrentHashMap<String, ChannelUser> map = conf.getChannelUser((Player) data.player);
            list = new ArrayList<>();
            for (ChannelUser chus : map.values()) {
                // チャンネルは必ず存在しているものとして処理する
                list.add(conf.getChannel(chus.id));
            }
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

        // 削除チャンネル件数は0件
        if (list.isEmpty()) {
            Utl.sendPluginMessage(plg, data.player, "削除件数は 0 でした");
            return;
        }
        
        // 離脱チャンネル一覧作成完了
        // 離脱チャンネルごとに所属と人数をチェック（全件のChannelUserは保持していないのでDBに問い合わせ）
        EcoChatDBChannelUser chusdb = (EcoChatDBChannelUser) plg.getDB("channel_user");
        EcoChatDBChannel chdb = (EcoChatDBChannel) plg.getDB("channel");
        for (Channel ch : list) {
            // 所属チェック
            if (!conf.isExistChannelUserByID(us.id, ch.id)) {
                Utl.sendPluginMessage(plg, data.player, "指定したチャンネル[{0}]に所属していません", ch.tag);
                continue;
            }
            // Ownerチェック
            if ((!data.player.isOp()) && (!conf.getChannelUser((Player) data.player, ch.tag).owner)) {
                Utl.sendPluginMessage(plg, data.player, "指定したチャンネル[{0}]の管理権限がありません", ch.tag);
                continue;
            }
            // confirm済みなので削除を実施する(離脱はDBの DELETE CASCADE で実施される)
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

    }

}
