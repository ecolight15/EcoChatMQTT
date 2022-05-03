package jp.minecraftuser.ecochatmqtt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserNgConf;
import jp.minecraftuser.ecoframework.DatabaseFrame;

/**
 * UserNgConf関連DB操作クラス
 * @author ecolight
 */
public class EcoChatDBUserNgConf extends DatabaseFrame {

    /**
     * コンストラクタ
     * @param frame_ EcoChatDBインスタンス 
     * @param name_ 一覧登録名
     */
    public EcoChatDBUserNgConf(DatabaseFrame frame_, String name_) {
        super(frame_, name_);
    }

    /**
     * 全ユーザーNG設定情報取得(設定がある場合のみ)
     * @param con コネクション
     * @return 全ユーザーNGリスト
     * @throws SQLException SQL異常
     */
    public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, UserNgConf>> loadAllUserNgConf(Connection con) throws SQLException {
        ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, UserNgConf>> ret = new ConcurrentHashMap<>();
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM USNGCONF;");
        rs = prep.executeQuery();
        while (rs.next()) {
            UserNgConf usngconf = new UserNgConf(
                    rs.getInt("USERID"),
                    rs.getInt("TARGET"),
                    rs.getBoolean("NG")
            );
            ConcurrentHashMap<Integer, UserNgConf> nglist;
            if (!ret.containsKey(usngconf.userid)) {
                nglist = new ConcurrentHashMap<>();
                ret.put(usngconf.userid, nglist);
            } else {
                nglist = ret.get(usngconf.userid);
            }
            nglist.put(usngconf.target, usngconf);
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * 単体ユーザーNG設定情報取得
     * @param con コネクション
     * @param userid 取得するユーザーID
     * @param targetid 取得する相手ユーザーID
     * @return ユーザーNG設定情報
     * @throws SQLException SQL異常
     */
    public UserNgConf reloadUserNgConf(Connection con, int userid, int targetid) throws SQLException {
        UserNgConf ret = null;

        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM USNGCONF WHERE USERID = ? AND TARGET = ?;");
        prep.setInt(1, userid);
        prep.setInt(2, targetid);
        rs = prep.executeQuery();
        while (rs.next()) {
            ret = new UserNgConf(
                    rs.getInt("USERID"),
                    rs.getInt("TARGET"),
                    rs.getBoolean("NG")
            );
        }
        rs.close();
        prep.close();

        return ret;
    }
    
    /**
     * 対ユーザーNG設定作成
     * @param con コネクション
     * @param userid 設定するユーザーID
     * @param targetid 設定するチャンネルのID
     * @throws SQLException SQL異常
     */
    public void insertUserNgConf(Connection con, int userid, int targetid) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("INSERT INTO USNGCONF (USERID, TARGET) VALUES (?, ?)");
        prep.setInt(1, userid);
        prep.setInt(2, targetid);
        prep.executeUpdate();
        prep.close();
    }

    /**
     * 対ユーザーNG設定更新
     * @param con コネクション
     * @param usngconf UserNgConfインスタンス
     * @throws SQLException SQL異常
     */
    public void updateUserNgConf(Connection con, UserNgConf usngconf) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("UPDATE USNGCONF SET NG = ? WHERE USERID = ? AND TARGET = ?");
        prep.setBoolean(1, usngconf.ng);
        prep.setInt(2, usngconf.userid);
        prep.setInt(3, usngconf.target);
        prep.executeUpdate();
        prep.close();
    }

}
