package jp.minecraftuser.ecochatmqtt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelPassword;
import jp.minecraftuser.ecoframework.DatabaseFrame;

/**
 * ChannelPassword関連DB操作クラス
 * @author ecolight
 */
public class EcoChatDBChannelPassword extends DatabaseFrame {

    /**
     * コンストラクタ
     * @param frame_ EcoChatDBインスタンス 
     * @param name_ 一覧登録名
     */
    public EcoChatDBChannelPassword(DatabaseFrame frame_, String name_) {
        super(frame_, name_);
    }

    /**
     * 全チャンネルパスワード情報取得(設定があるチャンネルのみ)
     * @param con コネクション
     * @return 全チャンネルパスワードリスト
     * @throws SQLException SQL異常
     */
    public ConcurrentHashMap<Integer, ChannelPassword> loadAllChanelPassword(Connection con) throws SQLException {
        ConcurrentHashMap<Integer, ChannelPassword> ret = new ConcurrentHashMap<>();
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM CHPASS;");
        rs = prep.executeQuery();
        while (rs.next()) {
            ChannelPassword chpass = new ChannelPassword(
                    rs.getInt("ID"),
                    rs.getString("PASS")
            );
            ret.put(chpass.id, chpass);
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * 単体チャンネルパスワード情報取得
     * @param con コネクション
     * @param channelid 取得するチャンネルのID
     * @return チャンネルパスワード情報
     * @throws SQLException SQL異常
     */
    public ChannelPassword reloadChannelPassword(Connection con, int channelid) throws SQLException {
        ChannelPassword ret = null;
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM CHPASS WHERE ID = ?;");
        prep.setInt(1, channelid);
        rs = prep.executeQuery();
        while (rs.next()) {
            ret = new ChannelPassword(
                    rs.getInt("ID"),
                    rs.getString("PASS")
            );
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * チャンネルパスワード作成
     * @param con コネクション
     * @param channelid
     * @param hash
     * @throws java.sql.SQLException
     */
    public void insertChannelPassword(Connection con, int channelid, String hash) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("INSERT INTO CHPASS (ID, PASS) VALUES (?, ?)");
        prep.setInt(1, channelid);
        prep.setString(2, hash);
        prep.executeUpdate();
        prep.close();
    }
    
    /**
     * チャンネルパスワード更新
     * @param con コネクション
     * @param chpass
     * @throws SQLException SQL異常
     */
    public void updateChannelPassword(Connection con, ChannelPassword chpass) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("UPDATE CHPASS SET PASS = ? WHERE ID = ?");
        prep.setString(1, chpass.pass);
        prep.setInt(2, chpass.id);
        prep.executeUpdate();
        prep.close();
    }
}
