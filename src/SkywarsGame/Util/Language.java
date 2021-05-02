package SkywarsGame.Util;

import org.bukkit.ChatColor;

public enum Language {

    ERR_MAP_NOT_FOUND("Es existiert keine kompatible Map mit diesem Namen."),
    ERR_TRY_TO_REJOIN("Joining failed. Please try again!"),
    ERR_NOT_ENOUGH_PLAYERS("Es sind zu wenig Spieler auf dem Server!"),
    TITLE_START(ChatColor.AQUA + "Skywars" + ChatColor.GRAY + " startet in: %d"),
    ANNOUNCE_WIN_TEAM("Team %d hat gewonnen."),
    ANNOUNCE_WIN_PLAYERS("%s");

    private static final String SYSTEM_PREFIX = ChatColor.AQUA + "Skywars " + ChatColor.DARK_GRAY + "» ";

    private final String text;

    Language(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getTitleText(){
        return ChatColor.AQUA + text;
    }

    public String getFormattedText() {
        return SYSTEM_PREFIX + ChatColor.GRAY + text;
    }
}
