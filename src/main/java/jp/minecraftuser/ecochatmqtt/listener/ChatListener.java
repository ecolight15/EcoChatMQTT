
package jp.minecraftuser.ecochatmqtt.listener;

import jp.minecraftuser.ecochatmqtt.config.EcoChatMQTTConfig;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecoframework.ListenerFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * ログイン・ログアウトListenerクラス
 * @author ecolight
 */
public class ChatListener extends ListenerFrame {
    
    /**
     * コンストラクタ
     * @param plg_ プラグインフレームインスタンス
     * @param name_ 名前
     */
    public ChatListener(PluginFrame plg_, String name_) {
        super(plg_, name_);
    }

    /**
     * チャットイベント処理
     * @param event イベント
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerChat(AsyncPlayerChatEvent event) {
        // チャットのpublishが無効であれば何もしない
        if (!conf.getBoolean("Topic.Chat.Enable")) return;
        
        Player pl = event.getPlayer();
        // 送信先は発言時に決定する(/cc xx message の場合を考慮)
        EcoChatMQTTConfig cnf = (EcoChatMQTTConfig) conf;
        
        // ユーザ情報がないと異常
        User us = cnf.getUser(pl);
        if (us == null) {
            Utl.sendPluginMessage(plg, pl, "チャット機能が無効です");
            event.setCancelled(true);
            return;
        }
        // チャンネル情報がないと異常
        Channel ch = cnf.getChannel(us.activeChannel);
        if (ch == null) {
            Utl.sendPluginMessage(plg, pl, "アクティブチャンネルが存在しません");
            Utl.sendPluginMessage(plg, pl, "setコマンド等でアクティブチャンネルを変更してください");
            event.setCancelled(true);
            return;
        }

        // 送信先チャンネルを設定して送信依頼
        AsyncWorker worker = (AsyncWorker) plg.getPluginTimer("worker");
        ChatPayload data = new ChatPayload(plg, pl, ChatPayload.Type.CHAT);
        data.message = event.getMessage();
        data.channelTag = ch.tag;
        worker.sendData(data);

        // メッセージキャンセル
        event.setCancelled(true);
    }
       

}
