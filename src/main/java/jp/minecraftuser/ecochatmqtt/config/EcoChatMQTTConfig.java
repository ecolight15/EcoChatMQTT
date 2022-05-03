
package jp.minecraftuser.ecochatmqtt.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannel;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelConf;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBChannelPassword;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUser;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUserChannelConf;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUserNgConf;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDBUserUserConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelPassword;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserChannelConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserNgConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserUserConf;
import jp.minecraftuser.ecoframework.ConfigFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.entity.Player;

/**
 * デフォルトコンフィグクラス
 * @author ecolight
 */
public class EcoChatMQTTConfig extends ConfigFrame{

    protected ConcurrentHashMap<String, Channel> map_ch;                              // 起動時に初回読み込み CH追加/削除/Channel変更通知で更新
    protected ConcurrentHashMap<Integer, Channel> chlist;

    protected ConcurrentHashMap<String, ChannelConf> map_chconf;                      // 起動時に初回読み込み CH追加/削除/ChannelConf変更通知で更新
    protected ConcurrentHashMap<Integer, ChannelConf> chconf;
            
    protected ConcurrentHashMap<String, ChannelPassword> map_chpass;                  // 起動時に初回読み込み CH追加/削除/ChannelPassword変更通知で更新
    protected ConcurrentHashMap<Integer, ChannelPassword> chpass;

    protected ConcurrentHashMap<String, ConcurrentHashMap<UUID, ChannelUser>> map_chuser;       // 起動時のオンラインユーザーとログイン時に読み込み チャンネルJoin/Leave通知で更新
    protected ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ChannelUser>> chuslist;
    protected ConcurrentHashMap<UUID, ConcurrentHashMap<String, ChannelUser>> map_chuser_us;    // ユーザー側が主キーのリストも作成しておく
    protected ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ChannelUser>> chuslist_us;

    protected ConcurrentHashMap<UUID, User> map_user;                                 // 起動時に初回読み込み ログイン時に新規作成判定(+新規作成) User変更通知で更新
    protected ConcurrentHashMap<Integer, User> uslist;
    
    protected ConcurrentHashMap<UUID, ConcurrentHashMap<String, UserChannelConf>> map_uschconf; // 起動時に初回読み込み CH追加/削除/UserChannelConf変更通知で更新
    protected ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, UserChannelConf>> uschconf;

    protected ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, UserNgConf>> map_usngconf;        // 起動時に初回読み込み UserNgConf変更通知で更新
    protected ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, UserNgConf>> usngconf;

    protected ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, UserUserConf>> map_ususconf;      // 起動時に初回読み込み UserUserConf変更通知で更新
    protected ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, UserUserConf>> ususconf;
    
    /**
     * コンストラクタ
     * @param plg_ プラグインフレームインスタンス
     */
    public EcoChatMQTTConfig(PluginFrame plg_) {
        super(plg_);
        map_ch = new ConcurrentHashMap<>();
        map_chconf = new ConcurrentHashMap<>();
        map_chpass = new ConcurrentHashMap<>();
        map_user = new ConcurrentHashMap<>();
        map_uschconf = new ConcurrentHashMap<>();
        map_usngconf = new ConcurrentHashMap<>();
        map_ususconf = new ConcurrentHashMap<>();
        chuslist = new ConcurrentHashMap<>();
        map_chuser = new ConcurrentHashMap<>();
        chuslist_us = new ConcurrentHashMap<>();
        map_chuser_us = new ConcurrentHashMap<>();
    }
    
