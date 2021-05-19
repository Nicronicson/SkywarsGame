package SkywarsGame.game;

import SkywarsGame.Main;
import SkywarsGame.util.Language;
import SkywarsGame.spectator.SpectatorManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class GameListener implements Listener {

    private final SpectatorManager spectatorManager;
    private final GameManager gameManager;

    public GameListener(SpectatorManager spectatorManager, GameManager gameManager) {
        this.spectatorManager = spectatorManager;
        this.gameManager = gameManager;
    }


    //TODO: TEst
    @EventHandler
    public void onPlayerJoin(PlayerSpawnLocationEvent e){
        //TODO:Test
        Bukkit.broadcastMessage("Player JOIN");
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
                if(gameManager.enoughPlayers()){
                    gameManager.initiateWarmupCountdown();
                }
                else {
                    gameManager.stopCountdown();
                }
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
    public void onPlayerLeave(PlayerQuitEvent e){
        removePlayer(e.getPlayer());
    }

    private void removePlayer(Player player){
        spectatorManager.removeSpectator(player);
        gameManager.removePlayer(player);
        gameManager.setPlayerCount();
        if(gameManager.getGameState() == GameState.LOBBY && !gameManager.enoughPlayers()){
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
                    if (eBYe != null && eBYe.getDamager() instanceof Player) {
                        e.setCancelled(true);
                    }
                    break;

                case RUNNING:
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
                            }
                        }

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

    /*
    @EventHandler
    public void onPlayerFallsOutOfLobby(EntityDamageEvent e){ //Teleport player to spawn when falling in Lobby
        if(gameManager.getGameState() == GameState.LOBBY && gameManager.getGameState() == GameState.FINISHED) {
            if (e.getEntity() instanceof Player && e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                e.setCancelled(true);
                gameManager.teleportToLobby((Player) e.getEntity());
            }
        }
    }*/

    /*
    @EventHandler
    public void onPlayerDeath(EntityDamageByEntityEvent event) { //Increase Kill counter
        if(gameManager.getGameState() == GameState.WARM_UP || gameManager.getGameState() == GameState.RUNNING) {
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                if(lastDamager.containsKey(event.getEntity()))
                    gameManager.increaseKillCounter(lastDamager.get(event.getEntity()));
            }
        }
    }
    */

}
