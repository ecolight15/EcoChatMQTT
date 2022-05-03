package jp.minecraftuser.ecochatmqtt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelConf;
import jp.minecraftuser.ecoframework.DatabaseFrame;

/**
 * ChannelConf関連DB操作クラス
 * @author ecolight
 */
public class EcoChatDBChannelConf extends DatabaseFrame {

    /**
     * コンストラクタ
     * @param frame_ EcoChatDBインスタンス 
     * @param name_ 一覧登録名
     */
    public EcoChatDBChannelConf(DatabaseFrame frame_, String name_) {
        super(frame_, name_);
    }

    /**
     * 全チャンネル設定情報取得(設定があるチャンネルのみ)
     * @param con コネクション
     * @return 全チャンネルリスト
     * @throws SQLException SQL異常
     */
    public ConcurrentHashMap<Integer, ChannelConf> loadAllChanelConf(Connection con) throws SQLException {
        ConcurrentHashMap<Integer, ChannelConf> ret = new ConcurrentHashMap<>();
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM CHCONF;");
        rs = prep.executeQuery();
        while (rs.next()) {
            ChannelConf chconf = new ChannelConf(
                    rs.getInt("ID"),
                    rs.getString("COLOR"),
                    rs.getBoolean("BOLD"),
                    rs.getBoolean("ITALIC"),
                    rs.getBoolean("LINE"),
                    rs.getBoolean("STRIKE")
            );
            ret.put(chconf.id, chconf);
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * 単体チャンネル設定情報取得
     * @param con コネクション
     * @param channelid 取得するチャンネルのID
     * @return チャンネル設定情報
     * @throws SQLException SQL異常
     */
    public ChannelConf reloadChanelConf(Connection con, int channelid) throws SQLException {
        ChannelConf ret = null;
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM CHCONF WHERE ID = ?;");
        prep.setInt(1, channelid);
        rs = prep.executeQuery();
        while (rs.next()) {
            ret = new ChannelConf(
                    rs.getInt("ID"),
                    rs.getString("COLOR"),
                    rs.getBoolean("BOLD"),
                    rs.getBoolean("ITALIC"),
                    rs.getBoolean("LINE"),
                    rs.getBoolean("STRIKE")
            );
        }
        rs.close();
        prep.close();

        return ret;
    }
    
    /**
     * チャンネル設定作成
     * @param con コネクション
     * @param channelid 設定するチャンネルのID
     * @throws SQLException SQL異常
     */
    public void insertChannelConf(Connection con, int channelid) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("INSERT INTO CHCONF (ID) VALUES (?)");
        prep.setInt(1, channelid);
        prep.executeUpdate();
        prep.close();
    }

    /**
     * チャンネル設定更新
     * @param con コネクション
     * @param chconf ChannelConfインスタンス
     * @throws SQLException SQL異常
     */
    public void updateChannelConf(Connection con, ChannelConf chconf) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("UPDATE CHCONF SET COLOR = ?, BOLD = ?, ITALIC = ?, LINE = ?, STRIKE = ? WHERE ID = ?");
        prep.setString(1, chconf.color);
        prep.setBoolean(2, chconf.bold);
        prep.setBoolean(3, chconf.italic);
        prep.setBoolean(4, chconf.line);
        prep.setBoolean(5, chconf.strike);
        prep.setInt(6, chconf.id);
        prep.executeUpdate();
        prep.close();
    }

}
