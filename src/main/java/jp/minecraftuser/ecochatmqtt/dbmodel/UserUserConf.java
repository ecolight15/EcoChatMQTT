
package jp.minecraftuser.ecochatmqtt.dbmodel;

import org.bukkit.ChatColor;

/**
 *
 * @author ecolight
 */
public class UserUserConf {
    public int userid;          // プライマリキー
    public int target;
    public String color;
    public boolean bold;
    public boolean italic;
    public boolean line;
    public boolean strike;
    
    public UserUserConf (
            int userid_,
            int target_,
            String color_,
            boolean bold_,
            boolean italic_,
            boolean line_,
            boolean strike_
    ) {
        userid = userid_;
        target = target_;
        color = color_;
        bold = bold_;
        italic = italic_;
        line = line_;
        strike = strike_;
    }

    /**
     * Colorインスタンスで色を返却する
     * @return 
     */
    public ChatColor getColor() {
        return ChatColor.valueOf(color);
    }
    /**
     * Colorインスタンスで色を返却する
     * @return 
     */
    public String getColorCode() {
        return String.format("§%c", ChatColor.valueOf(color).getChar());
    }
}
