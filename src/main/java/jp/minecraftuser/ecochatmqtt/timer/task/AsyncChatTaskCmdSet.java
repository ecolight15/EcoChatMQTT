
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUser;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 Set クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdSet extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdSet instance = null;
    public static final AsyncChatTaskCmdSet getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdSet(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdSet(PluginFrame plg_) {
        super(plg_);
    }

    /**
     * 非同期で実施する処理
     * Bukkit/Spigotインスタンス直接操作不可
     * @param thread
     * @param db
     * @param con
     * @param data 
     */
    @Override
    public void asyncThread(AsyncWorker thread, EcoChatDB db, Connection con, ChatPayload data) throws SQLException {
        // チャンネルの存在チェック
        if (!conf.isExistChannel(data.param[0])) {
            data.message = "指定したチャンネルは存在しません。";
            data.result = false;
            log.info("set channel not found.");
            return;
        }
            log.info("set channel found.");
        
        // チャンネルへの参加状況を確認
        if (!conf.isExistChannelUser((Player) data.player, data.param[0])) {
            data.message = "指定したチャンネルに参加していません。";
            data.result = false;
            return;
        }
        
        // cc channelTAG または /cc set で実行される
        Player pl = (Player) data.player;
        EcoChatDBUser usdb = (EcoChatDBUser) plg.getDB("user");
        usdb.updateUserActive(con, pl.getUniqueId(), conf.getChannel(data.param[0]).id);
        // 設定を読み直す ※activeチャンネル情報はログインサーバのみ知っていれば良いので通知不要
        conf.updateUser(usdb.reloadUser(con, pl.getUniqueId()));
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
        // コマンドの再実行
        log.info("set result : " + data.result);
        if (data.result) {
            Utl.sendPluginMessage(plg, data.player, "アクティブチャンネルを変更しました。");
        } else {
            if (data.message.length() != 0) {
                Utl.sendPluginMessage(plg, data.player, data.message);
            }
            Utl.sendPluginMessage(plg, data.player, "アクティブチャンネルの変更に失敗しました。");
        }
    }

    

}
