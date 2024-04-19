
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecochatmqtt.config.EcoChatMQTTDefaultChannelConfig;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannel;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelUser;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 Load クラス
 * @author ecolight
 */
public class AsyncChatTaskLoad extends AsyncChatTaskBase {

    // シングルトン実装
    private static AsyncChatTaskLoad instance = null;
    public static final AsyncChatTaskLoad getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskLoad(plg_);
        }
        return instance;
    }

    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskLoad(PluginFrame plg_) {
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
        log.log(Level.INFO, "Start loading user({0})", data.player.getName());
        // プレイヤーJOIN時の処理
        // アクティブチャンネルが存在しなかったら異常
        EcoChatMQTTDefaultChannelConfig defcnf = (EcoChatMQTTDefaultChannelConfig) plg.getPluginConfig("defch");
        String active = defcnf.getString("DefaultActive");
        if (!conf.isExistChannel(active)) {
            data.result = false;
            data.message = "設定が不正です。サーバー管理者に連絡してください。";
            return;
        }

        // プレイヤーのUser情報作成判定、およびChannelUserデータをロードする
        try {
            log.log(Level.INFO, "Start loading database ({0})", data.player.getName());
            EcoChatDBChannel chdb = (EcoChatDBChannel) plg.getDB("channel");
            EcoChatDBChannelUser chusdb = (EcoChatDBChannelUser) plg.getDB("channel_user");
            EcoChatDBUser usdb = (EcoChatDBUser) plg.getDB("user");
            Player pl = (Player) data.player;

            // User データ未生成なら生成する
            if (!conf.isExistUser(pl)) {
                log.log(Level.INFO, "Create new user data. player[{0}] active[{1}]", new Object[]{data.player.getName(), conf.getChannel(active).tag});
                // ユーザ情報を追加
                try {
                    // 既に情報がある場合は、データの重複でcatchへ飛ぶ。事前チェックは省略。
                    usdb.insertUser(con, pl, conf.getChannel(active).id);
                    con.commit();
 
                    // DBからユーザ情報を読み直す
                    log.log(Level.INFO, "Reload new user data.");
                    data.user = usdb.reloadUser(con, pl.getUniqueId());

                    // 初期チャンネルへの加入
                    log.log(Level.INFO, "Join new user default channels.");
                    ConcurrentHashMap<Integer, Channel> auto = chdb.loadAutoJoinChanels(con);
                    for (int chid : auto.keySet()) {
                        chusdb.insertChannelUser(con, chid, data.user.id);
                    }
                } catch (SQLException ex) {
                    // 重複以外で異常となる可能性もあるため念のためログ
                    log.log(Level.INFO, "Skip create new user data. cause...");
                    Logger.getLogger(AsyncChatTaskLoad.class.getName()).log(Level.INFO, null, ex);

                    data.user = usdb.reloadUser(con, pl.getUniqueId());
                }
            } else {
                data.user = usdb.reloadUser(con, pl.getUniqueId());
            }

            // チャンネルユーザ情報を再読み込み
            log.log(Level.INFO, "Loading channel user data({0})", data.player.getName());
            data.chuslist = chusdb.loadChannelUser(con, data.user.id);
            
            // 正常なのでユーザ情報、チャンネルユーザ情報を設定に更新かける
            if (data.user != null) {
                conf.updateUser(data.user);
                log.log(Level.INFO, "First User data created. [{0}]", data.player.getName());
            }
            if (data.chuslist != null) {
                conf.updateChannelUser((Player) data.player, data.chuslist);
                log.log(Level.INFO, "ChannelUser data loaded. [{0}]", data.player.getName());
            }

            data.result = true;
        } catch (SQLException ex) {
            Logger.getLogger(AsyncChatTaskLoad.class.getName()).log(Level.SEVERE, null, ex);
            data.result = false;
        }
    }

    /**
     * 応答後メインスレッド側で実施する処理
     * Bukkit/Spigotインスタンス直接操作可
     * @param thread
     * @param data 
     */
    @Override
    public void mainThread(AsyncWorker thread, ChatPayload data) {
        Utl.sendPluginMessage(plg, data.player, "ログイン時ロード処理");

        if (!data.result) {
            Utl.sendPluginMessage(plg, data.player, "Err:{0}", data.message);
            Utl.sendPluginMessage(plg, data.player, "チャット機能へのデータ追加に失敗しました。管理人に時間と合わせて報告をお願いします。");
            // Todo ★この状態で発言した場合の振る舞いをチャット送信処理で考慮すること
        } else {
            Utl.sendPluginMessage(plg, data.player, "チャット機能へ接続済み");
        }
    }

}
