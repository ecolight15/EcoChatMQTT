
package jp.minecraftuser.ecochatmqtt.dbmodel;

import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import jp.minecraftuser.ecochatmqtt.EcoChatMQTT;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author ecolight
 */
public class Channel {
    public int id;                  // IDプライマリキー
    public String tag;              // TAG(DBは大文字小文字保持して格納するが、SELECTなどでは大文字小文字判別しないようにする)
    public String name;             // 
    public Type type;               // チャンネルのタイプ
    public String enterMessage;     // JOIN時メッセージ
    public String leaveMessage;     // LEAVE時メッセージ
    public String welcomeMessage;   // JOIN時メッセージ
    public String goodbyeMessage;   // LEAVE時メッセージ
    public boolean autoJoin;        // アクティブチャンネルをデフォルト加入チャンネルに変更する(デフォルト非加入)
    public boolean listed;          // チャンネル一覧に表示するかどうか
    public boolean addPerm;         // addコマンドによる他ユーザ追加を一般ユーザーに公開/非公開設定する
    public boolean activate;        // 有効状態かどうか
    public long since;              // できればBIGINT (
    
    public enum Type {
        global(1, "global"),
        world(2, "world"),
        local(3, "local"),
        ;
        private final int num;
        private final String name;
        private Type(final int num_, final String name_) {
            this.num = num_;
            this.name = name_;
        }
        public int getInt() {
            return this.num;
        }
        public String getName() {
            return this.name;
        }
        public static Type getByNum(int num_) {
            for (Type t : Type.values()) {
                if (t.getInt() == num_) {
                    return t;
                }
            }
            return null;
        }
        public static Type getByName(String name_) {
            for (Type t : Type.values()) {
                if (t.getName().equalsIgnoreCase(name_)) {
                    return t;
                }
            }
            return null;
        }
    }
    public Channel(
            int id_,
            String tag_,
            String name_,
            Type type_,
            String enterMessage_,
            String leaveMessage_,
            String welcomeMessage_,
            String goodbyeMessage_,
            boolean autoJoin_,
            boolean listed_,
            boolean addPerm_,
            boolean activate_,
            long since_
    ) {
        id = id_;
        tag = tag_;
        name = name_;
        type = type_;
        enterMessage = enterMessage_;
        leaveMessage = leaveMessage_;
        welcomeMessage = welcomeMessage_;
        goodbyeMessage = goodbyeMessage_;
        autoJoin = autoJoin_;
        listed = listed_;
        addPerm = addPerm_;
        activate = activate_;
        since = since_;
        registerIgnoreTagName();
    }
    
    private static ArrayList<String> ignoreList;
    private static void registerIgnoreTagName() {
        if (ignoreList == null) ignoreList = new ArrayList<>();
        ignoreList.add("");
        ignoreList.add("a");
        ignoreList.add("b");
        ignoreList.add("c");
        ignoreList.add("d");
        ignoreList.add("e");
        ignoreList.add("f");
        ignoreList.add("g");
        ignoreList.add("h");
        ignoreList.add("i");
        ignoreList.add("j");
        ignoreList.add("k");
        ignoreList.add("l");
        ignoreList.add("m");
        ignoreList.add("n");
        ignoreList.add("o");
        ignoreList.add("p");
        ignoreList.add("q");
        ignoreList.add("r");
        ignoreList.add("s");
        ignoreList.add("t");
        ignoreList.add("u");
        ignoreList.add("v");
        ignoreList.add("w");
        ignoreList.add("x");
        ignoreList.add("y");
        ignoreList.add("z");
        ignoreList.add("add");
        ignoreList.add("channel");
        ignoreList.add("conf");
        ignoreList.add("delete");
        ignoreList.add("dice");
        ignoreList.add("history");
        ignoreList.add("join");
        ignoreList.add("leave");
        ignoreList.add("passjoin");
        ignoreList.add("pm");
        ignoreList.add("pmhistory");
        ignoreList.add("rs");
        ignoreList.add("set");
        ignoreList.add("spychat");
        ignoreList.add("spypm");
        ignoreList.add("info");
        ignoreList.add("kick");
        ignoreList.add("list");
        ignoreList.add("who");
    }
    public static boolean checkIgnoreTagName(String tag) {
        return (ignoreList.contains(tag.toLowerCase()));
    }

