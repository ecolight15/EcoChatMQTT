
package jp.minecraftuser.ecochatmqtt.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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
public class LeaveCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public LeaveCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
        setAuthBlock(true);
        setAuthConsole(true);
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecochatmqtt.chat.leave";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        // /leave [ALL | <channel TAG1> [channel TAG2]...]
        // パラメータチェック:1以上
        if (!checkRange(sender, args, 1, -1)) return true;

        AsyncWorker worker = (AsyncWorker) plg.getPluginTimer("worker");
        ChatPayload data = new ChatPayload(plg, (Player) sender, ChatPayload.Type.CMD_LEAVE);
        data.param = args.clone();
        worker.sendData(data);

        return true;
    }
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
     * confirmの外部呼出し用
     * @param sender
     * @param args 
     */
    private ConcurrentHashMap<CommandSender, String[]> map_args;
    public void call_confirm(CommandSender sender, String[] args) {
        if (map_args == null) map_args = new ConcurrentHashMap<>();
        map_args.put(sender, args);
        confirm(sender);
    }
    
    /**
     * accept 受付用
     * @param sender 
     */
    @Override
    protected void acceptCallback(CommandSender sender) {
        // チャンネル離脱のconfirmをacceptしたのでLEAVEしなおす
        AsyncWorker worker = (AsyncWorker) plg.getPluginTimer("worker");
        ChatPayload data = new ChatPayload(plg, (Player) sender, ChatPayload.Type.CMD_LEAVE);
        data.param = map_args.get(sender);
        data.confirm = true;
        worker.sendData(data);
    }
    
    /**
     * cancel 受付用
     * @param sender 
     */
    @Override
    protected void cancelCallback(CommandSender sender) {
        Utl.sendPluginMessage(plg, sender, "チャンネルの離脱をキャンセルしました");
    }
}
