package SkywarsGame.game;

import SkywarsGame.Main;
import SkywarsGame.Util.Language;
import SkywarsGame.spectator.SpectatorManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class GameListener implements Listener {

    private final SpectatorManager spectatorManager;
    private final GameManager gameManager;

    public GameListener(SpectatorManager spectatorManager, GameManager gameManager) {
        this.spectatorManager = spectatorManager;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){

        //Determine SpawnLocation and PlayerProperties
        switch (gameManager.getGameState()){
            /*case PREPARING:
                e.getPlayer().kickPlayer(Language.ERR_TRY_TO_REJOIN.getFormattedText());
                break;
            Not needed - look at default branch*/

            case WARM_UP:

            case RUNNING:
                joinAsSpectator(e.getPlayer());
                break;

            case LOBBY:
                if(gameManager.enoughPlayers()){
                    gameManager.initiateWarmupCountdown();
                }
                else {
                    gameManager.stopCountdown();
                }

            case FINISHED:
                joinInLobby(e.getPlayer());
                break;

            default:
                e.getPlayer().kickPlayer(Language.ERR_TRY_TO_REJOIN.getFormattedText());
                break;
        }
    }

    private void joinAsSpectator(Player player){
        new BukkitRunnable() {
            @Override
            public void run() {
                spectatorManager.joinOrRespawnAsSpectator(player);
            }
        }.runTaskLater(Main.getJavaPlugin(), 1);
    }

    private void joinInLobby(Player player){
        new BukkitRunnable() {
            @Override
            public void run() {
                gameManager.joinLobby(player);
            }
        }.runTaskLater(Main.getJavaPlugin(), 1);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        removePlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        removePlayer(e.getEntity());
    }

    private void removePlayer(Player player){
        spectatorManager.removeSpectator(player);
        gameManager.removePlayer(player);
        gameManager.setPlayerCount();
    }

    @EventHandler
    public void onPlayerGetHurt(EntityDamageByEntityEvent e){
        if(gameManager.getGameState() == GameState.WARM_UP)
        if(e.getEntity() instanceof Player && e.getDamager() instanceof Player){
            e.setCancelled(true);
        }
    }

}
