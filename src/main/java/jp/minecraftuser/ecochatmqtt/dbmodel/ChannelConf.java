
package jp.minecraftuser.ecochatmqtt.dbmodel;

import org.bukkit.ChatColor;

/**
 *
 * @author ecolight
 */
public class ChannelConf {
    public int id; // プライマリキー
    public String color;
    public boolean bold;
    public boolean italic;
    public boolean line;
    public boolean strike;
    
    public ChannelConf (
            int id_,
            String color_,
            boolean bold_,
            boolean italic_,
            boolean line_,
            boolean strike_
    ) {
        id = id_;
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
