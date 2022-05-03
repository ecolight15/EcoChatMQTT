
package jp.minecraftuser.ecochatmqtt.dbmodel;

/**
 *
 * @author ecolight
 */
public class ChannelPassword {
    public int id; // プライマリキー
    public String pass; // パスワード
    
    public ChannelPassword (
            int id_,
            String pass_
    ) {
        id = id_;
        pass = pass_;
    }
}
