
package jp.minecraftuser.ecochatmqtt.timer;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelUser;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.mqttmodel.ConfigJson;
import jp.minecraftuser.ecoframework.async.*;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.command.CommandSender;

/**
 * メインスレッドと非同期スレッド間のデータ送受用クラス(メッセージ送受用)
 * @author ecolight
 */
public class ChatPayload extends PayloadFrame {
    private final PluginFrame plg;
    public boolean result = false;
    public Type type = Type.NONE;       // 非同期タスク種別
    public Type reloadtype;             // 非同期タスクリトライ用種別退避
    public int retry = 0;               // リトライカウント
    public boolean broadcast = false;   // ブロードキャスト指定
    public boolean broadcast_target = false;   // ブロードキャスト(UUID)指定
    public UUID broadcast_target_uuid = null;  // UUID
    public boolean confirm = false;     // confirm外部確認用
    public boolean execute_confirm = false; // confirm外部確認用(メインスレッドへの通知用)
    
    // 文章
    public String message = "";     // エラーメッセージ、チャット本文など
    public ArrayList<String> message_list;
    
    // パラメタ
    public String[] param;  // コマンドパラメタ渡し用
    public String channelTag; // チャンネル指定用
    
    // データ伝達用
    public Channel channel = null; // チャンネル情報渡し用
    public User user = null;
    public ConcurrentHashMap<Integer, ChannelUser> chuslist;

    // コンフィグ更新固有
    public ConfigJson.Type configType;
    public int usid = 0;
    public int chid = 0;
    public int targetid = 0;
    
    // 単一プレイヤー用
    public CommandSender player;

    // 複数プレイヤー用
    public CommandSender[] players;

    // 処理種別を追加した場合、AsyncSaveLoadTimer の initTask に処理クラスを登録すること
    public enum Type {
        NONE,
        CREATE,
        LOAD,
        RESET,
        CHAT,
        CHAT_RECEIVE,
        CONFIG,
        CONFIG_RECEIVE,
        //CMD_ACTIVE,
        CMD_ADM_SPYCHAT,
        CMD_ADM_SPYPM,
        CMD_ADD,
        CMD_CHANNEL,
        CMD_CHANNEL_NAME,
        CMD_CHANNEL_TYPE,
        CMD_CHANNEL_COLOR,
        CMD_CHANNEL_DEF,
        CMD_CHANNEL_LIST,
        CMD_CHANNEL_PASSENABLE,
        CMD_CHANNEL_JOIN,
        CMD_CHANNEL_LEAVE,
        CMD_CHANNEL_WELCOME,
        CMD_CHANNEL_GOODBYE,
        CMD_CHANNEL_OWNER,
        CMD_CHANNEL_TAG,
        CMD_CHANNEL_PERM,
        CMD_CONF,
        CMD_CONF_CHANNEL,
        CMD_CONF_PLAYER,
        CMD_CONF_NGPLAYER,
        CMD_CONF_FLAG_INFO,
        CMD_CONF_FLAG_NGUSER,
        CMD_CONF_FLAG_RANGE,
        CMD_CONF_RANGE_LOCAL,
        CMD_CONF_FLAG_RS,
        CMD_CONF_MUTE,
        CMD_DELETE,
        CMD_DICE,
        CMD_HISTORY,
        CMD_INFO,
        CMD_JOIN,
        CMD_KICK,
        CMD_LEAVE,
        CMD_LIST,
        CMD_PASSJOIN,
        CMD_PM,
        CMD_PMHISTORY,
        CMD_RS,
        CMD_SET,
        CMD_WHO,
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス(ただし通信に用いられる可能性を念頭に一定以上の情報は保持しない)
     * @param type_
     */
    public ChatPayload(PluginFrame plg_, Type type_) {
        super(plg_);
        plg = plg_;
        this.type = type_;
        this.message_list = new ArrayList<>();
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス(ただし通信に用いられる可能性を念頭に一定以上の情報は保持しない)
     * @param player_
     * @param type_
     */
    public ChatPayload(PluginFrame plg_, CommandSender player_, Type type_) {
        super(plg_);
        plg = plg_;
        this.type = type_;
        player = player_;
        this.message_list = new ArrayList<>();
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス(ただし通信に用いられる可能性を念頭に一定以上の情報は保持しない)
     * @param uuid_
     * @param type_
     */
    public ChatPayload(PluginFrame plg_, UUID uuid_, Type type_) {
        super(plg_);
        plg = plg_;
        this.type = type_;
        broadcast_target = true;
        broadcast_target_uuid = uuid_;
        this.message_list = new ArrayList<>();
    }
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス(ただし通信に用いられる可能性を念頭に一定以上の情報は保持しない)
     * @param players_
     * @param type_
     */
    public ChatPayload(PluginFrame plg_, CommandSender[] players_, Type type_) {
        super(plg_);
        plg = plg_;
        this.type = type_;
        players = players_;
        this.message_list = new ArrayList<>();
    }

    /**
     * コマンドの再キュー指定を子スレッドから親スレッドに依頼する
     */
    public void request_reset() {
        reloadtype = type;
        type = Type.RESET;
    }
    
    /**
     * 依頼されたリセットを実行
     */
    public void reset() {
        type = reloadtype;
    }
    
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder("PlayerDataPayload->");
//        if (player != null) {
//            sb.append("[").append(player.getName()).append("]");
//        }
//        if (pic != null) {
//            if (pic.inv != null) sb.append(" inv:").append(pic.inv);
//            if (pic.ender != null) sb.append(" ender:").append(pic.ender);
//        }
//        return sb.toString();
//    }
}
