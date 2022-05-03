
package jp.minecraftuser.ecochatmqtt.mqttmodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * ログイン・ログアウト情報(Player) Jsonモデル
 * @author ecolight
 */
public class PlayerJson {
    @SerializedName("uuid")
    @Expose
    public UUID uuid;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("world")
    @Expose
    public String world;
    @SerializedName("x")
    @Expose
    public double x;
    @SerializedName("y")
    @Expose
    public double y;
    @SerializedName("z")
    @Expose
    public double z;

    /**
     * コンストラクタ
     * @param p プレイヤーインスタンス
     */
    PlayerJson(Player p) {
        uuid = p.getUniqueId();
        name = p.getName();
        world = p.getWorld().getName();
        Location loc = p.getLocation();
        x = loc.getX();
        y = loc.getY();
        z = loc.getZ();
    }
    
    /**
     * null チェック
     */
    public void check() {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(name);
        Objects.requireNonNull(world);
        Objects.requireNonNull(x);
        Objects.requireNonNull(y);
        Objects.requireNonNull(z);
    }

}
