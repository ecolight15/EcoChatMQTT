package jp.minecraftuser.ecochatmqtt.receiver;

import java.util.logging.Level;
import jp.minecraftuser.ecochatmqtt.EcoChatMQTT;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecomqtt.io.MQTTController;
import jp.minecraftuser.ecomqtt.io.MQTTReceiver;

/**
 * MQTTサブスクライブ受信ハンドラ/パブリッシュ制御クラス
 * @author ecolight
 */
public class ConfigReceiver extends MQTTController implements MQTTReceiver {
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     */
    public ConfigReceiver(EcoChatMQTT plg_) {
        super(plg_);
    }

    /**
     * コマンド受信登録ハンドラ
     * @param topic 受信トピック
     * @param payload 受信電文
     */
    @Override
    public void handler(String topic, byte[] payload) {
        plg.getLogger().log(Level.INFO, "topic[{0}] payload[{1}]", new Object[]{topic, new String(payload)});
        EcoChatMQTT plugin = (EcoChatMQTT) plg;
        AsyncWorker worker = (AsyncWorker) plugin.getPluginTimer("worker");
        ChatPayload data = new ChatPayload(plugin, ChatPayload.Type.CONFIG_RECEIVE);
        data.message = new String(payload);
        worker.sendData(data);
    }
}
