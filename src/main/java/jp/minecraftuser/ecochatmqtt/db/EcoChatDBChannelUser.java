package jp.minecraftuser.ecochatmqtt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelUser;
import jp.minecraftuser.ecoframework.DatabaseFrame;

/**
 * UserNgConf関連DB操作クラス
 * @author ecolight
 */
public class EcoChatDBChannelUser extends DatabaseFrame {

    /**
     * コンストラクタ
     * @param frame_ EcoChatDBインスタンス 
     * @param name_ 一覧登録名
     */
    public EcoChatDBChannelUser(DatabaseFrame frame_, String name_) {
        super(frame_, name_);
    }

    /**
     * 全チャンネルユーザー情報取得
     * @param con コネクション
     * @return 全ユーザーNGリスト
     * @throws SQLException SQL異常
     */
    public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ChannelUser>> loadAllChannelUser(Connection con) throws SQLException {
        ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ChannelUser>> ret = new ConcurrentHashMap<>();
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM CHUSERS;");
        rs = prep.executeQuery();
        while (rs.next()) {
            ChannelUser chus = new ChannelUser(
                    rs.getInt("ID"),
                    rs.getInt("USERID"),
                    rs.getBoolean("OWNER"),
                    rs.getLong("JOINDATE")
            );
            ConcurrentHashMap<Integer, ChannelUser> uslist;
            if (!ret.containsKey(chus.userid)) {
                uslist = new ConcurrentHashMap<>();
                ret.put(chus.id, uslist);
            } else {
                uslist = ret.get(chus.id);
            }
            uslist.put(chus.userid, chus);
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * 単一ユーザーのチャンネルユーザー情報取得
     * @param con コネクション
     * @param userid ユーザーID
     * @return 全ユーザーNGリスト
     * @throws SQLException SQL異常
     */
    public ConcurrentHashMap<Integer, ChannelUser> loadChannelUser(Connection con, Integer userid) throws SQLException {
        ConcurrentHashMap<Integer, ChannelUser> ret = new ConcurrentHashMap<>();
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM CHUSERS WHERE USERID = ?;");
        prep.setInt(1, userid);
        rs = prep.executeQuery();
        while (rs.next()) {
            ChannelUser chus = new ChannelUser(
                    rs.getInt("ID"),
                    rs.getInt("USERID"),
                    rs.getBoolean("OWNER"),
                    rs.getLong("JOINDATE")
            );
            ret.put(chus.id, chus);
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * 特定チャンネルのOwnerユーザー情報取得
     * @param con コネクション
     * @param chid チャンネルID
     * @return 全ユーザーNGリスト
     * @throws SQLException SQL異常
     */
    public ConcurrentHashMap<Integer, ChannelUser> loadOwnerChannelUsers(Connection con, Integer chid) throws SQLException {
        ConcurrentHashMap<Integer, ChannelUser> ret = new ConcurrentHashMap<>();
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM CHUSERS WHERE ID = ? AND OWNER = 1;");
        prep.setInt(1, chid);
        rs = prep.executeQuery();
        while (rs.next()) {
            ChannelUser chus = new ChannelUser(
                    rs.getInt("ID"),
                    rs.getInt("USERID"),
                    rs.getBoolean("OWNER"),
                    rs.getLong("JOINDATE")
            );
            ret.put(chus.id, chus);
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * 特定チャンネルの人数情報取得
     * @param con コネクション
     * @param chid チャンネルID
     * @return 全ユーザーNGリスト
     * @throws SQLException SQL異常
     */
    public int countChannelUser(Connection con, int chid) throws SQLException {
        int ret = 0;
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT COUNT(*) FROM CHUSERS WHERE ID = ?;");
        prep.setInt(1, chid);
        rs = prep.executeQuery();
        if (rs.next()) {
            ret = rs.getInt(1);
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * 単体チャンネルユーザー情報取得
     * @param con コネクション
     * @param channelid 取得するチャンネルID
     * @param userid 取得するユーザーID
     * @return ユーザーNG設定情報
     * @throws SQLException SQL異常
     */
    public ChannelUser reloadChannelUser(Connection con, int channelid, int userid) throws SQLException {
        ChannelUser ret = null;

        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM CHUSERS WHERE ID = ? AND USERID = ?;");
        prep.setInt(1, channelid);
        prep.setInt(2, userid);
        rs = prep.executeQuery();
        while (rs.next()) {
            ret = new ChannelUser(
                    rs.getInt("ID"),
                    rs.getInt("USERID"),
                    rs.getBoolean("OWNER"),
                    rs.getLong("JOINDATE")
            );
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * ユーザー作成
     * @param con コネクション
     * @param ch 所属するチャンネル
     * @param us 所属するユーザー
     * @throws SQLException SQL異常
     */
    public void insertChannelUser(Connection con, Integer ch, Integer us) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("INSERT INTO CHUSERS (ID, USERID, JOINDATE) VALUES (?, ?, ?)");
        prep.setInt(1, ch);
        prep.setInt(2, us);
        prep.setLong(3, new Date().getTime());
        prep.executeUpdate();
        prep.close();
    }

    /**
     * ユーザー作成
     * @param con コネクション
     * @param ch 所属するチャンネル
     * @param us 所属するユーザー
     * @throws SQLException SQL異常
     */
    public void insertOwnerChannelUser(Connection con, Integer ch, Integer us) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("INSERT INTO CHUSERS (ID, USERID, OWNER, JOINDATE) VALUES (?, ?, ?, ?)");
        prep.setInt(1, ch);
        prep.setInt(2, us);
        prep.setBoolean(3, true);
        prep.setLong(4, new Date().getTime());
        prep.executeUpdate();
        prep.close();
    }
    
    /**
     * ユーザー離脱
     * @param con コネクション
     * @param ch 所属するチャンネル
     * @param us 所属するユーザー
     * @throws SQLException SQL異常
     */
    public void deleteChannelUser(Connection con, Integer ch, Integer us) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("DELETE FROM CHUSERS WHERE ID = ? AND USERID = ?");
        prep.setInt(1, ch);
        prep.setInt(2, us);
        prep.executeUpdate();
        prep.close();
    }
    
    /**
     * チャンネルユーザ更新
     * @param con コネクション
     * @param chus ChannelUserインスタンス
     * @throws SQLException SQL異常
     */
    public void updateChannelUser(Connection con, ChannelUser chus) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("UPDATE CHUSERS SET OWNER = ? WHERE ID = ? AND USERID = ?");
        prep.setBoolean(1, chus.owner);
        prep.setInt(2, chus.id);
        prep.setInt(3, chus.userid);
        prep.executeUpdate();
        prep.close();
    }
}
