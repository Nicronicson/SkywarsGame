package SkywarsGame.game;

import SkywarsGame.Main;
import SkywarsGame.util.Language;
import SkywarsGame.spectator.SpectatorManager;
import net.minecraft.server.v1_16_R3.EntityExperienceOrb;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.HashSet;
import java.util.Set;

public class GameListener implements Listener {

    private final SpectatorManager spectatorManager;
    private final GameManager gameManager;

    public GameListener(SpectatorManager spectatorManager, GameManager gameManager) {
        this.spectatorManager = spectatorManager;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerSpawnLocation(PlayerSpawnLocationEvent e){
        //Determine PlayerProperties
        switch (gameManager.getGameState()){
            /*case PREPARING:
                e.getPlayer().kickPlayer(Language.ERR_TRY_TO_REJOIN.getFormattedText());
                break;
            Not needed - look at default branch*/

            case WARM_UP:

            case RUNNING:
                spectatorManager.joinAsSpectator(e);
                break;

            case LOBBY:
                gameManager.joinGame(e.getPlayer());

            case FINISHED:
                gameManager.joinLobby(e);
                break;

            default:
                e.getPlayer().kickPlayer(Language.ERR_TRY_TO_REJOIN.getFormattedText());
                break;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        //Custom join message
        if(gameManager.getGameState() == GameState.LOBBY || gameManager.getGameState() == GameState.FINISHED){
            e.setJoinMessage(String.format(Language.PLAYER_JOIN.getText(), e.getPlayer().getName()));
        } else {
            e.setJoinMessage("");
        }

        //Check if there are enough players to start
        if(gameManager.getGameState() == GameState.LOBBY) {
            if(gameManager.enoughPlayers()){
                gameManager.initiateWarmupCountdown();
            }
            else {
                gameManager.broadcastNeededPlayers();
                gameManager.stopCountdown();
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        //Custom leave message
        if(!spectatorManager.isSpectator(e.getPlayer())){
            e.setQuitMessage(String.format(Language.PLAYER_LEAVE.getText(), e.getPlayer().getName()));
        } else {
            e.setQuitMessage("");
        }

        //Remove Player from all important Game entries
        removePlayer(e.getPlayer());
    }

    private void removePlayer(Player player){
        spectatorManager.removeSpectator(player);
        gameManager.removePlayer(player);
        if(gameManager.getGameState() == GameState.WARM_UP || gameManager.getGameState() == GameState.RUNNING) {
            gameManager.setPlayerCount();
        }
        if(gameManager.getGameState() == GameState.LOBBY && !gameManager.enoughPlayers()){
            gameManager.broadcastNeededPlayers();
            gameManager.stopCountdown();
        }
    }

    @EventHandler
    public void onPlayerGetHurt(EntityDamageEvent e){
        EntityDamageByEntityEvent eBYe = e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK ? (EntityDamageByEntityEvent) e : null;

        if(e.getEntity() instanceof Player) {
            switch (gameManager.getGameState()) {
                //Cancel damaged received from other players in the Warmup phase
                case WARM_UP:
                    Bukkit.broadcastMessage("WARM_UP");
                    if (eBYe != null && eBYe.getDamager() instanceof Player) {
                        e.setCancelled(true);
                    }
                    break;

                case RUNNING:
                    Bukkit.broadcastMessage("RUNNING");
                    //Cancel damage from teammates
                    if(eBYe != null && eBYe.getDamager() instanceof Player && gameManager.getPlayerTeamMap().get((Player) eBYe.getEntity()).isPlayerInTeam((Player) eBYe.getDamager())){
                        e.setCancelled(true);
                        break;
                    }

                    //Save last damager
                    if (eBYe != null && eBYe.getDamager() instanceof Player) {
                        if(gameManager.getLastDamager().containsKey((Player) eBYe.getEntity()))
                            gameManager.getLastDamager().replace((Player) eBYe.getEntity(),(Player) eBYe.getDamager());
                        else
                            gameManager.getLastDamager().put((Player) eBYe.getEntity(),(Player) eBYe.getDamager());
                    }

                    //Checking if Player would die
                    if(((Player) e.getEntity()).getHealth() - e.getFinalDamage() <= 0) {
                        //Increase Kill Counter if it was caused by a Player
                        if (eBYe != null && eBYe.getDamager() instanceof Player) {
                            if (gameManager.getLastDamager().containsKey((Player) eBYe.getEntity())) {
                                gameManager.increaseKillCounter(gameManager.getLastDamager().get((Player) eBYe.getEntity()));

                                //Play death sound
                                ((Player) eBYe.getEntity()).playSound(eBYe.getEntity().getLocation(), Sound.BLOCK_ANVIL_LAND, SoundCategory.AMBIENT, 1, 0.5F);

                                //Play kill sound
                                ((Player) eBYe.getDamager()).playSound(eBYe.getDamager().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.AMBIENT, 1, 1.5F);
                            }
                        }

                        e.setCancelled(true);

                        //Remove player when he dies
                        removePlayer((Player) e.getEntity());

                        //Let the player respawn as a Spectator
                        spectatorManager.RespawnAsSpectator((Player) e.getEntity());
                    }

                    break;

                case PREPARING:
                    //Cancel everything which happens in this phase
                        e.setCancelled(true);
                    break;

                case LOBBY:
                    //Teleport player to spawn when falling in Lobby
                case FINISHED:
                    if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                        e.setCancelled(true);
                        gameManager.teleportToLobby((Player) e.getEntity());
                    }
                    break;
            }
        }
    }
}
