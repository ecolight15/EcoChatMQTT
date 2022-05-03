
package jp.minecraftuser.ecochatmqtt.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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
public class DeleteCommand extends CommandFrame {
    private ConcurrentHashMap<CommandSender, String[]> map_args;

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public DeleteCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
        setAuthBlock(true);
        setAuthConsole(true);
        map_args = new ConcurrentHashMap<>();
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecochatmqtt.chat.delete";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        // /delete [channel TAG]
        // パラメータチェック:1以上
        if (!checkRange(sender, args, 0, -1)) return true;
        EcoChatMQTTConfig cnf = (EcoChatMQTTConfig) conf;
        map_args.put(sender, args.clone());
        if (args.length == 0) {
            Utl.sendPluginMessage(plg, sender, "現在のアクティブチャンネル[{0}]を削除しますがよろしいですか？",
                    cnf.getChannel(cnf.getUser((Player) sender).activeChannel).tag);
        } else if (args[0].equalsIgnoreCase("all")) {
            Utl.sendPluginMessage(plg, sender, "全ての所属チャンネルに削除を試行しますがよろしいですか？");
        } else {
            Utl.sendPluginMessage(plg, sender, "指定したチャンネルを削除しますがよろしいですか？");
        }
        confirm(sender);

        return true;
    }
    
    /**
     * 入力補完
     * @param sender
     * @param cmd
     * @param string
     * @param strings
     * @return 
     */
    protected List<String> getTabComplete(CommandSender sender, Command cmd, String string, String[] strings) {
        ArrayList<String> list = new ArrayList<>();
        if (strings.length == 1) {
            list.add("ALL");
            list.add("<channel TAG>");
        } else if (strings.length > 1) {
            if (!strings[0].equalsIgnoreCase("all")) {
                list.add("<channel TAG>");
            }
        }
        return list;
    }
    
    /**
     * accept 受付用
     * @param sender 
     */
    @Override
    protected void acceptCallback(CommandSender sender) {
        AsyncWorker worker = (AsyncWorker) plg.getPluginTimer("worker");
        ChatPayload data = new ChatPayload(plg, (Player) sender, ChatPayload.Type.CMD_DELETE);
        data.param = map_args.get(sender);
        worker.sendData(data);
    }
    
    /**
     * cancel 受付用
     * @param sender 
     */
    @Override
    protected void cancelCallback(CommandSender sender) {
        Utl.sendPluginMessage(plg, sender, "チャンネルの削除をキャンセルしました");
    }
}
