
package jp.minecraftuser.ecochatmqtt.mqttmodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.Objects;

/**
 * チャット情報 Jsonモデル
 * @author ecolight
 */
public class ConfigJson {
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("channel")
    @Expose
    public int ch;
    @SerializedName("target")
    @Expose
    public int target;
    @SerializedName("user")
    @Expose
    public int us;

    // 処理種別を追加した場合、AsyncSaveLoadTimer の initTask に処理クラスを登録すること
    public enum Type {
        NONE(1, "NONE"),
        CHANNEL(2, "CHANNEL"),
        CHANNEL_CONF(3, "CHANNEL_CONF"),
        CHANNEL_PASSWORD(4, "CHANNEL_PASSWORD"),
        CHANNEL_USER(5, "CHANNEL_USER"),
        USER(6, "USER"),
        USER_CHANNEL_CONF(7, "USER_CHANNEL_CONF"),
        USER_NG_CONF(8, "USER_NG_CONF"),
        USER_USER_CONF(9, "USER_USER_CONF"),
        ;
        private final int num;
        private final String name;
        private Type(final int num_, final String name_) {
            this.num = num_;
            this.name = name_;
        }
        public int getInt() {
            return this.num;
        }
        public String getName() {
            return this.name;
        }
        public static Type getByNum(int num_) {
            for (Type t : Type.values()) {
                if (t.getInt() == num_) {
                    return t;
                }
            }
            return null;
        }
        public static Type getByName(String name_) {
            for (Type t : Type.values()) {
                if (t.getName().equalsIgnoreCase(name_)) {
                    return t;
                }
            }
            return null;
        }

    }

    /**
     * コンストラクタ
     * @param type_ 種別
     * @param usid_ ユーザーID
     * @param chid_ チャンネルID
     * @param target_ 対ユーザーの相手のID
     */
    public ConfigJson(Type type_, int usid_, int chid_, int target_) {
        type = type_.getName();
        ch = chid_;
        us = usid_;
        target = target_;
    }
    
    /**
     * null チェック
     */
    public void check() {
        Objects.requireNonNull(type);
        Objects.requireNonNull(ch);
        Objects.requireNonNull(us);
    }
}
