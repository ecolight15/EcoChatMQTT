
package jp.minecraftuser.ecochatmqtt.timer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskBase;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskChat;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskChatReceive;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdAdd;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdAdmSpyChat;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdAdmSpyPM;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdChannel;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdChannelColor;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdChannelDef;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdChannelGoodbye;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdChannelJoin;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdChannelLeave;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdChannelList;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdChannelName;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdChannelOwner;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdChannelPassEnable;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdChannelPerm;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdChannelTag;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdChannelType;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdChannelWelcome;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdConf;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdConfChannel;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdConfFlagInfo;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdConfFlagNgUser;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdConfFlagRS;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdConfFlagRange;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdConfMute;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdConfNgPlayer;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdConfPlayer;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdConfRangeLocal;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdDelete;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdDice;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdHistory;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdInfo;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdJoin;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdKick;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdLeave;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdList;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdPM;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdPMHistory;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdPassJoin;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdRS;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdSet;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCmdWho;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskConfig;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskConfigReceive;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskCreate;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskLoad;
import jp.minecraftuser.ecochatmqtt.timer.task.AsyncChatTaskReset;
import jp.minecraftuser.ecoframework.async.*;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;

/**
 * ????????????????????????????????????????????????
 * @author ecolight
 */
public class AsyncWorker extends AsyncProcessFrame {
    // ??????????????????????????????????????????
    HashMap<ChatPayload.Type, AsyncChatTaskBase> tasktable;
    // ??????????????????????????????????????????????????????????????????????????????????????????????????????
    private static AsyncWorker instance = null;
    public static final AsyncWorker getInstance(PluginFrame plg_, String name_) {
        if (instance == null) {
            instance = new AsyncWorker(plg_, name_);
        }
        return instance;
    }

    /**
     * ???????????????????????????????????????
     * @param plg_ ?????????????????????????????????????????????
     * @param name_ ??????
     */
    public AsyncWorker(PluginFrame plg_, String name_) {
        super(plg_, name_);
        initTask();
    }

    /**
     * ???????????????????????????????????????
     * @param plg_ ?????????????????????????????????????????????
     * @param name_ ??????
     * @param frame_ ??????????????????????????????
     */
    public AsyncWorker(PluginFrame plg_, String name_, AsyncFrame frame_) {
        super(plg_, name_, frame_);
        initTask();
    }
    
    /**
     * ?????????????????????????????????????????????????????????
     * ???????????????????????????????????????????????????????????????????????????
     */
    private void initTask() {
        tasktable = new HashMap<>();
        tasktable.put(ChatPayload.Type.RESET, AsyncChatTaskReset.getInstance(plg));
        tasktable.put(ChatPayload.Type.CREATE, AsyncChatTaskCreate.getInstance(plg));
        tasktable.put(ChatPayload.Type.LOAD, AsyncChatTaskLoad.getInstance(plg));
        tasktable.put(ChatPayload.Type.CHAT, AsyncChatTaskChat.getInstance(plg));
        tasktable.put(ChatPayload.Type.CHAT_RECEIVE, AsyncChatTaskChatReceive.getInstance(plg));
        tasktable.put(ChatPayload.Type.CONFIG, AsyncChatTaskConfig.getInstance(plg));
        tasktable.put(ChatPayload.Type.CONFIG_RECEIVE, AsyncChatTaskConfigReceive.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_ADM_SPYCHAT, AsyncChatTaskCmdAdmSpyChat.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_ADM_SPYPM, AsyncChatTaskCmdAdmSpyPM.getInstance(plg));
        //tasktable.put(ChatPayload.Type.CMD_ACTIVE, AsyncChatTaskCmdActive.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_ADD, AsyncChatTaskCmdAdd.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CHANNEL, AsyncChatTaskCmdChannel.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CHANNEL_NAME, AsyncChatTaskCmdChannelName.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CHANNEL_TYPE, AsyncChatTaskCmdChannelType.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CHANNEL_COLOR, AsyncChatTaskCmdChannelColor.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CHANNEL_DEF, AsyncChatTaskCmdChannelDef.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CHANNEL_LIST, AsyncChatTaskCmdChannelList.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CHANNEL_PASSENABLE, AsyncChatTaskCmdChannelPassEnable.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CHANNEL_JOIN, AsyncChatTaskCmdChannelJoin.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CHANNEL_LEAVE, AsyncChatTaskCmdChannelLeave.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CHANNEL_WELCOME, AsyncChatTaskCmdChannelWelcome.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CHANNEL_GOODBYE, AsyncChatTaskCmdChannelGoodbye.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CHANNEL_OWNER, AsyncChatTaskCmdChannelOwner.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CHANNEL_TAG, AsyncChatTaskCmdChannelTag.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CHANNEL_PERM, AsyncChatTaskCmdChannelPerm.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CONF, AsyncChatTaskCmdConf.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CONF_CHANNEL, AsyncChatTaskCmdConfChannel.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CONF_PLAYER, AsyncChatTaskCmdConfPlayer.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CONF_NGPLAYER, AsyncChatTaskCmdConfNgPlayer.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CONF_FLAG_INFO, AsyncChatTaskCmdConfFlagInfo.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CONF_FLAG_NGUSER, AsyncChatTaskCmdConfFlagNgUser.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CONF_FLAG_RANGE, AsyncChatTaskCmdConfFlagRange.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CONF_RANGE_LOCAL, AsyncChatTaskCmdConfRangeLocal.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CONF_FLAG_RS, AsyncChatTaskCmdConfFlagRS.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_CONF_MUTE, AsyncChatTaskCmdConfMute.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_DELETE, AsyncChatTaskCmdDelete.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_DICE, AsyncChatTaskCmdDice.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_HISTORY, AsyncChatTaskCmdHistory.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_INFO, AsyncChatTaskCmdInfo.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_JOIN, AsyncChatTaskCmdJoin.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_KICK, AsyncChatTaskCmdKick.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_LEAVE, AsyncChatTaskCmdLeave.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_LIST, AsyncChatTaskCmdList.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_PASSJOIN, AsyncChatTaskCmdPassJoin.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_PM, AsyncChatTaskCmdPM.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_PMHISTORY, AsyncChatTaskCmdPMHistory.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_RS, AsyncChatTaskCmdRS.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_SET, AsyncChatTaskCmdSet.getInstance(plg));
        tasktable.put(ChatPayload.Type.CMD_WHO, AsyncChatTaskCmdWho.getInstance(plg));
    }
    
