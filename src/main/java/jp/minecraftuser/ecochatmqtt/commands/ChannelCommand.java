
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
public class ChannelCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public ChannelCommand(PluginFrame plg_, String name_) {
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
        return "ecochatmqtt.chat.channel";
    }

    /**
     * タブコンプリート機能に追加する文字列を返却する
     * @param sender コマンド送信者インスタンス
     * @param cmd コマンドインスタンス
     * @param string コマンド文字列
     * @param strings パラメタ文字列配列
     * @return タブコンプリートに追加する文字列郡
     */
    @Override
    protected List<String> getTabComplete(CommandSender sender, Command cmd, String string, String[] strings) {
        ArrayList<String> list = new ArrayList<>();
        if (strings.length == 1) {
            list.add("bold");
            list.add("italic");
            list.add("line");
            list.add("strike");
        }
        return list;
    }
    
    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        // /channel 
        // パラメータチェック:0のみ
        if (!checkRange(sender, args, 1, 1)) return true;

        AsyncWorker worker = (AsyncWorker) plg.getPluginTimer("worker");
        ChatPayload data = new ChatPayload(plg, (Player) sender, ChatPayload.Type.CMD_CHANNEL);
        data.param = args.clone();
        worker.sendData(data);

        return true;
    }
    
}
