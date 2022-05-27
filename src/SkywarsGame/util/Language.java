package SkywarsGame.util;

import org.bukkit.ChatColor;

public enum Language {

    GAME_START("Das Spiel startet in: " + ChatColor.YELLOW + "%d" + ChatColor.GRAY + " Sekunden"),
    GAME_START_TITLE(ChatColor.AQUA + "Skywars " + ChatColor.GRAY + "startet in: " + ChatColor.WHITE),
    GAME_START_CANCEL("Der Spielstart wurde manuell " + ChatColor.RED + "abgebrochen"),
    WARM_UP("Die Schutzzeit endet in " + ChatColor.YELLOW + "%d" + ChatColor.GRAY + " Sekunden"),
    WARM_UP_FINAL("Die Schutzzeit ist jetzt vorbei!"),
    ANTI_FALL("Das Spiel startet in:"),
    GENERAL_ERROR(ChatColor.DARK_RED + "Something weird happened, nobody is safe :/"),
    PLAYER_JOIN(ChatColor.GREEN + "» " + ChatColor.WHITE + "%s"),
    PLAYER_LEAVE(ChatColor.RED + "« " + ChatColor.WHITE + "%s"),
    MAP_CHANGED("Map wurde erfolgreich geändert."),
    ERR_MAP_NOT_FOUND("Es existiert keine kompatible Map mit diesem Namen."),
    ERR_TRY_TO_REJOIN("Joining failed. Please try again!"),
    ERR_NOT_ENOUGH_PLAYERS("Es sind zu wenig Spieler auf dem Server!"),
    //TITLE_START(ChatColor.AQUA + "Skywars" + ChatColor.GRAY + " startet in: %d"),
    //TITLE_START_FIGHT("WarmUp Phase endet in: %d"),
    PLAYERS_NEEDED("Es werden noch " + ChatColor.RED + "%d" + ChatColor.GRAY + " weitere Spieler benötigt!"),
    PLAYERS_NEEDED_ONE("Es wird noch " + ChatColor.RED + "1" + ChatColor.GRAY + " weiterer Spieler benötigt!"),
    GAME_FINISHED(ChatColor.GRAY + "---Spiel vorbei---"),
    ANNOUNCE_WIN_TEAM("%sTeam %d" + ChatColor.GRAY + " hat gewonnen."),
    ANNOUNCE_WIN_PLAYERS("%s" + ChatColor.GRAY + "haben gewonnen."),
    ANNOUNCE_WIN_PLAYER("%s" + ChatColor.GRAY + "hat gewonnen."),
    PLAYER_TEAM_NAME(ChatColor.GRAY + "[%sT%d" + ChatColor.GRAY + "] " + ChatColor.WHITE + "%s" + ChatColor.GRAY),
    PLAYER_HEALTH("%s: %s"),
    DEATH("%s ist gestorben."),
    DEATH_BY_PLAYER("%s wurde von %s ermordet."),
    CUSTOM("%s"),
    KIT_CHANGE("Das Kit wurde erfolgreich gewechselt."),
    TEAM_CHANGE("Das Team wurde erfolgreich gewechselt."),
    TEAM_FULL("Das Team ist voll.");

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
