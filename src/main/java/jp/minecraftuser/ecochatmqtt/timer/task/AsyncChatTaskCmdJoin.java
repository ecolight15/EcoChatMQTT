
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannel;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelUser;
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
 * タスク別処理分割用 Join クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdJoin extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdJoin instance = null;
    public static final AsyncChatTaskCmdJoin getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdJoin(plg_);
        }
        return instance;
        
        
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdJoin(PluginFrame plg_) {
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
        // パラメタに入っているチャンネル全てにjoinする
        EcoChatDBChannel chdb = (EcoChatDBChannel) plg.getDB("channel");
        EcoChatDBChannelUser chusdb = (EcoChatDBChannelUser) plg.getDB("channel_user");
        User us = conf.getUser((Player) data.player);
        for (String tag : data.param) {
            Channel ch;
            // すでに加入していたら何もしない
            if (conf.isExistChannelUser((Player) data.player, tag)) {
                ch = conf.getChannel(tag);
                ch.sendChannelMessage(plg, data.player, "すでに所属済みです");
                continue;
            }
            
            // チャンネルに加入する
            if (!conf.isExistChannel(tag)) {
                // 禁止チャンネルタグ名を判定
                if (Channel.checkIgnoreTagName(tag)) {
                    Utl.sendPluginMessage(plg, data.player, "指定チャンネルタグは作成が禁止されています[" + tag + "]");
                    continue;
                }

                // ないので作成する
                chdb.insertChannel(con, tag);
                //con.commit();
                
                // コンフィグにチャンネル情報を追加(MQTT経由でも通知が来ることになるが即応性のため自分でも追加する)
                ch = chdb.reloadChanel(con, tag);
                conf.updateChannel(ch);

                // 他サーバにチャンネルの作成情報を通知する(チャンネルユーザー情報は送信不要)
                notifyConfigReload(ConfigJson.Type.CHANNEL, 0, ch.id, 0);
                
                // チャンネルにオーナーとして加入する
                chusdb.insertOwnerChannelUser(con, ch.id, us.id);
            } else {
                // あったら取得する
                ch = conf.getChannel(tag);
                
                // パスワード指定が存在するかチェック
                ChannelPassword password = conf.getChannelPassword(ch.tag);
                if ((password != null) && (!password.pass.isEmpty())) { // エントリ自体なし or パスワード長 = 0 ならjoin可
                    Utl.sendPluginMessage(plg, data.player, "指定チャンネルはパスワードが設定されているためpassjoinで加入する必要があります[" + tag + "]");
                    continue;
                }

                // チャンネルに加入する
                chusdb.insertChannelUser(con, ch.id, us.id);
            }
            // Welcomeメッセージの送信
            ch.sendChannelMessage(plg, data.player, ch.getWelcomeMessage(data.player));
            
            // チャンネルの他のメンバーにブロードキャスト通知
            ch.sendChannelBroadcast(plg, data.player, ch.getEnterMessage(data.player));

            // コンフィグのMAPに追加する(ログイン中のサーバーだけ読み込んでいれば良いため通知なし)
            ChannelUser chus = chusdb.reloadChannelUser(con, ch.id, us.id);
            conf.updateChannelUser(chus);
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
        if (!data.result) {
            Utl.sendPluginMessage(plg, data.player, "チャンネルへの参加に失敗しました");
        } 
    }

}