    /**
     * DB作成後に呼び出されるDB情報読み込み
     * @throws SQLException 
     */
    public void loadDatabase() throws SQLException {
        log.info("Start loading database.");
        // connectionの取得
        EcoChatDB db = (EcoChatDB) plg.getDB("chat");
        Connection con = db.connect();

        // channel の読み込み
        EcoChatDBChannel chdb = (EcoChatDBChannel) plg.getDB("channel");
        chlist = chdb.loadAllChanels(con);
        for (Channel ch : chlist.values()) {
            log.log(Level.INFO, "Loading channel [{0}] data.", ch.tag);
            map_ch.put(ch.tag.toLowerCase(), ch);
        }

        // 各種チャンネル関連情報の読み込み
        EcoChatDBChannelConf chconfdb = (EcoChatDBChannelConf) plg.getDB("channel_conf");
        EcoChatDBChannelPassword chpassdb = (EcoChatDBChannelPassword) plg.getDB("channel_password");
        chconf = chconfdb.loadAllChanelConf(con);
        chpass = chpassdb.loadAllChanelPassword(con);
        for (Channel ch : map_ch.values()) {
            log.log(Level.INFO, "Loading channel [{0}] configuration data.", ch.tag);
            // チャンネル設定
            if (chconf.containsKey(ch.id)) {
                map_chconf.put(ch.tag.toLowerCase(), chconf.get(ch.id));
            }
            // チャンネルパスワード
            if (chpass.containsKey(ch.id)) {
                map_chpass.put(ch.tag.toLowerCase(), chpass.get(ch.id));
            }
        }
        
        // 各種プレイヤー情報の読み込み
        EcoChatDBUser usdb = (EcoChatDBUser) plg.getDB("user");
        uslist = usdb.loadAllUser(con);
        for (User us : uslist.values()) {
            log.log(Level.INFO, "Loading user [{0}] data.", plg.getServer().getOfflinePlayer(us.uuid).getName());
            map_user.put(us.uuid, us);
        }
        
        // 各種ユーザー関連情報の読み込み
        EcoChatDBUserChannelConf uschconfdb = (EcoChatDBUserChannelConf) plg.getDB("user_channel_conf");
        EcoChatDBUserNgConf usngconfdb = (EcoChatDBUserNgConf) plg.getDB("user_ng_conf");
        EcoChatDBUserUserConf ususconfdb = (EcoChatDBUserUserConf) plg.getDB("user_user_conf");
        uschconf = uschconfdb.loadAllUserChannelConf(con);
        usngconf = usngconfdb.loadAllUserNgConf(con);
        ususconf = ususconfdb.loadAllUserUserConf(con);
        for (User us : map_user.values()) {
            log.log(Level.INFO, "Loading user [{0}] configuration data.", plg.getServer().getOfflinePlayer(us.uuid).getName());
            // UserChannelConf
            if (uschconf.containsKey(us.id)) {
                // ユーザーのテーブルを作成・取り出し
                ConcurrentHashMap<String, UserChannelConf> list;
                if (map_uschconf.containsKey(us.uuid)) {
                    list = map_uschconf.get(us.uuid);
                } else {
                    list = new ConcurrentHashMap<>();
                    map_uschconf.put(us.uuid, list);
                }
                // チャンネルのテーブルを追加
                for (Integer chid : uschconf.get(us.id).keySet()) {
                    list.put(chlist.get(chid).tag.toLowerCase(), uschconf.get(us.id).get(chid));
                }
            }
            // UserNgConf
            if (usngconf.containsKey(us.id)) {
                // ユーザーのテーブルを作成・取り出し
                ConcurrentHashMap<UUID, UserNgConf> list;
                if (map_usngconf.containsKey(us.uuid)) {
                    list = map_usngconf.get(us.uuid);
                } else {
                    list = new ConcurrentHashMap<>();
                    map_usngconf.put(us.uuid, list);
                }
                // 対ユーザーのテーブルを追加
                for (Integer targetid : usngconf.get(us.id).keySet()) {
                    list.put(uslist.get(targetid).uuid, usngconf.get(us.id).get(targetid));
                }
            }
            // UserUserConf
            if (ususconf.containsKey(us.id)) {
                // ユーザーのテーブルを作成・取り出し
                ConcurrentHashMap<UUID, UserUserConf> list;
                if (map_ususconf.containsKey(us.uuid)) {
                    list = map_ususconf.get(us.uuid);
                } else {
                    list = new ConcurrentHashMap<>();
                    map_ususconf.put(us.uuid, list);
                }
                // 対ユーザーのテーブルを追加
                for (Integer targetid : ususconf.get(us.id).keySet()) {
                    list.put(uslist.get(targetid).uuid, ususconf.get(us.id).get(targetid));
                }
            }
        }

        // 初期設定チャンネルリストを取得し、存在していなければ追加する
        EcoChatMQTTDefaultChannelConfig defconf = (EcoChatMQTTDefaultChannelConfig) plg.getPluginConfig("defch");
        for (String tag : defconf.getSectionList("DefaultChannels")) {
            if (!map_ch.containsKey(tag.toLowerCase())) {
                log.log(Level.INFO, "Create default channel [{0}]", tag);
                // ないので作成する
                chdb.insertChannel(con, new Channel(0, 
                    tag,
                    defconf.getString("DefaultChannels."+tag+".Name"),
                    Channel.Type.getByName(defconf.getString("DefaultChannels."+tag+".Type")),
                    defconf.getString("DefaultChannels."+tag+".EnterMessage"),
                    defconf.getString("DefaultChannels."+tag+".LeaveMessage"),
                    defconf.getString("DefaultChannels."+tag+".WelcomeMessage"),
                    defconf.getString("DefaultChannels."+tag+".GoodbyeMessage"),
                    defconf.getBoolean("DefaultChannels."+tag+".AutoJoin"),
                    defconf.getBoolean("DefaultChannels."+tag+".ListEnabled"),
                    defconf.getBoolean("DefaultChannels."+tag+".AddReqPerm"),
                    defconf.getBoolean("DefaultChannels."+tag+".Activate"),
                    0));
                con.commit();
                Channel ch = chdb.reloadChanel(con, tag);
                map_ch.put(tag.toLowerCase(), ch);
                chlist.put(ch.id, ch);
            }
        }
        con.commit();
        log.log(Level.INFO, "Loading database complete.");
    }

