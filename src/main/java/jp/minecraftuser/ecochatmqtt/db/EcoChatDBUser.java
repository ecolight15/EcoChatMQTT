package jp.minecraftuser.ecochatmqtt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecoframework.DatabaseFrame;
import org.bukkit.entity.Player;

/**
 * User関連DB操作クラス
 * @author ecolight
 */
public class EcoChatDBUser extends DatabaseFrame {

    /**
     * コンストラクタ
     * @param frame_ EcoChatDBインスタンス 
     * @param name_ 一覧登録名
     */
    public EcoChatDBUser(DatabaseFrame frame_, String name_) {
        super(frame_, name_);
    }

    /**
     * 全ユーザー設定情報取得
     * @param con コネクション
     * @return 全ユーザー設定情報リスト
     * @throws SQLException SQL異常
     */
    public ConcurrentHashMap<Integer, User> loadAllUser(Connection con) throws SQLException {
        ConcurrentHashMap<Integer, User> ret = new ConcurrentHashMap<>();
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM USERS;");
        rs = prep.executeQuery();
        while (rs.next()) {
            User us = new User(
                    rs.getInt("USERID"),
                    new UUID(
                        rs.getLong("MOSTUUID"),
                        rs.getLong("LEASTUUID")
                    ),
                    rs.getInt("ACTIVE"),
                    rs.getBoolean("MUTE"),
                    rs.getInt("LOCAL"),
                    rs.getBoolean("INFO"),
                    rs.getBoolean("NGVIEW"),
                    rs.getBoolean("LRANGE"),
                    rs.getBoolean("RSWARN"),
                    rs.getBoolean("SPYCHAT"),
                    rs.getBoolean("SPYPM")
            );
            ret.put(us.id, us);
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * 単体ユーザー設定情報取得
     * @param con コネクション
     * @param uuid 取得するユーザーのUUID
     * @return ユーザー設定情報
     * @throws SQLException SQL異常
     */
    public User reloadUser(Connection con, UUID uuid) throws SQLException {
        User ret = null;
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM USERS WHERE MOSTUUID = ? AND LEASTUUID = ?;");
        prep.setLong(1, uuid.getMostSignificantBits());
        prep.setLong(2, uuid.getLeastSignificantBits());
        rs = prep.executeQuery();
        while (rs.next()) {
            ret = new User(
                    rs.getInt("USERID"),
                    new UUID(
                        rs.getLong("MOSTUUID"),
                        rs.getLong("LEASTUUID")
                    ),
                    rs.getInt("ACTIVE"),
                    rs.getBoolean("MUTE"),
                    rs.getInt("LOCAL"),
                    rs.getBoolean("INFO"),
                    rs.getBoolean("NGVIEW"),
                    rs.getBoolean("LRANGE"),
                    rs.getBoolean("RSWARN"),
                    rs.getBoolean("SPYCHAT"),
                    rs.getBoolean("SPYPM")
            );
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * 単体ユーザー設定情報取得
     * @param con コネクション
     * @param id 取得するユーザーのID
     * @return ユーザー設定情報
     * @throws SQLException SQL異常
     */
    public User reloadUser(Connection con, int id) throws SQLException {
        User ret = null;
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM USERS WHERE USERID = ?;");
        prep.setInt(1, id);
        rs = prep.executeQuery();
        while (rs.next()) {
            ret = new User(
                    rs.getInt("USERID"),
                    new UUID(
                        rs.getLong("MOSTUUID"),
                        rs.getLong("LEASTUUID")
                    ),
                    rs.getInt("ACTIVE"),
                    rs.getBoolean("MUTE"),
                    rs.getInt("LOCAL"),
                    rs.getBoolean("INFO"),
                    rs.getBoolean("NGVIEW"),
                    rs.getBoolean("LRANGE"),
                    rs.getBoolean("RSWARN"),
                    rs.getBoolean("SPYCHAT"),
                    rs.getBoolean("SPYPM")
            );
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * ユーザー作成
     * @param con コネクション
     * @param pl Userインスタンス
     * @param active アクティブチャンネルに設定するチャンネルのID
     * @throws SQLException SQL異常
     */
    public void insertUser(Connection con, Player pl, int active) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("INSERT INTO USERS (MOSTUUID, LEASTUUID, ACTIVE) VALUES (?, ?, ?)");
        prep.setLong(1, pl.getUniqueId().getMostSignificantBits());
        prep.setLong(2, pl.getUniqueId().getLeastSignificantBits());
        prep.setInt(3, active);
        prep.executeUpdate();
        prep.close();
    }

    /**
     * ユーザー作成
     * @param con コネクション
     * @param uuid uuidインスタンス
     * @param active アクティブチャンネルに設定するチャンネルのID
     * @throws SQLException SQL異常
     */
    public void updateUserActive(Connection con, UUID uuid, int active) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("UPDATE USERS SET ACTIVE = ? WHERE MOSTUUID = ? AND LEASTUUID = ?");
        prep.setInt(1, active);
        prep.setLong(2, uuid.getMostSignificantBits());
        prep.setLong(3, uuid.getLeastSignificantBits());
        prep.executeUpdate();
        prep.close();
    }

    /**
     * ユーザー作成
     * @param con コネクション
     * @param us Userインスタンス
     * @throws SQLException SQL異常
     */
    public void updateUser(Connection con, User us) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("UPDATE USERS SET ACTIVE = ?, MUTE = ?, LOCAL = ?, INFO = ?, NGVIEW = ?, LRANGE = ?, RSWARN = ? WHERE MOSTUUID = ? AND LEASTUUID = ?");
        prep.setInt(1, us.activeChannel);
        prep.setBoolean(2, us.mute);
        prep.setInt(3, us.localRange);
        prep.setBoolean(4, us.infoJoinLeave);
        prep.setBoolean(5, us.showNGUser);
        prep.setBoolean(6, us.infoNotifyCount);
        prep.setBoolean(7, us.rsWarn);
        prep.setLong(8, us.uuid.getMostSignificantBits());
        prep.setLong(9, us.uuid.getLeastSignificantBits());
        prep.executeUpdate();
        prep.close();
    }

    /**
     * ユーザー作成
     * @param con コネクション
     * @param uuid uuidインスタンス
     * @param chat
     * @param pm
     * @throws SQLException SQL異常
     */
    public void updateUserSpy(Connection con, UUID uuid, boolean chat, boolean pm) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("UPDATE USERS SET SPYCHAT = ?, SPYPM = ? WHERE MOSTUUID = ? AND LEASTUUID = ?");
        prep.setBoolean(1, chat);
        prep.setBoolean(2, pm);
        prep.setLong(3, uuid.getMostSignificantBits());
        prep.setLong(4, uuid.getLeastSignificantBits());
        prep.executeUpdate();
        prep.close();
    }
}
