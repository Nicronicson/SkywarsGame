package SkywarsGame.util;

import org.bukkit.ChatColor;

public enum Language {

    GENERAL_ERROR(ChatColor.DARK_RED + "Something weird happened, nobody is safe :/"),
    PLAYER_JOIN(ChatColor.GREEN + "» " + ChatColor.WHITE + "%s"),
    PLAYER_LEAVE(ChatColor.RED + "« " + ChatColor.WHITE + "%s"),
    ERR_MAP_NOT_FOUND("Es existiert keine kompatible Map mit diesem Namen."),
    ERR_TRY_TO_REJOIN("Joining failed. Please try again!"),
    ERR_NOT_ENOUGH_PLAYERS("Es sind zu wenig Spieler auf dem Server!"),
    TITLE_START(ChatColor.AQUA + "Skywars" + ChatColor.GRAY + " startet in: %d"),
    TITLE_START_FIGHT("WarmUp Phase endet in: %d"),
    PLAYERS_NEEDED("Es werden noch " + ChatColor.RED + "%d" + ChatColor.GRAY + " weitere Spieler benötigt!"),
    PLAYERS_NEEDED_ONE("Es wird noch " + ChatColor.RED + "1" + ChatColor.GRAY + " weiterer Spieler benötigt!"),
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
