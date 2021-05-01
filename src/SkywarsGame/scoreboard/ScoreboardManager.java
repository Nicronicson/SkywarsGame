package SkywarsGame.scoreboard;

import SkywarsGame.game.Role;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ScoreboardManager {

    private final Map<Player, Integer> playerKills = new HashMap<>();

    public void setScoreboard(Player player, Role role) {

        Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        Objective obj = board.registerNewObjective("Infos", "dummy", ChatColor.RED + "TROUBLES");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore("").setScore(6);

        Score roleName = obj.getScore(ChatColor.WHITE + "Rolle:");
        roleName.setScore(5);
        Team roleCounter = board.registerNewTeam("roleCounter");
        roleCounter.addEntry(ChatColor.RED + "" + ChatColor.WHITE);
        roleCounter.setPrefix(role.getRoleName().getText());
        obj.getScore(ChatColor.RED + "" + ChatColor.WHITE).setScore(4);

        obj.getScore("  ").setScore(3);

        Score kills = obj.getScore(ChatColor.WHITE + "Kills:");
        kills.setScore(2);
        Team killCounter = board.registerNewTeam("killCounter");
        killCounter.addEntry(ChatColor.GREEN + "" + ChatColor.WHITE);
        killCounter.setPrefix(ChatColor.RED + "0");
        obj.getScore(ChatColor.GREEN + "" + ChatColor.WHITE).setScore(1);

        player.setScoreboard(board);
    }

    public void increaseKillCounter(Player player) {
        playerKills.put(player, playerKills.containsKey(player) ? playerKills.get(player) + 1 : 1);
        Scoreboard board = player.getScoreboard();
        Objects.requireNonNull(board.getTeam("killCounter")).setPrefix(ChatColor.RED + "" + playerKills.get(player));
    }

}