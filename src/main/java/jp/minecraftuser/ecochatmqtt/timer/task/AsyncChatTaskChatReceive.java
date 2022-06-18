
package jp.minecraftuser.ecochatmqtt.timer.task;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import jp.minecraftuser.ecochatmqtt.db.EcoChatDB;
import jp.minecraftuser.ecochatmqtt.dbmodel.Channel;
import jp.minecraftuser.ecochatmqtt.dbmodel.ChannelConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.User;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserChannelConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserNgConf;
import jp.minecraftuser.ecochatmqtt.dbmodel.UserUserConf;
import jp.minecraftuser.ecochatmqtt.mqttmodel.ChatJson;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * タスク別処理分割用 チャット クラス
 * @author ecolight
 */
public class AsyncChatTaskChatReceive extends AsyncChatTaskBase {
    Gson gson = new Gson();
    long startTime;
    
    // シングルトン実装
    private static AsyncChatTaskChatReceive instance = null;
    public static final AsyncChatTaskChatReceive getInstance(PluginFrame plg_) {
        if (instance == null) {
            instance = new AsyncChatTaskChatReceive(plg_);
        }
        return instance;
    }
    
    /**
     * コンストラクタ
     * @param plg_ 
     */
    public AsyncChatTaskChatReceive(PluginFrame plg_) {
        super(plg_);
    }

