package jp.minecraftuser.ecochatmqtt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserUserConf;
import jp.minecraftuser.ecoframework.DatabaseFrame;

/**
 * UserUserConf関連DB操作クラス
 * @author ecolight
 */
public class EcoChatDBUserUserConf extends DatabaseFrame {

    /**
     * コンストラクタ
     * @param frame_ EcoChatDBインスタンス 
     * @param name_ 一覧登録名
     */
    public EcoChatDBUserUserConf(DatabaseFrame frame_, String name_) {
        super(frame_, name_);
    }

    /**
     * 全ユーザー対ユーザー設定情報取得(設定があるチャンネルのみ)
     * @param con コネクション
     * @return 全ユーザーチャンネルリスト
     * @throws SQLException SQL異常
     */
    public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, UserUserConf>> loadAllUserUserConf(Connection con) throws SQLException {
        ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, UserUserConf>> ret = new ConcurrentHashMap<>();
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM USUSCONF;");
        rs = prep.executeQuery();
        while (rs.next()) {
            UserUserConf ususconf = new UserUserConf(
                    rs.getInt("USERID"),
                    rs.getInt("TARGET"),
                    rs.getString("COLOR"),
                    rs.getBoolean("BOLD"),
                    rs.getBoolean("ITALIC"),
                    rs.getBoolean("LINE"),
                    rs.getBoolean("STRIKE")
            );
            ConcurrentHashMap<Integer, UserUserConf> uslist;
            if (!ret.containsKey(ususconf.userid)) {
                uslist = new ConcurrentHashMap<>();
                ret.put(ususconf.userid, uslist);
            } else {
                uslist = ret.get(ususconf.userid);
            }
            uslist.put(ususconf.target, ususconf);
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * 単体ユーザー対ユーザー設定情報取得
     * @param con コネクション
     * @param userid 取得するユーザーID
     * @param targetid 取得する相手ユーザーID
     * @return ユーザーチャンネル設定情報
     * @throws SQLException SQL異常
     */
    public UserUserConf reloadUserUserConf(Connection con, int userid, int targetid) throws SQLException {
        UserUserConf ret = null;

        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM USUSCONF WHERE USERID = ? AND TARGET = ?;");
        prep.setInt(1, userid);
        prep.setInt(2, targetid);
        rs = prep.executeQuery();
        while (rs.next()) {
            ret = new UserUserConf(
                    rs.getInt("USERID"),
                    rs.getInt("TARGET"),
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
     * 対ユーザー設定作成
     * @param con コネクション
     * @param userid 設定するユーザーID
     * @param targetid 設定するチャンネルのID
     * @throws SQLException SQL異常
     */
    public void insertUserUserConf(Connection con, int userid, int targetid) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("INSERT INTO USUSCONF (USERID, TARGET) VALUES (?, ?)");
        prep.setInt(1, userid);
        prep.setInt(2, targetid);
        prep.executeUpdate();
        prep.close();
    }

    /**
     * 対ユーザー設定更新
     * @param con コネクション
     * @param ususconf UserChannelConfインスタンス
     * @throws SQLException SQL異常
     */
    public void updateUserUserConf(Connection con, UserUserConf ususconf) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("UPDATE USUSCONF SET COLOR = ?, BOLD = ?, ITALIC = ?, LINE = ?, STRIKE = ? WHERE USERID = ? AND TARGET = ?");
        prep.setString(1, ususconf.color);
        prep.setBoolean(2, ususconf.bold);
        prep.setBoolean(3, ususconf.italic);
        prep.setBoolean(4, ususconf.line);
        prep.setBoolean(5, ususconf.strike);
        prep.setInt(6, ususconf.userid);
        prep.setInt(7, ususconf.target);
        prep.executeUpdate();
        prep.close();
    }

}
