
package jp.minecraftuser.ecochatmqtt.dbmodel;

/**
 *
 * @author ecolight
 */
public class ChannelUser {
    public int id; // プライマリキー
    public int userid; // UUID
    public boolean owner; // 
    public long joinDate; // 加入時刻
    
    public ChannelUser (
            int id_,
            int userid_,
            boolean owner_,
            long joinDate_
    ) {
        id = id_;
        userid = userid_;
        owner = owner_;
        joinDate = joinDate_;
    }
}
