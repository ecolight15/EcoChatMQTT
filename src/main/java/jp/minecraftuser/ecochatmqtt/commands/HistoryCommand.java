
package jp.minecraftuser.ecochatmqtt.commands;

import java.util.ArrayList;
import java.util.List;
import jp.minecraftuser.ecochatmqtt.timer.AsyncWorker;
import jp.minecraftuser.ecochatmqtt.timer.ChatPayload;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * リロードコマンドクラス
 * @author ecolight
 */
public class HistoryCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public HistoryCommand(PluginFrame plg_, String name_) {
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
        return "ecochatmqtt.chat.history";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        // /history [yyyy/mm/dd] [page]
        // /history [page]
        // パラメータチェック:0-2
        if (!checkRange(sender, args, 0, 2)) return true;

        AsyncWorker worker = (AsyncWorker) plg.getPluginTimer("worker");
        ChatPayload data = new ChatPayload(plg, (Player) sender, ChatPayload.Type.CMD_HISTORY);
        data.param = args.clone();
        worker.sendData(data);

        return true;
    }
    protected List<String> getTabComplete(CommandSender sender, Command cmd, String string, String[] strings) {
        ArrayList<String> list = new ArrayList<>();
        if (strings.length == 1) {
            list.add("[page]");
            list.add("[yyyy/mm/dd]");
        } else if (strings.length == 2) {
            if (strings[0].contains("/")) {
                list.add("[page]");
            }
        }
        return list;
    }
}
