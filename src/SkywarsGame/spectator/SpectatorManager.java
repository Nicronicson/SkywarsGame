package SkywarsGame.spectator;

import SkywarsGame.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SpectatorManager {

    private final List<Player> spectators = new ArrayList<>();
    private final GameManager gameManager;

    public SpectatorManager(GameManager gameManager){
        this.gameManager = gameManager;
    }

    public void joinOrRespawnAsSpectator(Player player){

        spectators.add(player);

        //Teleport to SpectatorSpawn
        Location spectatorSpawn = gameManager.getMapGame().getMiddle();
        spectatorSpawn.setWorld(Bukkit.getWorld(gameManager.getMapGame().getMapname()));

        player.teleport(spectatorSpawn);

        player.setPlayerListName(ChatColor.GRAY + player.getName() + ChatColor.RED + " ✗");
        player.setInvisible(true);
        player.setInvulnerable(true);
        player.setCollidable(false);
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    public void removeSpectator(Player player){
        spectators.remove(player);
    }

    public boolean isSpectator(Player player){
        return spectators.contains(player);
    }

}