    /**
     * チャンネルが存在するかどうか
     * @param tag
     * @return 
     */
    public boolean isExistChannel(String tag) {
        return map_ch.containsKey(tag.toLowerCase());
    }
    
    /**
     * チャンネルパスワードが存在するかどうか
     * @param id
     * @return 
     */
    public boolean isExistChannelPassword(int id) {
        return chpass.containsKey(id);
    }
    
    /**
     * ユーザーが存在するかどうか
     * @param pl
     * @return 
     */
    public boolean isExistUser(Player pl) {
        return map_user.containsKey(pl.getUniqueId());
    }
    
    /**
     * 指定ユーザーが指定チャンネルのメンバーかどうか
     * @param tag
     * @param pl
     * @return 
     */
    public boolean isExistChannelUser(Player pl, String tag) {
        if (map_chuser.containsKey(tag.toLowerCase())) {
            return map_chuser.get(tag.toLowerCase()).containsKey(pl.getUniqueId());
        }
        return false;
    }

    /**
     * 指定ユーザーが指定チャンネルのメンバーかどうか
     * @param ch
     * @param pl
     * @return 
     */
    public boolean isExistChannelUserByID(int pl, int ch) {
        if (chuslist.containsKey(ch)) {
            return chuslist.get(ch).containsKey(pl);
        }
        return false;
    }

    /**
     * ユーザーチャンネル設定が存在するかどうか
     * @param pl
     * @return 
     */
    public boolean isExistUserChannelConf(Player pl) {
        return map_uschconf.containsKey(pl.getUniqueId());
    }

    /**
     * プレイヤーインスタンスからプレイヤーIDを取得する
     * @param pl
     * @return 
     */
    public int getPlayerIdByPlayer(Player pl) {
        return map_user.get(pl.getUniqueId()).id;
    }
    
    /**
     * チャンネルをタグ指定で取得する
     * @param tag
     * @return 
     */
    public Channel getChannel(String tag) {
        return map_ch.get(tag.toLowerCase());
    }
    
    /**
     * チャンネルをID指定で取得する
     * @param id
     * @return 
     */
    public Channel getChannel(int id) {
        return chlist.get(id);
    }
    
    /**
     * プレイヤー指定で参加チャンネル情報をすべて取得する
     * @param pl
     * @return 
     */
    public ConcurrentHashMap<String, ChannelUser> getChannelUser(Player pl) {
        return map_chuser_us.get(pl.getUniqueId());
    }

    /**
     * プレイヤー指定で参加チャンネル情報をすべて取得する
     * @param uuid
     * @param ch
     * @return 
     */
    public ChannelUser getChannelUser(UUID uuid, String ch) {
        if (map_chuser_us.containsKey(uuid)) {
            if (map_chuser_us.get(uuid).containsKey(ch.toLowerCase())) {
                return map_chuser_us.get(uuid).get(ch);
            }
        }
        return null;
    }

    /**
     * プレイヤー/チャンネル指定で参加チャンネル情報を取得する
     * @param pl
     * @param ch
     * @return 
     */
    public ChannelUser getChannelUser(Player pl, String ch) {
        return getChannelUser(pl.getUniqueId(), ch);
    }

    /**
     * ユーザをプレイヤー指定で取得する
     * @param pl
     * @return 
     */
    public User getUser(Player pl) {
        return EcoChatMQTTConfig.this.getUser(pl.getUniqueId());
    }
    
    /**
     * ユーザをUUID指定で取得する
     * @param pl
     * @return 
     */
    public User getUser(UUID pl) {
        return map_user.get(pl);
    }
    
    /**
     * ユーザをid指定で取得する
     * @param id
     * @return 
     */
    public User getUser(int id) {
        return uslist.get(id);
    }
    
