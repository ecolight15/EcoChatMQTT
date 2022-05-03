
package jp.minecraftuser.ecochatmqtt.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jp.minecraftuser.ecochatmqtt.config.EcoChatMQTTConfig;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.Utl;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * リロードコマンドクラス
 * @author ecolight
 */
public class JoinCommand extends CommandFrame {
    HashMap<CommandSender, String[]> argList;

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public JoinCommand(PluginFrame plg_, String name_) {
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
        return "ecochatmqtt.chat.join";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        // /join <channel TAG1> [channel TAG2]...
        // パラメータチェック:1以上
        if (!checkRange(sender, args, 1, -1)) return true;
        
        EcoChatMQTTConfig cnf = (EcoChatMQTTConfig) conf;
        boolean notExist = false;
        for (String ch : args) {
            // チャンネルの存在チェック
            if (!cnf.isExistChannel(ch)) {
                // 無ければ新規作成のconfirmへ
                notExist = true;
                break;
            }
        }
        // 新規作成判定
        if (notExist) {
            // チャンネルが存在しない場合、作成確認のconfirm表示
            if (args.length == 1) {
                Utl.sendPluginMessage(plg, sender, "新規にチャットチャンネルを作成しますがよろしいですか？");
            } else {
                Utl.sendPluginMessage(plg, sender, "存在しないチャンネルが含まれています。");
                Utl.sendPluginMessage(plg, sender, "新規にチャットチャンネルを作成しますがよろしいですか？");
            }
            if (argList.containsKey(sender)) {
                argList.replace(sender, args.clone());
            } else {
                argList.put(sender, args.clone());
            }
            confirm(sender);
            return true;
        } else {
            // 新規作成する
            AsyncWorker worker = (AsyncWorker) plg.getPluginTimer("worker");
            ChatPayload data = new ChatPayload(plg, (Player) sender, ChatPayload.Type.CMD_JOIN);
            data.param = args.clone();
            worker.sendData(data);
        }
        return true;
    }
    protected List<String> getTabComplete(CommandSender sender, Command cmd, String string, String[] strings) {
        ArrayList<String> list = new ArrayList<>();
        if (strings.length == 1) {
            list.add("<channel TAG1>");
        } else {
            list.add("[<channel TAG"+strings.length+">]");
        }
        return list;
    }

    /**
     * accept 受付用
     * @param sender 
     */
    @Override
    protected void acceptCallback(CommandSender sender) {
        // チャンネル作成のconfirmをacceptしたのでJOINする
        AsyncWorker worker = (AsyncWorker) plg.getPluginTimer("worker");
        ChatPayload data = new ChatPayload(plg, (Player) sender, ChatPayload.Type.CMD_JOIN);
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
