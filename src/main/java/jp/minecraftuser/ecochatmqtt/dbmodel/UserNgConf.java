
package jp.minecraftuser.ecochatmqtt.dbmodel;

/**
 *
 * @author ecolight
 */
public class UserNgConf {
    public int userid;  // UNIQUE(userid, target) 
    public int target;      
    public boolean ng;  // NGãã©ãã
    
    public UserNgConf (
            int userid_,
            int target_,
            boolean ng_
    ) {
        userid = userid_;
        target = target_;
        ng = ng_;
    }
}
