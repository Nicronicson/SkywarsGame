package SkywarsGame.Util;

import org.bukkit.ChatColor;

public enum Language {

    ERR_MAP_NOT_FOUND(""),
    TITLE_START(ChatColor.AQUA + "Skywars" + ChatColor.GRAY + " startet in: %d"),
    NOT_ENOUGH_PLAYERS("Es sind zu wenig Spieler auf dem Server!");

    private static final String SYSTEM_PREFIX = ChatColor.AQUA + "Skywars " + ChatColor.DARK_GRAY + "» ";

    private final String text;

    Language(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getFormattedText() {
        return SYSTEM_PREFIX + ChatColor.GRAY + text;
    }
}
