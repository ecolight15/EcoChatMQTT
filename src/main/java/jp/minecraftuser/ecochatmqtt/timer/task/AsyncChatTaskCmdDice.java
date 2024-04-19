
package jp.minecraftuser.ecochatmqtt.timer.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 Dice クラス
 * @author ecolight
 */
public class AsyncChatTaskCmdDice extends AsyncChatTaskBase {
    
    // シングルトン実装
    private static AsyncChatTaskCmdDice instance = null;
    public static final AsyncChatTaskCmdDice getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskCmdDice(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskCmdDice(PluginFrame plg_) {
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
        User us = conf.getUser((Player) data.player);
        Channel act = conf.getChannel(us.activeChannel);
        if (act == null) {
            Utl.sendPluginMessage(plg, data.player, "アクティブチャンネルが存在しません");
            data.result = false;
            return;
        }
        Random r = new Random();
        if (data.param.length == 0) {
            // ﾊﾟﾗﾒﾀなし
            String str = String.format("%s が 6 面サイコロを転がしました。コロコロ...[%d]", data.player.getName(), r.nextInt(6)+1);
            act.sendChannelBroadcast(plg, data.player, str);
        } else {
            // ﾊﾟﾗﾒﾀはサイコロの母数
            int i = Integer.parseInt(data.param[0]);
            if (i < 1) {
                Utl.sendPluginMessage(plg, data.player, "サイコロの目の指定が小さすぎます");
                data.result = false;
                return;
                }
            String str = String.format("%s が %d 面サイコロを転がしました。コロコロ...[%d]", data.player.getName(), i, r.nextInt(i)+1);
            act.sendChannelBroadcast(plg, data.player, str);
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