    /**
     * チャンネル設定をタグ指定で取得する
     * @param tag
     * @return 
     */
    public ChannelConf getChannelConf(String tag) {
        return map_chconf.get(tag.toLowerCase());
    }
    
    /**
     * チャンネルパスワード設定をタグ指定で取得する
     * @param tag
     * @return 
     */
    public ChannelPassword getChannelPassword(String tag) {
        return map_chpass.get(tag.toLowerCase());
    }

    /**
     * ユーザーチャンネル設定をUUID,タグ指定で取得する
     * @param pl
     * @param tag
     * @return 
     */
    public UserChannelConf getUserChannelConf(UUID pl, String tag) {
        if (map_uschconf.containsKey(pl)) {
            return map_uschconf.get(pl).get(tag.toLowerCase());
        }
        return null;
    }

    /**
     * 対ユーザー設定をプレイヤーとターゲットのUUID指定で取得する
     * @param pl
     * @param target
     * @return 
     */
    public UserUserConf getUserUserConf(UUID pl, UUID target) {
        if (map_ususconf.containsKey(pl)) {
            return map_ususconf.get(pl).get(target);
        }
        return null;
    }
    
    /**
     * ユーザー対ユーザーNG設定取得
     * @param pl
     * @param target
     * @return 
     */
    public UserNgConf getUserNgConf(UUID pl, UUID target) {
        if (map_usngconf.containsKey(pl)) {
            return map_usngconf.get(pl).get(target);
        }
        return null;
    }
    
    /**
     * ユーザー対ユーザーNG設定取得
     * @param pl
     * @param target
     * @return 
     */
    public UserNgConf getUserNgConfByID(int pl, int target) {
        if (usngconf.containsKey(pl)) {
            return usngconf.get(pl).get(target);
        }
        return null;
    }

    /**
     * チャンネルを更新する
     * @param ch 
     */
    public void updateChannel(Channel ch) {
        // アップデート時はTAG検索用テーブルとID検索テーブル両方に更新をかける(idは主にDBのレコード検索用)
        if (chlist.containsKey(ch.id)) {
            chlist.replace(ch.id, ch);
        } else {
            chlist.put(ch.id, ch);
        }
        if (map_ch.containsKey(ch.tag.toLowerCase())) {
            map_ch.replace(ch.tag.toLowerCase(), ch);
        } else {
            map_ch.put(ch.tag.toLowerCase(), ch);
        }
    }

    /**
     * チャンネル設定を更新する
     * @param chconf_ 
     */
    public void updateChannelConf(ChannelConf chconf_) {
        Channel ch = chlist.get(chconf_.id);
        if (ch != null) {
            // アップデート時はTAG検索用テーブルとID検索テーブル両方に更新をかける(idは主にDBのレコード検索用)
            if (chconf.containsKey(chconf_.id)) {
                chconf.replace(chconf_.id, chconf_);
            } else {
                chconf.put(chconf_.id, chconf_);
            }
            if (map_chconf.containsKey(ch.tag.toLowerCase())) {
                map_chconf.replace(ch.tag.toLowerCase(), chconf_);
            } else {
                map_chconf.put(ch.tag.toLowerCase(), chconf_);
            }
        } else {
            log.log(Level.WARNING, "Unknown ChannelConf update.[{0}]", chconf_.id);
        }
    }

    /**
     * チャンネルパスワードを更新する
     * @param chps 
     */
    public void updateChannelPassword(ChannelPassword chps) {
        // アップデート時はTAG検索用テーブルとID検索テーブル両方に更新をかける(idは主にDBのレコード検索用)
        Channel ch = chlist.get(chps.id);
        if (ch != null) {
            if (chpass.containsKey(chps.id)) {
                chpass.replace(chps.id, chps);
            } else {
                chpass.put(chps.id, chps);
            }
            if (map_chpass.containsKey(ch.tag.toLowerCase())) {
                map_chpass.replace(ch.tag.toLowerCase(), chps);
            } else {
                map_chpass.put(ch.tag.toLowerCase(), chps);
            }
        } else {
            log.log(Level.WARNING, "Unknown ChannelPassword update.[{0}]", chps.id);
        }
    }
    
