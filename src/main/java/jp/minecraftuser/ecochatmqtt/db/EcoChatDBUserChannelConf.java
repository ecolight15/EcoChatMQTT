package jp.minecraftuser.ecochatmqtt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserChannelConf;
import jp.minecraftuser.ecoframework.DatabaseFrame;

/**
 * UserChannelConf関連DB操作クラス
 * @author ecolight
 */
public class EcoChatDBUserChannelConf extends DatabaseFrame {

    /**
     * コンストラクタ
     * @param frame_ EcoChatDBインスタンス 
     * @param name_ 一覧登録名
     */
    public EcoChatDBUserChannelConf(DatabaseFrame frame_, String name_) {
        super(frame_, name_);
    }

    /**
     * 全ユーザーチャンネル設定情報取得(設定があるチャンネルのみ)
     * @param con コネクション
     * @return 全ユーザーチャンネルリスト
     * @throws SQLException SQL異常
     */
    public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, UserChannelConf>> loadAllUserChannelConf(Connection con) throws SQLException {
        ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, UserChannelConf>> ret = new ConcurrentHashMap<>();
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM USCHCONF;");
        rs = prep.executeQuery();
        while (rs.next()) {
            UserChannelConf uschconf = new UserChannelConf(
                    rs.getInt("USERID"),
                    rs.getInt("ID"),
                    rs.getString("COLOR"),
                    rs.getBoolean("BOLD"),
                    rs.getBoolean("ITALIC"),
                    rs.getBoolean("LINE"),
                    rs.getBoolean("STRIKE")
            );
            ConcurrentHashMap<Integer, UserChannelConf> chlist;
            if (!ret.containsKey(uschconf.userid)) {
                chlist = new ConcurrentHashMap<>();
                ret.put(uschconf.userid, chlist);
            } else {
                chlist = ret.get(uschconf.userid);
            }
            chlist.put(uschconf.id, uschconf);
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * 単体ユーザーチャンネル設定情報取得
     * @param con コネクション
     * @param userid 取得するユーザーID
     * @param channelid 取得するチャンネルのID
     * @return ユーザーチャンネル設定情報
     * @throws SQLException SQL異常
     */
    public UserChannelConf reloadUserChannelConf(Connection con, int userid, int channelid) throws SQLException {
        UserChannelConf ret = null;

        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM USCHCONF WHERE USERID = ? AND ID = ?;");
        prep.setInt(1, userid);
        prep.setInt(2, channelid);
        rs = prep.executeQuery();
        while (rs.next()) {
            ret = new UserChannelConf(
                    rs.getInt("USERID"),
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
     * ユーザーチャンネル設定作成
     * @param con コネクション
     * @param userid 設定するユーザーID
     * @param channelid 設定するチャンネルのID
     * @throws SQLException SQL異常
     */
    public void insertUserChannelConf(Connection con, int userid, int channelid) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("INSERT INTO USCHCONF (USERID, ID) VALUES (?, ?)");
        prep.setInt(1, userid);
        prep.setInt(2, channelid);
        prep.executeUpdate();
        prep.close();
    }

    /**
     * ユーザーチャンネル設定更新
     * @param con コネクション
     * @param uschconf UserChannelConfインスタンス
     * @throws SQLException SQL異常
     */
    public void updateUserChannelConf(Connection con, UserChannelConf uschconf) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("UPDATE USCHCONF SET COLOR = ?, BOLD = ?, ITALIC = ?, LINE = ?, STRIKE = ? WHERE USERID = ? AND ID = ?");
        prep.setString(1, uschconf.color);
        prep.setBoolean(2, uschconf.bold);
        prep.setBoolean(3, uschconf.italic);
        prep.setBoolean(4, uschconf.line);
        prep.setBoolean(5, uschconf.strike);
        prep.setInt(6, uschconf.userid);
        prep.setInt(7, uschconf.id);
        prep.executeUpdate();
        prep.close();
    }

}
