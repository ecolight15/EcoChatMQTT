
package jp.minecraftuser.ecochatmqtt.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecoframework.DatabaseFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.db.CTYPE;

/**
 *
 * @author ecolight
 */
public class EcoChatDB extends DatabaseFrame {

    public EcoChatDB(PluginFrame plg_, String dbname_, String name_) throws ClassNotFoundException, SQLException {
        super(plg_, dbname_, name_);
    }

    public EcoChatDB(PluginFrame plg_, String addr_, String user_, String pass_, String dbname_, String name_) throws ClassNotFoundException, SQLException {
        super(plg_, addr_, user_, pass_, dbname_, name_);
    }


    /**
     * データベース移行処理
     * 内部処理からトランザクション開始済みの状態で呼ばれる
     * @throws SQLException
     */
    @Override
    protected void migrationData(Connection con) throws SQLException {
        // version 1 の場合、新規作成もしくは旧バージョンのデータベース引き継ぎの場合を検討する
        if (dbversion == 1) {
            if (justCreated) {
                // 新規作成の場合、テーブル定義のみ作成して終わり
                MessageFormat mf = new MessageFormat(
                          "CREATE TABLE IF NOT EXISTS CHANNEL("
                        + "ID {0} PRIMARY KEY {1},"
                        + " TAG {2} NOT NULL UNIQUE,"
                        + " NAME {3} NOT NULL UNIQUE,"
                        + " TYPE {4} DEFAULT " + Channel.Type.getByName(conf.getString("ChannelDefault.Type")).getInt() + ","
                        + " ENTERMSG {5} DEFAULT ''"+ conf.getString("ChannelDefault.EnterMessage").replace("{", "'{").replace("}", "'}") +"'',"
                        + " LEAVEMSG {6} DEFAULT ''"+ conf.getString("ChannelDefault.LeaveMessage").replace("{", "'{").replace("}", "'}") +"'',"
                        + " WELCOMEMSG {7} DEFAULT ''"+ conf.getString("ChannelDefault.WelcomeMessage").replace("{", "'{").replace("}", "'}") +"'',"
                        + " GOODBYEMSG {8} DEFAULT ''"+ conf.getString("ChannelDefault.GoodbyeMessage").replace("{", "'{").replace("}", "'}") +"'',"
                        + " AUTOJOIN {9} DEFAULT "+ (conf.getBoolean("ChannelDefault.AutoJoin") ? 1 : 0) +","
                        + " LISTED {10} DEFAULT "+ (conf.getBoolean("ChannelDefault.ListEnabled") ? 1 : 0) +","
                        + " ADDPERM {11} DEFAULT "+ (conf.getBoolean("ChannelDefault.AddReqPerm") ? 1 : 0) +","
                        + " ACTIVATE {12} DEFAULT "+ (conf.getBoolean("ChannelDefault.Activate") ? 1 : 0) +","
                        + " SINCE {13} DEFAULT 0);");
                try {
                    log.log(Level.INFO, "[CHANNEL SQL]:" + mf.format(new String[]{
                        CTYPE.LONG.get(jdbc), CTYPE.AUTOINCREMENT.get(jdbc), CTYPE.STRING_KEY.get(jdbc), CTYPE.STRING_KEY.get(jdbc), CTYPE.LONG.get(jdbc),
                        CTYPE.STRING_KEY.get(jdbc), CTYPE.STRING_KEY.get(jdbc), CTYPE.STRING_KEY.get(jdbc), CTYPE.STRING_KEY.get(jdbc),
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                    executeStatement(con, mf.format(new String[]{
                        CTYPE.LONG.get(jdbc), CTYPE.AUTOINCREMENT.get(jdbc), CTYPE.STRING_KEY.get(jdbc), CTYPE.STRING_KEY.get(jdbc), CTYPE.LONG.get(jdbc),
                        CTYPE.STRING_KEY.get(jdbc), CTYPE.STRING_KEY.get(jdbc), CTYPE.STRING_KEY.get(jdbc), CTYPE.STRING_KEY.get(jdbc),
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[CHANNEL].");
                    Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, e);
                }
                log.info("DataBase CHANNEL table checked.");
                //--------------------------------------------------------------
                mf = new MessageFormat("CREATE TABLE IF NOT EXISTS CHCONF("
                        + "ID {0} PRIMARY KEY, COLOR {1} DEFAULT ''WHITE'', BOLD {2} DEFAULT 0, ITALIC {3} DEFAULT 0, LINE {4} DEFAULT 0, STRIKE {5} DEFAULT 0, FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE);");
                try {
                    log.log(Level.INFO, "[CHCONF SQL]:" + mf.format(new String[]{
                        CTYPE.LONG.get(jdbc), CTYPE.STRING_KEY.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                    executeStatement(con, mf.format(new String[]{
                        CTYPE.LONG.get(jdbc), CTYPE.STRING_KEY.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[CHCONF].");
                    Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, e);
                }
                log.info("DataBase CHCONF table checked.");
                //--------------------------------------------------------------
                mf = new MessageFormat("CREATE TABLE IF NOT EXISTS USERS("
                        + "USERID {0} PRIMARY KEY {1}, MOSTUUID {2} NOT NULL, LEASTUUID {3} NOT NULL, "
                        + "ACTIVE {4} NOT NULL, MUTE {5} DEFAULT 0, LOCAL {6} DEFAULT 50, INFO {7} DEFAULT 1, "
                        + "NGVIEW {8} DEFAULT 0, LRANGE {9} DEFAULT 1, RSWARN {10} DEFAULT 1, SPYCHAT {11} DEFAULT 0, "
                        + "SPYPM {12} DEFAULT 0, UNIQUE(MOSTUUID,LEASTUUID));");
                try {
                    log.log(Level.INFO, "[USERS SQL]:" + mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.AUTOINCREMENT.get(jdbc),
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc),
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc),
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                    executeStatement(con, mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.AUTOINCREMENT.get(jdbc),
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc),
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc),
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[USERS].");
                    Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, e);
                }
                log.info("DataBase USERS table checked.");
                //--------------------------------------------------------------
                mf = new MessageFormat("CREATE TABLE IF NOT EXISTS CHUSERS("
                        + "ID {0} NOT NULL, USERID {1} NOT NULL, OWNER {2} DEFAULT 0, JOINDATE {3} NOT NULL, UNIQUE(ID,USERID), FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE, FOREIGN KEY(USERID) REFERENCES USERS(USERID) ON DELETE CASCADE);");
                try {
                    log.log(Level.INFO, "[CHUSERS SQL]:" + mf.format(new String[]{
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                    executeStatement(con, mf.format(new String[]{
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[CHUSERS].");
                    Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, e);
                }
                log.info("DataBase CHUSERS table checked.");
                //--------------------------------------------------------------
                mf = new MessageFormat("CREATE TABLE IF NOT EXISTS CHPASS("
                        + "ID {0} NOT NULL, PASS {1}, FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE);");
                try {
                    log.log(Level.INFO, "[CHPASS SQL]:" + mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.STRING_KEY.get(jdbc)}));
                    executeStatement(con, mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.STRING_KEY.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[CHPASS].");
                    Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, e);
                }
                log.info("DataBase CHPASS table checked.");
                //--------------------------------------------------------------
                mf = new MessageFormat("CREATE TABLE IF NOT EXISTS USCHCONF("
                        + "USERID {0} NOT NULL, ID {1} NOT NULL, COLOR {2} DEFAULT ''WHITE'', BOLD {3} DEFAULT 0, ITALIC {4} DEFAULT 0, LINE {5} DEFAULT 0, STRIKE {6} DEFAULT 0, UNIQUE(USERID,ID), FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE, FOREIGN KEY(USERID) REFERENCES USERS(USERID) ON DELETE CASCADE);");
                try {
                    log.log(Level.INFO, "[USCHCONF SQL]:" + mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING_KEY.get(jdbc),
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                    executeStatement(con, mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING_KEY.get(jdbc),
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[USCHCONF].");
                    Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, e);
                }
                log.info("DataBase USCHCONF table checked.");
                //--------------------------------------------------------------
                mf = new MessageFormat("CREATE TABLE IF NOT EXISTS USUSCONF("
                        + "USERID {0} NOT NULL, TARGET {1} NOT NULL, COLOR {2} DEFAULT ''WHITE'', BOLD {3} DEFAULT 0, ITALIC {4} DEFAULT 0, LINE {5} DEFAULT 0, STRIKE {6} DEFAULT 0, UNIQUE(USERID,TARGET), FOREIGN KEY(USERID) REFERENCES USERS(USERID) ON DELETE CASCADE, FOREIGN KEY(TARGET) REFERENCES USERS(USERID) ON DELETE CASCADE);");
                try {
                    log.log(Level.INFO, "[USUSCONF SQL]:" + mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING_KEY.get(jdbc),
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                    executeStatement(con, mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.STRING_KEY.get(jdbc),
                        CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[USUSCONF].");
                    Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, e);
                }
                log.info("DataBase USUSCONF table checked.");
                //--------------------------------------------------------------
                mf = new MessageFormat("CREATE TABLE IF NOT EXISTS USNGCONF("
                        + "USERID {0} NOT NULL, TARGET {1} NOT NULL, NG {2} DEFAULT 0, UNIQUE(USERID,TARGET), FOREIGN KEY(USERID) REFERENCES USERS(USERID) ON DELETE CASCADE, FOREIGN KEY(TARGET) REFERENCES USERS(USERID) ON DELETE CASCADE);");
                try {
                    log.log(Level.INFO, "[USNGCONF SQL]:" + mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                    executeStatement(con, mf.format(new String[]{CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc), CTYPE.LONG.get(jdbc)}));
                } catch (Exception e) {
                    log.log(Level.INFO, "Error create table[USNGCONF].");
                    Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, e);
                }
                log.info("DataBase USNGCONF table checked.");
                //--------------------------------------------------------------
                // データベースバージョンは最新版数に設定する
                log.info("create " + name + " version 2");
                updateSettingsVersion(con, 2);
                return;
            } else {
 
                //-----------------------------------------------------------------------------------
                // データベースバージョンは次版にする
                //-----------------------------------------------------------------------------------
                updateSettingsVersion(con);
                
                log.info(plg.getName() + " database migration " + name + " version 1 -> 2 completed.");
            }
        }
    }

    
}