    /**
     * チャンネルユーザーを更新する
     * @param chus_
     */
    public void updateChannelUser(ChannelUser chus_) {
        Channel ch = chlist.get(chus_.id);
        User us = uslist.get(chus_.userid);
        if ((ch != null) && (us != null)) {
            // id検索用のリストを更新
            ConcurrentHashMap<Integer, ChannelUser> buf;
            if (!chuslist.containsKey(ch.id)) {
                // チャンネルのリストが存在しないので新規作成する
                buf = new ConcurrentHashMap<>();
                chuslist.put(ch.id, buf);
            } else {
                buf = chuslist.get(ch.id);
            }
            if (buf.containsKey(us.id)) {
                buf.replace(us.id, chus_);
            } else {
                buf.put(us.id, chus_);
            }
            
            // tag検索用のリストを更新
            ConcurrentHashMap<UUID, ChannelUser> tagchus;
            if (!map_chuser.containsKey(ch.tag.toLowerCase())) {
                tagchus = new ConcurrentHashMap<>();
                map_chuser.put(ch.tag.toLowerCase(), tagchus);
            } else {
                tagchus = map_chuser.get(ch.tag.toLowerCase());
            }
            if (tagchus.containsKey(us.uuid)) {
                tagchus.replace(us.uuid, chus_);
            } else {
                tagchus.put(us.uuid, chus_);
            }

            // ユーザーUUID主キーのリストの更新
            // UserID主キー
            ConcurrentHashMap<Integer, ChannelUser> buf_us;
            if (!chuslist_us.containsKey(us.id)) {
                // チャンネルのリストが存在しないので新規作成する
                buf_us = new ConcurrentHashMap<>();
                chuslist_us.put(us.id, buf_us);
            } else {
                buf_us = chuslist_us.get(us.id);
            }
            if (buf_us.containsKey(ch.id)) {
                buf_us.replace(ch.id, chus_);
            } else {
                buf_us.put(ch.id, chus_);
            }
            
            // UserUUID主キー
            ConcurrentHashMap<String, ChannelUser> tagchus_us;
            if (!map_chuser_us.containsKey(us.uuid)) {
                tagchus_us = new ConcurrentHashMap<>();
                map_chuser_us.put(us.uuid, tagchus_us);
            } else {
                tagchus_us = map_chuser_us.get(us.uuid);
            }
            if (tagchus_us.containsKey(ch.tag.toLowerCase())) {
                tagchus_us.replace(ch.tag.toLowerCase(), chus_);
            } else {
                tagchus_us.put(ch.tag.toLowerCase(), chus_);
            }
            
        } else {
            log.log(Level.WARNING, "Unknown ChannelUser updata ch[{0}] us[{1}]", new Object[]{chus_.id, chus_.userid});
        }
    }

