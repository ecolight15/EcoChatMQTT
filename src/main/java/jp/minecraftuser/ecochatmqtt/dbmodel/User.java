
package jp.minecraftuser.ecochatmqtt.dbmodel;

import java.util.UUID;

/**
 *
 * @author ecolight
 */
public class User {
    public int id;                  // プレイヤーテーブル上のプライマリキー
    public UUID uuid;               // UUID
    public int activeChannel;       // アクティブチャンネル
    public boolean mute;            // 管理者専用コマンド、プレイヤーの発言を全面禁止にする
    public int localRange;          // localタイプのチャットの発言をどの程度はなれた距離から受信できるかブロック単位で設定する
    public boolean infoJoinLeave;   // チャンネルへ他のプレイヤーが参加／不参加した際の情報表示を切り替える(デフォルト非表示)
    public boolean showNGUser;      // NGユーザーの発言のチャット画面への表示を切り替える(デフォルト非表示)
    public boolean infoNotifyCount; // 範囲指定設定があるチャンネルへの発言が何人に届いたか表示する(デフォルト有効)
    public boolean rsWarn;          // RSコマンドの返信時、前回受信と前々回受信のユーザーが異なる場合に警告する(デフォルト有効)
    public boolean spyChat;         // 
    public boolean spyPM;           // 

    public User (
            int id_,
            UUID uuid_,
            int activeChannel_,
            boolean mute_,
            int localRange_,
            boolean infoJoinLeave_,
            boolean showNGUser_,
            boolean infoNotifyCount_,
            boolean rsWarn_,
            boolean spyChat_,
            boolean spyPM_
    ) {
        id = id_;
        uuid = uuid_;
        activeChannel = activeChannel_;
        mute = mute_;
        localRange = localRange_;
        infoJoinLeave = infoJoinLeave_;
        showNGUser = showNGUser_;
        infoNotifyCount = infoNotifyCount_;
        rsWarn = rsWarn_;
        spyChat = spyChat_;
        spyPM = spyPM_;
    }
}
