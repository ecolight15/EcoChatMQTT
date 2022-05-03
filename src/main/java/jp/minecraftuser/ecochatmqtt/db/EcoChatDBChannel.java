package jp.minecraftuser.ecochatmqtt.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecoframework.DatabaseFrame;

/**
 * Channel関連DB操作クラス
 * @author ecolight
 */
public class EcoChatDBChannel extends DatabaseFrame {

    /**
     * コンストラクタ
     * @param frame_ EcoChatDBインスタンス 
     * @param name_ 一覧登録名
     */
    public EcoChatDBChannel(DatabaseFrame frame_, String name_) {
        super(frame_, name_);
    }

    /**
     * 全チャンネル情報取得
     * @param con コネクション
     * @return 全チャネルリスト
     * @throws SQLException SQL異常
     */
    public ConcurrentHashMap<Integer, Channel> loadAllChanels(Connection con) throws SQLException {
        ConcurrentHashMap<Integer, Channel> ret = new ConcurrentHashMap<>();
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM CHANNEL;");
        rs = prep.executeQuery();
        while (rs.next()) {
            Channel ch = new Channel(
                    rs.getInt("ID"),
                    rs.getString("TAG"),
                    rs.getString("NAME"),
                    Channel.Type.getByNum(rs.getInt("TYPE")),
                    rs.getString("ENTERMSG"),
                    rs.getString("LEAVEMSG"),
                    rs.getString("WELCOMEMSG"),
                    rs.getString("GOODBYEMSG"),
                    rs.getBoolean("AUTOJOIN"),
                    rs.getBoolean("LISTED"),
                    rs.getBoolean("ADDPERM"),
                    rs.getBoolean("ACTIVATE"),
                    rs.getLong("SINCE")
                    );
            ret.put(ch.id, ch);
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * 全チャンネル情報取得
     * @param con コネクション
     * @return 全チャネルリスト
     * @throws SQLException SQL異常
     */
    public ConcurrentHashMap<Integer, Channel> loadAutoJoinChanels(Connection con) throws SQLException {
        ConcurrentHashMap<Integer, Channel> ret = new ConcurrentHashMap<>();
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM CHANNEL WHERE AUTOJOIN = 1;");
        rs = prep.executeQuery();
        while (rs.next()) {
            Channel ch = new Channel(
                    rs.getInt("ID"),
                    rs.getString("TAG"),
                    rs.getString("NAME"),
                    Channel.Type.getByNum(rs.getInt("TYPE")),
                    rs.getString("ENTERMSG"),
                    rs.getString("LEAVEMSG"),
                    rs.getString("WELCOMEMSG"),
                    rs.getString("GOODBYEMSG"),
                    rs.getBoolean("AUTOJOIN"),
                    rs.getBoolean("LISTED"),
                    rs.getBoolean("ADDPERM"),
                    rs.getBoolean("ACTIVATE"),
                    rs.getLong("SINCE")
                    );
            ret.put(ch.id, ch);
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * 単体チャンネル情報取得
     * @param con コネクション
     * @param tag 取得するチャンネルのタグ名(大文字小文字不問)
     * @return チャンネル情報
     * @throws SQLException SQL異常
     */
    public Channel reloadChanel(Connection con, String tag) throws SQLException {
        Channel ret = null;
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM CHANNEL WHERE TAG LIKE ?;");
        prep.setString(1, tag);
        rs = prep.executeQuery();
        while (rs.next()) {
            ret = new Channel(
                    rs.getInt("ID"),
                    rs.getString("TAG"),
                    rs.getString("NAME"),
                    Channel.Type.getByNum(rs.getInt("TYPE")),
                    rs.getString("ENTERMSG"),
                    rs.getString("LEAVEMSG"),
                    rs.getString("WELCOMEMSG"),
                    rs.getString("GOODBYEMSG"),
                    rs.getBoolean("AUTOJOIN"),
                    rs.getBoolean("LISTED"),
                    rs.getBoolean("ADDPERM"),
                    rs.getBoolean("ACTIVATE"),
                    rs.getLong("SINCE")
                    );
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * 単体チャンネル情報取得
     * @param con コネクション
     * @param id 取得するチャンネルのID
     * @return チャンネル情報
     * @throws SQLException SQL異常
     */
    public Channel reloadChanel(Connection con, int id) throws SQLException {
        Channel ret = null;
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        prep = con.prepareStatement("SELECT * FROM CHANNEL WHERE ID = ?;");
        prep.setInt(1, id);
        rs = prep.executeQuery();
        while (rs.next()) {
            ret = new Channel(
                    rs.getInt("ID"),
                    rs.getString("TAG"),
                    rs.getString("NAME"),
                    Channel.Type.getByNum(rs.getInt("TYPE")),
                    rs.getString("ENTERMSG"),
                    rs.getString("LEAVEMSG"),
                    rs.getString("WELCOMEMSG"),
                    rs.getString("GOODBYEMSG"),
                    rs.getBoolean("AUTOJOIN"),
                    rs.getBoolean("LISTED"),
                    rs.getBoolean("ADDPERM"),
                    rs.getBoolean("ACTIVATE"),
                    rs.getLong("SINCE")
                    );
        }
        rs.close();
        prep.close();

        return ret;
    }

    /**
     * チャンネル作成
     * @param con コネクション
     * @param ch Channelインスタンス
     * @return ユーザー設定情報
     * @throws SQLException SQL異常
     */
    public User insertChannel(Connection con, Channel ch) throws SQLException {
        User ret = null;
        
        PreparedStatement prep = null;
        prep = con.prepareStatement("INSERT INTO CHANNEL (TAG, NAME, TYPE, ENTERMSG, LEAVEMSG, WELCOMEMSG, GOODBYEMSG, AUTOJOIN, LISTED, ADDPERM, ACTIVATE, SINCE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        prep.setString(1, ch.tag);
        prep.setString(2, ch.name);
        prep.setInt(3, ch.type.getInt());
        prep.setString(4, ch.enterMessage);
        prep.setString(5, ch.leaveMessage);
        prep.setString(6, ch.enterMessage);
        prep.setString(7, ch.leaveMessage);
        prep.setBoolean(8, ch.autoJoin);
        prep.setBoolean(9, ch.listed);
        prep.setBoolean(10, ch.addPerm);
        prep.setBoolean(11, ch.activate);
        prep.setLong(12, (new Date()).getTime());
        prep.executeUpdate();
        prep.close();
        return ret;
    }

    /**
     * チャンネル作成
     * @param con コネクション
     * @param chtag Channel タグ名
     * @return ユーザー設定情報
     * @throws SQLException SQL異常
     */
    public User insertChannel(Connection con, String chtag) throws SQLException {
        User ret = null;
        
        PreparedStatement prep = null;
        prep = con.prepareStatement("INSERT INTO CHANNEL (TAG, NAME, TYPE, SINCE) VALUES (?, ?, ?, ?)");
        prep.setString(1, chtag);
        prep.setString(2, chtag);
        prep.setInt(3, Channel.Type.global.getInt());
        prep.setLong(4, (new Date()).getTime());
        prep.executeUpdate();
        prep.close();
        return ret;
    }

    /**
     * チャンネル削除
     * @param con コネクション
     * @param ch 削除するチャンネル
     * @throws SQLException SQL異常
     */
    public void deleteChannel(Connection con, Integer ch) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("DELETE FROM CHANNEL WHERE ID = ?");
        prep.setInt(1, ch);
        prep.executeUpdate();
        prep.close();
    }

    /**
     * チャンネル更新
     * @param con コネクション
     * @param ch Channelインスタンス
     * @throws SQLException SQL異常
     */
    public void updateChannel(Connection con, Channel ch) throws SQLException {
        PreparedStatement prep = null;
        prep = con.prepareStatement("UPDATE CHANNEL SET TAG = ?, NAME = ?, TYPE = ?, ENTERMSG = ?, LEAVEMSG = ?, WELCOMEMSG = ?, GOODBYEMSG = ?, AUTOJOIN = ?, LISTED = ?, ADDPERM = ?, ACTIVATE = ? WHERE ID = ?");
        prep.setString(1, ch.tag);
        prep.setString(2, ch.name);
        prep.setInt(3, ch.type.getInt());
        prep.setString(4, ch.enterMessage);
        prep.setString(5, ch.leaveMessage);
        prep.setString(6, ch.welcomeMessage);
        prep.setString(7, ch.goodbyeMessage);
        prep.setBoolean(8, ch.autoJoin);
        prep.setBoolean(9, ch.listed);
        prep.setBoolean(10, ch.addPerm);
        prep.setBoolean(11, ch.activate);
        prep.setInt(12, ch.id);
        prep.executeUpdate();
        prep.close();
    }

}