    /**
     * チャンネルユーザーを更新する
     * @param pl
     * @param chuser 
     */
    public void updateChannelUser(Player pl, ConcurrentHashMap<Integer, ChannelUser> chuser) {
        // 加入しているチャンネルごとにループ
        // ロード指定されたら該当ユーザーのデータはすべて置き換えること
        User us = getUser(pl);
        if (us != null) {
            // 全チャンネルで回して、keySetに一致するものは再登録、無いものは削除
            for (int chid : chuser.keySet()) {
// 全件更新用、一旦削除
//            for (int chid : chlist.keySet()) {
                Channel ch = chlist.get(chid);

// 全件更新用、一旦削除
//                // 削除対象のチェック -------------------------------------------
//                // 指定されたリストにないチャンネルはユーザーの情報を削除する
//                if (!chuser.containsKey(ch.id)) {
//                    // chid検索用 --------------------------------------------
//                    // チャンネルの所属情報がなければ何もしない
//                    if (!chuslist.containsKey(ch.id)) continue;
//                    // チャンネルに所属していれば削除する
//                    if (chuslist.get(ch.id).containsKey(us.id)) {
//                        chuslist.get(ch.id).remove(us.id);
//                    }
//                    // tag検索用 ---------------------------------------------
//                    // チャンネルの所属情報がなければ何もしない
//                    if (!map_chuser.containsKey(ch.tag.toLowerCase())) continue;
//                    // チャンネルに所属していれば削除する
//                    if (map_chuser.get(ch.tag.toLowerCase()).containsKey(us.uuid)) {
//                        map_chuser.get(ch.tag.toLowerCase()).remove(us.uuid);
//                    }
//                    // usid検索用 --------------------------------------------
//                    // ユーザーの情報がなければ何もしない
//                    if (!chuslist_us.containsKey(us.id)) continue;
//                    // チャンネルの情報をすべて削除する
//                    else chuslist_us.remove(us.id);
//                    // UUID検索用 --------------------------------------------
//                    // ユーザーの情報がなければ何もしない
//                    if (!map_chuser_us.containsKey(us.uuid)) continue;
//                    // チャンネルの情報をすべて削除する
//                    else map_chuser_us.remove(us.uuid);
//                    continue;
//                }

                // 以降は更新処理 -----------------------------------------------
                ChannelUser data = chuser.get(chid);
                // chid検索用のリストを更新
                ConcurrentHashMap<Integer, ChannelUser> chus;
                if (!chuslist.containsKey(ch.id)) {
                    // チャンネルのリストが存在しないので新規作成する
                    chus = new ConcurrentHashMap<>();
                    chuslist.put(ch.id, chus);
                } else {
                    chus = chuslist.get(ch.id);
                }
                if (chus.containsKey(us.id)) {
                    chus.replace(us.id, data);
                } else {
                    chus.put(us.id, data);
                }
                // tag検索用のリストを更新
                ConcurrentHashMap<UUID, ChannelUser> tagchus;
                if (!map_chuser.containsKey(getChannel(ch.id).tag.toLowerCase())) {
                    tagchus = new ConcurrentHashMap<>();
                    map_chuser.put(getChannel(ch.id).tag.toLowerCase(), tagchus);
                } else {
                    tagchus = map_chuser.get(getChannel(ch.id).tag.toLowerCase());
                }
                if (tagchus.containsKey(pl.getUniqueId())) {
                    tagchus.replace(pl.getUniqueId(), data);
                } else {
                    tagchus.put(pl.getUniqueId(), data);
                }
                
                // ユーザーID主キーのリストも更新しておく
                // id検索用のリストを更新
                ConcurrentHashMap<Integer, ChannelUser> chus_us;
                if (!chuslist_us.containsKey(us.id)) {
                    // チャンネルのリストが存在しないので新規作成する
                    chus_us = new ConcurrentHashMap<>();
                    chuslist_us.put(us.id, chus_us);
                } else {
                    chus_us = chuslist_us.get(us.id);
                }
                if (chus_us.containsKey(ch.id)) {
                    chus_us.replace(ch.id, data);
                } else {
                    chus_us.put(ch.id, data);
                }
                // tag検索用のリストを更新
                ConcurrentHashMap<String, ChannelUser> tagchus_us;
                if (!map_chuser_us.containsKey(us.uuid)) {
                    tagchus_us = new ConcurrentHashMap<>();
                    map_chuser_us.put(us.uuid, tagchus_us);
                } else {
                    tagchus_us = map_chuser_us.get(us.uuid);
                }
                if (tagchus_us.containsKey(ch.tag.toLowerCase())) {
                    tagchus_us.replace(ch.tag.toLowerCase(), data);
                } else {
                    tagchus_us.put(ch.tag.toLowerCase(), data);
                }
                
            }
        } else {
            log.log(Level.WARNING, "Unknown ChannelUser update.[{0}]", pl.getUniqueId().toString());
        }
    }

    /**
     * ユーザーを更新する
     * @param user 
     */
    public void updateUser(User user) {
        // アップデート時はUUID検索用テーブルとID検索テーブル両方に更新をかける(idは主にDBのレコード検索用)
        if (map_user.containsKey(user.uuid)) {
            map_user.replace(user.uuid, user);
        } else {
            map_user.put(user.uuid, user);
        }
        if (uslist.containsKey(user.id)) {
            uslist.replace(user.id, user);
        } else {
            uslist.put(user.id, user);
        }
    }

    /**
     * ユーザーチャンネル設定を更新する
     * @param uschconf_ 
     */
    public void updateUserChannelConf(UserChannelConf uschconf_) {
        // アップデート時はUUID検索用テーブルとID検索テーブル両方に更新をかける(idは主にDBのレコード検索用)
        User us = uslist.get(uschconf_.userid);
        Channel ch = chlist.get(uschconf_.id);
        if ((us != null) && (ch != null)) {
            // id検索用のリストを更新
            ConcurrentHashMap<Integer, UserChannelConf> buf;
            if (!uschconf.containsKey(uschconf_.userid)) {
                // チャンネルのリストが存在しないので新規作成する
                buf = new ConcurrentHashMap<>();
                uschconf.put(uschconf_.userid, buf);
            } else {
                buf = uschconf.get(uschconf_.userid);
            }
            if (buf.containsKey(uschconf_.id)) {
                buf.replace(uschconf_.id, uschconf_);
            } else {
                buf.put(uschconf_.id, uschconf_);
            }
            //chus.put(getPlayerIdByPlayer(pl), chuser.get(chid));
            // tag検索用のリストを更新
            ConcurrentHashMap<String, UserChannelConf> taguschconf;
            if (!map_uschconf.containsKey(us.uuid)) {
                taguschconf = new ConcurrentHashMap<>();
                map_uschconf.put(us.uuid, taguschconf);
            } else {
                taguschconf = map_uschconf.get(us.uuid);
            }
            if (taguschconf.containsKey(ch.tag.toLowerCase())) {
                taguschconf.replace(ch.tag.toLowerCase(), uschconf_);
            } else {
                taguschconf.put(ch.tag.toLowerCase(), uschconf_);
            }
        } else {
            log.log(Level.WARNING, "Unknown UserChannelConf updata us[{0}] ch[{1}]", new Object[]{uschconf_.userid, uschconf_.id});
        }
    }