    /**
     * チャンネル情報を埋め込むフォーマット変換(MessageFormat向けエスケープ済み)
     * @param msg
     * @param sender
     * @return 
     */
    public String convertChannelInfoString(String msg, CommandSender sender) {
        msg = msg.replace("$", "\\$");
        if (sender != null) {
            msg = msg.replaceAll("(?i)" + "\\{player\\}", Matcher.quoteReplacement(sender.getName()));
        } else {
            msg = msg.replaceAll("(?i)" + "\\{player\\}", "");
        }
        msg = msg.replaceAll("(?i)" + "\\{tag\\}", Matcher.quoteReplacement(tag));
        msg = msg.replaceAll("(?i)" + "\\{name\\}", Matcher.quoteReplacement(name));
        msg = msg.replaceAll("(?i)" + "\\{type\\}", Matcher.quoteReplacement(type.getName()));
        msg = msg.replace("\\$", "$");
        return msg;
    }

    /**
     * enterMessageの埋め込み文字列を処理後に返却
     * @param sender
     * @return 
     */
    public String getEnterMessage(CommandSender sender) {
        return convertChannelInfoString(enterMessage, sender);
    }

    /**
     * leaveMessageの埋め込み文字列を処理後に返却
     * @param sender
     * @return 
     */
    public String getLeaveMessage(CommandSender sender) {
        return convertChannelInfoString(leaveMessage, sender);
    }

    /**
     * welcomeMessageの埋め込み文字列を処理後に返却
     * @param sender
     * @return 
     */
    public String getWelcomeMessage(CommandSender sender) {
        return convertChannelInfoString(welcomeMessage, sender);
    }

    /**
     * goodbyeMessageの埋め込み文字列を処理後に返却
     * @param sender
     * @return 
     */
    public String getGoodbyeMessage(CommandSender sender) {
        return convertChannelInfoString(goodbyeMessage, sender);
    }

    /**
     * メッセージ送信処理(TagPrefix)
     * @param plg プラグインインスタンス
     * @param sender 送信者インスタンス(nullの場合ブロードキャスト)
     * @param msg 送信文字列
     */
    public void sendChannelMessage(PluginFrame plg, CommandSender sender, String msg) {
        StringBuilder sb = new StringBuilder();
        if (sender instanceof Player) {
            sb.append(ChatColor.YELLOW);
            sb.append("[");
            sb.append(tag);
            sb.append("] ");
        } else {
            sb.append("[");
            sb.append(plg.getName());
            sb.append("] ");
        }
        sb.append(msg);
        sb.append(ChatColor.RESET);
        if (sender != null) {
            sender.sendMessage(sb.toString());
        } else {
            plg.getServer().broadcastMessage(sb.toString());
        }
    }

    /**
     * メッセージ送信処理(TagPrefix)
     * @param plg プラグインインスタンス
     * @param sender 送信者インスタンス(nullの場合ブロードキャスト)
     * @param msg 送信文字列
     */
    public void sendChannelBroadcast(PluginFrame plg, CommandSender sender, String msg) {
        // 送信依頼
        AsyncWorker worker = (AsyncWorker) plg.getPluginTimer("worker");
        ChatPayload payload = new ChatPayload(plg, sender, ChatPayload.Type.CHAT);
        payload.message = msg;
        payload.channelTag = tag;
        payload.broadcast = true;
        worker.sendData(payload);
    }

    /**
     * メッセージ送信処理(TagPrefix)
     * @param plg プラグインインスタンス
     * @param target 送信先ユーザUUID
     * @param msg 送信文字列
     */
    public void sendPlayerBroadcast(PluginFrame plg, UUID target, String msg) {
        // 送信依頼
        AsyncWorker worker = (AsyncWorker) plg.getPluginTimer("worker");
        ChatPayload payload = new ChatPayload(plg, target, ChatPayload.Type.CHAT);
        payload.message = msg;
        payload.channelTag = tag;
        payload.broadcast_target = true;
        worker.sendData(payload);
    }
}
