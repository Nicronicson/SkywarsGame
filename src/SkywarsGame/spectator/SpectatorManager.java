package SkywarsGame.spectator;

import SkywarsGame.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpectatorManager {

    private final Set<Player> spectators = new HashSet<>();
    private final GameManager gameManager;

    public SpectatorManager(GameManager gameManager){
        this.gameManager = gameManager;
    }

    public void joinAsSpectator(PlayerSpawnLocationEvent e){

        Player player = e.getPlayer();

        spectators.add(player);

        //Teleport to SpectatorSpawn
        Location spectatorSpawn = gameManager.getMapGame().getMiddle();
        spectatorSpawn.setWorld(Bukkit.getWorld(gameManager.getMapGame().getMapname()));

        e.setSpawnLocation(spectatorSpawn);

        player.setPlayerListName(ChatColor.GRAY + player.getName() + ChatColor.RED + " ✗");
        player.setInvisible(true);
        player.setInvulnerable(true);
        player.setCollidable(false);
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    public void RespawnAsSpectator(Player player){

        spectators.add(player);

        //Teleport to SpectatorSpawn
        Location spectatorSpawn = gameManager.getMapGame().getMiddle();
        spectatorSpawn.setWorld(Bukkit.getWorld(gameManager.getMapGame().getMapname()));

        player.teleport(spectatorSpawn);

        player.setHealth(20);
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
