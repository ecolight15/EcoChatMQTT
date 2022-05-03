
package jp.minecraftuser.ecochatmqtt.listener;

import com.google.gson.Gson;
import jp.minecraftuser.ecoframework.ListenerFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * ログイン・ログアウトListenerクラス
 * @author ecolight
 */
public class LoginLogoutListener extends ListenerFrame {
    Gson gson = new Gson();
    
    /**
     * コンストラクタ
     * @param plg_ プラグインフレームインスタンス
     * @param name_ 名前
     */
    public LoginLogoutListener(PluginFrame plg_, String name_) {
        super(plg_, name_);
    }

    /**
     * プレイヤーログイン処理
     * プレイヤーのログインをMQTTで通知する
     * 参考：同一プレイヤーが多重ログインしてきた場合のイベント発生順序は次の通り。
     * (A)Kick->(A)Quit->(B)AsyncPreLogin->(B)PreLogin->(B)Login->(B)Join
     * @param event イベント
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        // ログイン時のUserロード　すでに読み込み済みでも読み直す
        AsyncWorker worker = (AsyncWorker) plg.getPluginTimer("worker");
        Player pl = event.getPlayer();
        ChatPayload data = new ChatPayload(plg, pl, ChatPayload.Type.LOAD);
        worker.sendData(data);
    }

}
