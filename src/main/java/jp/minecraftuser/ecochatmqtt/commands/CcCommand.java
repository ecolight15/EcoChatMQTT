
package jp.minecraftuser.ecochatmqtt.commands;

import java.util.HashMap;
import jp.minecraftuser.ecochatmqtt.config.EcoChatMQTTConfig;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * リロードコマンドクラス
 * @author ecolight
 */
public class CcCommand extends CommandFrame {
    HashMap<CommandSender, String[]> argList;

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public CcCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
        setAuthBlock(true);
        setAuthConsole(true);
        argList = new HashMap<>();
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecochatmqtt.chat";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        // 続く文字列をチェンネル名と判断し、アクティブの変更か新規作成
        // /cc xxx
        // または該当チェンネルへの発言
        // /cc xxx message
        EcoChatMQTTConfig cnf = (EcoChatMQTTConfig) conf;
        // パラメータチェック:1以上
        if (!checkRange(sender, args, 1, -1)) return true;
        
        AsyncWorker worker = (AsyncWorker) plg.getPluginTimer("worker");
        ChatPayload data;
        if (args.length == 1) {
            // activeの切り替えかjoinかの振り分け
            // チャンネルが存在していて、参加していればアクティブの切り替え

            // チャンネルの存在チェック
            if (cnf.isExistChannel(args[0])) {
                // 所属していればSET
                if (cnf.isExistChannelUser((Player) sender, args[0])) {
                    data = new ChatPayload(plg, (Player) sender, ChatPayload.Type.CMD_SET);
                    data.param = args.clone();
                    worker.sendData(data);
                    return true;
                }
            }
            // そうでない場合はJOIN
            if (cnf.isExistChannel(args[0])) {
                // すでに存在する場合はJOINタスクへ
                data = new ChatPayload(plg, (Player) sender, ChatPayload.Type.CMD_JOIN);
                data.param = args.clone();
                worker.sendData(data);
                return true;
            } else {
                // チャンネルが存在しない場合、作成確認のconfirm表示
                Utl.sendPluginMessage(plg, sender, "新規にチャットチャンネルを作成しますがよろしいですか？");
                if (argList.containsKey(sender)) {
                    argList.replace(sender, args.clone());
                } else {
                    argList.put(sender, args.clone());
                }
                confirm(sender);
                return true;
            }
        } else {
            // それ以上の場合は該当チェンネルへの発言
            data = new ChatPayload(plg, (Player) sender, ChatPayload.Type.CHAT);
            int i = 0;
            StringBuilder sb = new StringBuilder();
            for (String s : args) {
                if (i == 0) {
                    data.channelTag = s;
                } else if (i == 1) {
                    sb.append(s);
                } else {
                    sb.append(" ");
                    sb.append(s);
                }
                i++;
            }
            data.message = sb.toString();
            worker.sendData(data);
        }
        
        return true;
    }

    /**
     * accept 受付用
     * @param sender 
     */
    @Override
    protected void acceptCallback(CommandSender sender) {
        // チャンネル作成のconfirmをacceptしたのでJOINする
        AsyncWorker worker = (AsyncWorker) plg.getPluginTimer("worker");
        ChatPayload data;
        data = new ChatPayload(plg, (Player) sender, ChatPayload.Type.CMD_JOIN);
        data.param = argList.get(sender);
        worker.sendData(data);
        return;
    }
    
    /**
     * cancel 受付用
     * @param sender 
     */
    @Override
    protected void cancelCallback(CommandSender sender) {
        Utl.sendPluginMessage(plg, sender, "チャンネルの作成をキャンセルしました");
    }
}