    /**
     * ??????????????????????????????????????????????????????
     * @throws InterruptedException 
     */
    public synchronized void timerWait() throws InterruptedException {
        log.log(Level.INFO, "Wait for thread stop.");
        wait();
        log.log(Level.INFO, "Detect thread stop.");
    }

    /**
     * ??????????????????????????????????????? 
     */
    public synchronized void timerStop() {
        log.log(Level.INFO, "Notify thread stop.");
        notifyAll();
        log.log(Level.INFO, "Call thread cancel.");
        cancel();
    }

    /**
     * ?????????????????????????????????????????????????????????
     */
    public void stop() {
        ((AsyncWorker) parentFrame).timerStop();
    }
    
    /**
     * Data??????????????????????????????
     * @param data_ ?????????????????????????????????
     */
    @Override
    protected void executeProcess(PayloadFrame data_) {
        ChatPayload data = (ChatPayload) data_;
        EcoChatDB db = (EcoChatDB) plg.getDB("chat");
        Connection con;
        if (data.type != null) log.info("type:" + data.type.name());
        log.info("retry:" + data.retry);
        log.info("result:" + data.result);
        if (data.channelTag != null) log.info("tag:" + data.channelTag);
        if (data.message != null) log.info("message:" + data.message);
        if (data.reloadtype != null) log.info("reloadtype:" + data.reloadtype);
        
        try {
            con = db.connect();
        } catch (SQLException ex) {
            Logger.getLogger(AsyncWorker.class.getName()).log(Level.SEVERE, null, ex);
            // ?????????????????????
            data.result = false;
            receiveData(data);
            return;
        }
        
        try {
            if (tasktable.containsKey(data.type)) {
                tasktable.get(data.type).asyncThread(this, db, con, data);
            } else {
                log.log(Level.SEVERE, "reject unknown payload");
            }
        } catch (SQLException e) {
            // ?????????????????????????????????????????????????????????
            Logger.getLogger(AsyncWorker.class.getName()).log(Level.SEVERE, null, e);
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(AsyncWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                Logger.getLogger(AsyncWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (data.retry < 5) {
                // DB???????????????????????????
                data.request_reset();
                data.retry++;
            } else {
                // ????????????????????????
                data.message = "Database ???????????????????????????";
                log.warning("DB??????????????????????????????");
            }
            data.result = false;
        } catch (Exception e) {
            log.warning("???????????????");
            Logger.getLogger(AsyncWorker.class.getName()).log(Level.SEVERE, null, e);
            Utl.sendPluginMessage(plg, player, "?????????????????????????????????");
            try {
                con.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(AsyncWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
            data.result = false;
        }

        if (data.result == true) {
            try {
                con.commit();
            } catch (SQLException e) {
                Logger.getLogger(AsyncWorker.class.getName()).log(Level.SEVERE, null, e);
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    Logger.getLogger(AsyncWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ex) {
                    Logger.getLogger(AsyncWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (data.retry < 5) {
                    data.retry++;
                    // DB???????????????????????????
                    data.request_reset();
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(AsyncWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    // ????????????????????????
                    data.message = "Database ???????????????????????????";
                }
            }
        }
        
        try {
            con.close();
        } catch (SQLException e) {
            Logger.getLogger(AsyncWorker.class.getName()).log(Level.SEVERE, null, e);
        }
        
        // ?????????????????????
        receiveData(data);
    }

    /**
     * Data?????????????????????????????????
     * @param data_ ?????????????????????????????????
     */
    @Override
    protected void executeReceive(PayloadFrame data_) {
        ChatPayload data = (ChatPayload) data_;
        if (data.type != null) {
            tasktable.get(data.type).mainThread(this, data);
        }
    }

    /**
     * ????????????????????????????????????????????????????????????
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????
     * synchronized?????????????????????????????????????????????????????????????????????
     * @return AsyncFrame????????????????????????????????????
     */
    @Override
    protected AsyncFrame clone() {
        return new AsyncWorker(plg, name, this);
    }
  
}