    /**
     * ユーザーNG設定を更新する
     * @param usng_ 
     */
    public void updateUserNgConf(UserNgConf usng_) {
        // アップデート時はUUID検索用テーブルとID検索テーブル両方に更新をかける(idは主にDBのレコード検索用)
        User us = uslist.get(usng_.userid);
        User target = uslist.get(usng_.target);
        if ((us != null) && (target != null)) {
            // id検索用のリストを更新
            ConcurrentHashMap<Integer, UserNgConf> buf;
            if (!usngconf.containsKey(usng_.userid)) {
                // 対NGユーザーのリストが存在しないので新規作成する
                buf = new ConcurrentHashMap<>();
                usngconf.put(usng_.userid, buf);
            } else {
                buf = usngconf.get(usng_.userid);
            }
            if (buf.containsKey(usng_.target)) {
                buf.replace(usng_.target, usng_);
            } else {
                buf.put(usng_.target, usng_);
            }
            // UUID検索用のリストを更新
            ConcurrentHashMap<UUID, UserNgConf> uuidusng;
            if (!map_usngconf.containsKey(us.uuid)) {
                uuidusng = new ConcurrentHashMap<>();
                map_usngconf.put(us.uuid, uuidusng);
            } else {
                uuidusng = map_usngconf.get(us.uuid);
            }
            if (uuidusng.containsKey(target.uuid)) {
                uuidusng.replace(target.uuid, usng_);
            } else {
                uuidusng.put(target.uuid, usng_);
            }
        } else {
            log.log(Level.WARNING, "Unknown UserNgConf updata us[{0}] target[{1}]", new Object[]{usng_.userid, usng_.target});
        }
    }

    /**
     * 対ユーザー設定を更新する
     * @param usus_ 
     */
    public void updateUserUserConf(UserUserConf usus_) {
        // アップデート時はUUID検索用テーブルとID検索テーブル両方に更新をかける(idは主にDBのレコード検索用)
        User us = uslist.get(usus_.userid);
        User target = uslist.get(usus_.target);
        if ((us != null) && (target != null)) {
            // id検索用のリストを更新
            ConcurrentHashMap<Integer, UserUserConf> buf;
            if (!ususconf.containsKey(usus_.userid)) {
                // 対NGユーザーのリストが存在しないので新規作成する
                buf = new ConcurrentHashMap<>();
                ususconf.put(usus_.userid, buf);
            } else {
                buf = ususconf.get(usus_.userid);
            }
            if (buf.containsKey(usus_.target)) {
                buf.replace(usus_.target, usus_);
            } else {
                buf.put(usus_.target, usus_);
            }
            // UUID検索用のリストを更新
            ConcurrentHashMap<UUID, UserUserConf> uuidusus;
            if (!map_ususconf.containsKey(us.uuid)) {
                uuidusus = new ConcurrentHashMap<>();
                map_ususconf.put(us.uuid, uuidusus);
            } else {
                uuidusus = map_ususconf.get(us.uuid);
            }
            if (uuidusus.containsKey(target.uuid)) {
                uuidusus.replace(target.uuid, usus_);
            } else {
                uuidusus.put(target.uuid, usus_);
            }
        } else {
            log.log(Level.WARNING, "Unknown UserUserConf updata us[{0}] target[{1}]", new Object[]{usus_.userid, usus_.target});
        }
    }
    
    /**
     * Channel削除
     * @param ch 
     */
    public void deleteChannel(Channel ch) {
        if (map_ch.containsKey(ch.tag.toLowerCase())) map_ch.remove(ch.tag.toLowerCase());
        if (chlist.containsKey(ch.id)) chlist.remove(ch.id);
    }
    
