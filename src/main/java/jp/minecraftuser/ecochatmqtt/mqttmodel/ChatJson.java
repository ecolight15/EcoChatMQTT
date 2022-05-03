
package jp.minecraftuser.ecochatmqtt.mqttmodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * チャット情報 Jsonモデル
 * @author ecolight
 */
public class ChatJson {
    @SerializedName("channel")
    @Expose
    public String channel;
    @SerializedName("player")
    @Expose
    public PlayerJson player;
    @SerializedName("msg")
    @Expose
    public String msg;
    @SerializedName("date")
    @Expose
    public String date;
    @SerializedName("broadcast")
    @Expose
    public boolean broadcast;
    @SerializedName("broadcast_target")
    @Expose
    public boolean broadcast_target;
    @SerializedName("target_uuid")
    @Expose
    public UUID target_uuid;
    @SerializedName("url")
    @Expose
    public String url;

    /**
     * コンストラクタ
     * @param channel_ チャンネル
     * @param p プレイヤーインスタンス
     * @param msg_ メッセージ
     * @param date_ 発生時刻
     * @param broadcast_ ブロードキャスト指定
     * @param broadcast_target_ 個人宛てブロードキャスト指定
     * @param target_uuid_ 個人宛てブロードキャストの送信先UUID
     * @param url_ 付帯URL(Lambda等でWebhook送信等で利用可能)
     */
    public ChatJson(String channel_, Player p, String msg_, String date_, boolean broadcast_, boolean broadcast_target_, UUID target_uuid_, String url_) {
        channel = channel_;
        player = new PlayerJson(p);
        msg = msg_;
        date = date_;
        broadcast = broadcast_;
        broadcast_target = broadcast_target_;
        target_uuid = target_uuid_;
        url = url_;
    }
    
    /**
     * null チェック
     */
    public void check() {
        Objects.requireNonNull(channel);
        Objects.requireNonNull(player);
        player.check();
        Objects.requireNonNull(msg);
        Objects.requireNonNull(date);
        Objects.requireNonNull(broadcast);
        Objects.requireNonNull(broadcast_target);
        Objects.requireNonNull(url);
    }
}
