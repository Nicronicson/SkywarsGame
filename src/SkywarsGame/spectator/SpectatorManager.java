package SkywarsGame.spectator;

import SkywarsGame.Main;
import SkywarsGame.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.*;

public class SpectatorManager {

    private final Set<Player> spectators = new HashSet<>();
    private final GameManager gameManager;

    public SpectatorManager(GameManager gameManager){
        this.gameManager = gameManager;
    }

    public void joinAsSpectator(PlayerSpawnLocationEvent e){

        Player player = e.getPlayer();

        //Hide all Spectators to the new Player.
        spectators.forEach(playerToHide -> player.hidePlayer(Main.getJavaPlugin(), playerToHide));

        //Add player to the spectator list
        spectators.add(player);

        //Teleport to SpectatorSpawn
        Location spectatorSpawn = gameManager.getMapGame().getMiddle();
        spectatorSpawn.setWorld(Bukkit.getWorld(gameManager.getMapGame().getMapname()));

        e.setSpawnLocation(spectatorSpawn);

        player.setPlayerListName(ChatColor.GRAY + player.getName() + ChatColor.RED + " ✗");
        player.setInvisible(true);

        //Everything which needs to be set after spawning
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setCollidable(false);
                player.setInvulnerable(true);
                player.getInventory().clear();
                hidePlayer(player);
            }
        }.runTaskLater(Main.getJavaPlugin(), 2L);
    }

    public void RespawnAsSpectator(Player player, boolean voidDamage){

        //Add player to the spectator list
        spectators.add(player);

        //Teleport to SpectatorSpawn
        Location spectatorSpawn = gameManager.getMapGame().getMiddle();
        spectatorSpawn.setWorld(Bukkit.getWorld(gameManager.getMapGame().getMapname()));

        if(voidDamage) {
            player.teleport(spectatorSpawn);
        } else {
            Vector bounceVector = player.getLocation().getDirection().normalize();
            bounceVector.setY(0);
            bounceVector.multiply(-2);
            player.setVelocity(bounceVector);
        }

        player.setHealth(20);
        player.setPlayerListName(ChatColor.GRAY + player.getName() + ChatColor.RED + " ✗");
        player.setInvisible(true);
        player.setInvulnerable(true);
        player.setCollidable(false);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().clear();
        player.getActivePotionEffects().clear();
        player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
        hidePlayer(player);
    }

    public void removeSpectator(Player player){
        spectators.remove(player);
    }

    public boolean isSpectator(Player player){
        return spectators.contains(player);
    }

    public void sendMessageToSpectators(String message){
        Bukkit.getOnlinePlayers().forEach(player -> {
            if(isSpectator(player)) {
                player.sendMessage(message);
            }
        });
    }

    public void hidePlayer(Player playerToHide){
        Bukkit.getOnlinePlayers().forEach(player -> player.hidePlayer(Main.getJavaPlugin(), playerToHide));
    }

}