    /**
     * ChannelConf削除
     * @param ch 
     */
    public void deleteChannelConf(Channel ch) {
        if (map_chconf.containsKey(ch.tag.toLowerCase())) map_chconf.remove(ch.tag.toLowerCase());
        if (chconf.containsKey(ch.id)) chconf.remove(ch.id);
    }

    /**
     * ChannelPassword削除
     * @param ch 
     */
    public void deleteChannelPassword(Channel ch) {
        if (map_chpass.containsKey(ch.tag.toLowerCase())) map_chpass.remove(ch.tag.toLowerCase());
        if (chpass.containsKey(ch.id)) chpass.remove(ch.id);
    }

    /**
     * ChannelUser(Channel)削除
     * @param ch
     * @param us 
     */
    public void deleteChannelUser_Channel(Channel ch, User us) {
        if (map_chuser.containsKey(ch.tag.toLowerCase())) map_chuser.remove(ch.tag.toLowerCase());
        if (chuslist.containsKey(ch.id)) chuslist.remove(ch.id);
        if (map_chuser_us.containsKey(us.uuid))
            if (map_chuser_us.get(us.uuid).containsKey(ch.tag.toLowerCase())) map_chuser_us.get(us.uuid).remove(ch.tag.toLowerCase());
        if (chuslist_us.containsKey(us.id))
            if (chuslist_us.get(us.id).containsKey(ch.id)) chuslist_us.get(us.id).remove(ch.id);
    }

    /**
     * ChannelUser(User)削除
     * @param ch
     * @param us 
     */
    public void deleteChannelUser_User(Channel ch, User us) {
        if (map_chuser_us.containsKey(us.uuid)) map_chuser_us.remove(us.uuid);
        if (chuslist_us.containsKey(us.id)) chuslist_us.get(us.id);
        if (map_chuser.containsKey(ch.tag.toLowerCase())) 
            if (map_chuser.get(ch.tag.toLowerCase()).containsKey(us.uuid)) map_chuser.get(ch.tag.toLowerCase()).remove(us.uuid);
        if (chuslist.containsKey(ch.id))
            if (chuslist.get(ch.id).containsKey(us.id)) chuslist.get(ch.id).remove(us.id);
    }

    /**
     * User削除
     * @param us
     */
    public void deleteUser(User us) {
        if (map_user.containsKey(us.uuid))
            map_user.remove(us.uuid);
        if (uslist.containsKey(us.id))
            uslist.remove(us.id);
    }

    /**
     * UserChannelConf削除
     * @param us
     * @param ch 
     */
    public void deleteUserChannelConf(User us, Channel ch) {
        if (map_uschconf.containsKey(us.uuid))
            if (map_uschconf.get(us.uuid).containsKey(ch.tag.toLowerCase())) map_uschconf.get(us.uuid).remove(ch.tag.toLowerCase());
        if (uschconf.containsKey(us.id))
            if (uschconf.get(us.id).containsKey(ch.id)) uschconf.get(us.id).remove(ch.id);
    }

    /**
     * UserNgConf削除
     * @param us
     * @param target
     */
    public void deleteUserNgConf(User us, User target) {
        if (map_usngconf.containsKey(us.uuid))
            if (map_usngconf.get(us.uuid).containsKey(target.uuid)) map_usngconf.get(us.uuid).remove(target.uuid);
        if (usngconf.containsKey(us.id))
            if (usngconf.get(us.id).containsKey(target.id)) usngconf.get(us.id).remove(target.id);
    }

    /**
     * UserNgConf削除
     * @param us
     */
    public void deleteUserNgConf(User us) {
        if (map_usngconf.containsKey(us.uuid))
            map_usngconf.remove(us.uuid);
        if (usngconf.containsKey(us.id))
            usngconf.remove(us.id);
    }

    /**
     * UserUserConf削除
     * @param us
     * @param target
     */
    public void deleteUserUserConf(User us, User target) {
        if (map_ususconf.containsKey(us.uuid))
            if (map_ususconf.get(us.uuid).containsKey(target.uuid)) map_ususconf.get(us.uuid).remove(target.uuid);
        if (ususconf.containsKey(us.id))
            if (ususconf.get(us.id).containsKey(target.id)) ususconf.get(us.id).remove(target.id);
    }

    /**
     * UserUserConf削除
     * @param us
     */
    public void deleteUserUserConf(User us) {
        if (map_ususconf.containsKey(us.uuid))
            map_ususconf.remove(us.uuid);
        if (ususconf.containsKey(us.id))
            ususconf.remove(us.id);
    }
}