    /**
     * 非同期で実施する処理
     * Bukkit/Spigotインスタンス直接操作不可
     * @param thread
     * @param db
     * @param con
     * @param data 
     * @throws java.sql.SQLException 
     */
    @Override
    public void asyncThread(AsyncWorker thread, EcoChatDB db, Connection con, ChatPayload data) throws SQLException {
        ChatJson json;
        startTime = System.nanoTime();
        try {
            json = gson.fromJson(data.message, ChatJson.class);
            json.check();   // メンバのnullチェック
        } catch(Exception e) {
            plg.getLogger().log(Level.WARNING,
                    "gson convert failed. payload[{1}]",
                    new Object[]{data.message});
            plg.getLogger().log(Level.WARNING, null, e);
            data.result = false;
            return;
        }
        log.info("receive json tag:"+json.msg);
        // 送信ユーザー情報の取得
        User sender = conf.getUser(json.player.uuid);
        
        // チャンネル情報の取得
        Channel ch = conf.getChannel(json.channel);
        
        // 送信ユーザーの選定
        HashMap<Player, StringBuilder> sendList = new HashMap<>();
        for (Player opl : plg.getServer().getOnlinePlayers()) {
            User us = conf.getUser(opl);
            boolean reject = false;
            
            // broadcastのUUID指定の場合は、該当UUIDでなければスキップ
            if (json.broadcast_target) {
                if (!us.uuid.equals(json.target_uuid)) continue;
            }
            
            // 該当チャンネルに所属しているユーザーかどうか
            if (!conf.isExistChannelUserByID(us.id, ch.id)) {
                reject = true;
            }

            // まずはNGユーザー判定
            if (!reject) {
                UserNgConf ng = conf.getUserNgConfByID(us.id, sender.id);
                if (ng != null) {
                    if (ng.ng) {
                        // NGかつNGユーザーの発言を表示するフラグが落ちていれば表示しない
                        if (!us.showNGUser) {
                            reject = true;
                        }
                    }
                }
            }

            // ワールドおよびローカル指定の場合は同じワールドのユーザーのみに伝達する
            if (!reject) {
                // ブロードキャスト指定の場合は距離関係なし
                if ((!json.broadcast) && (!json.broadcast_target)) {
                    if (ch.type == Channel.Type.world || ch.type == Channel.Type.local) {
                        if (!json.player.world.equalsIgnoreCase(opl.getWorld().getName())) {
                            reject = true;
                        }
                        // 以下はローカルの場合の判定
                        if (!reject) {
                            if (ch.type == Channel.Type.local) {
                                Location senderLoc = new Location(opl.getWorld(), json.player.x, json.player.y, json.player.z);
                                if (us.localRange < opl.getLocation().distance(senderLoc)) {
                                    reject = true;
                                }
                            }
                        }
                    }
                }
            }
            
            // ブロードキャスト設定の場合は、設定を確認
            // 個人宛てブロードキャスト設定の場合は確認しない
            if (!reject) {
                if (json.broadcast) {
                    // 通知が無効なら送信しない
                    if (!us.infoJoinLeave) {
                        reject = true;
                    }
                }
            }
            
            StringBuilder sb = new StringBuilder();
            // 送信対象じゃない場合
            if (reject && us.spyChat) {
                // spy設定者はプリフィックス付与の上継続
                sb.append(ChatColor.GREEN);
                sb.append("[SpyChat] ");
                sb.append(ChatColor.RESET);
            } else if (reject) {
                // それ以外はreject
                continue;
            }
            // ここから表示確定
            //------------------------------------------------------------------
            // メッセージ整形
            //------------------------------------------------------------------
            // ブロードキャストの場合
            if (json.broadcast || json.broadcast_target) {
                sb.append(ChatColor.YELLOW);
                sb.append("[");
                sb.append(ch.tag);
                sb.append("] ");
                sb.append(json.msg);
                sendList.put(opl, sb);
                continue;
            }
            // 通常チャットの場合
            // チャンネルタグ部(チャンネル設定から取得)
            ChannelConf chconf = conf.getChannelConf(json.channel);
            if (chconf != null) {
                sb.append(chconf.getColor());
                if (chconf.bold) sb.append(ChatColor.BOLD);
                if (chconf.italic) sb.append(ChatColor.ITALIC);
                if (chconf.line) sb.append(ChatColor.UNDERLINE);
                if (chconf.strike) sb.append(ChatColor.STRIKETHROUGH);
                sb.append("[");
                sb.append(ch.tag);
                sb.append(ChatColor.RESET);
                sb.append(chconf.getColor());
                if (chconf.bold) sb.append(ChatColor.BOLD);
                if (chconf.italic) sb.append(ChatColor.ITALIC);
                if (chconf.line) sb.append(ChatColor.UNDERLINE);
                if (chconf.strike) sb.append(ChatColor.STRIKETHROUGH);
                sb.append("] ");
                sb.append(ChatColor.RESET);
            } else {
                sb.append("[");
                sb.append(ch.tag);
                sb.append("] ");
            }

            // ユーザー名部(送信先ユーザー設定から取得)
            UserUserConf ususconf = conf.getUserUserConf(opl.getUniqueId(), json.player.uuid);
            if (ususconf != null) {
                sb.append(ususconf.getColor());
                if (ususconf.bold) sb.append(ChatColor.BOLD);
                if (ususconf.italic) sb.append(ChatColor.ITALIC);
                if (ususconf.line) sb.append(ChatColor.UNDERLINE);
                if (ususconf.strike) sb.append(ChatColor.STRIKETHROUGH);
                sb.append("<");
                sb.append(json.player.name);
                sb.append("> ");
                sb.append(ChatColor.RESET);
            } else {
                sb.append("<");
                sb.append(json.player.name);
                sb.append("> ");
            }
                
            // メッセージ部(送信先ユーザー設定から取得)
            UserChannelConf uschconf = conf.getUserChannelConf(opl.getUniqueId(), json.channel);
            if (uschconf != null) {
                sb.append(uschconf.getColor());
                if (uschconf.bold) sb.append(ChatColor.BOLD);
                if (uschconf.italic) sb.append(ChatColor.ITALIC);
                if (uschconf.line) sb.append(ChatColor.UNDERLINE);
                if (uschconf.strike) sb.append(ChatColor.STRIKETHROUGH);
                sb.append(json.msg);
                sb.append(ChatColor.RESET);
            } else {
                sb.append(json.msg);
            }
            
            // 送信リストに追加
            sendList.put(opl, sb);
            if (opl.getUniqueId().equals(json.player.uuid)) {
                data.player = opl;
            }
        }
        
        // 送信データの設定
        log.info("diff = " + (System.nanoTime() - startTime) / 1000000);
        // 正常であれば送信する
        for (Player pl : sendList.keySet()) {
            // ブロードキャスト送信でない、かつlocalかつ送信者の場合は送信人数を表示する
            StringBuilder sb = sendList.get(pl);
            if ((!json.broadcast) && (ch.type == Channel.Type.local)) {
                if (pl.equals(data.player)) {
                    sb.insert(0, ")");
                    sb.insert(0, sendList.size());
                    sb.insert(0, "(+");
                }
            }
            pl.sendMessage(sb.toString());
        }
        data.result = true;
        log.info("send diff = " + (System.nanoTime() - startTime) / 1000000);
    }

    /**
     * 応答後メインスレッド側で実施する処理
     * Bukkit/Spigotインスタンス直接操作可
     * @param thread
     * @param data 
     */
    @Override
    public void mainThread(AsyncWorker thread, ChatPayload data) {
        log.info("return main therad = " + (System.nanoTime() - startTime) / 1000000);
    }

}
