package SkywarsGame.tools;

import SkywarsGame.game.GameManager;
import SkywarsGame.game.GameState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class WorldProtectionListener implements Listener {

    private final GameManager gameManager;

    public WorldProtectionListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    //Cancels Block Breaks
    public void onBlockBreak(BlockBreakEvent e){
        protectLobby(e);
    }

    @EventHandler
    //Cancels Block Placing
    public void onBlockPlace(BlockPlaceEvent e){
        protectLobby(e);
    }

    @EventHandler
    //Cancels all Interactions
    public void onPlayerInteract(PlayerInteractEvent e){
        protectLobby(e);
    }

    @EventHandler
    //Cancels all Block Changes
    public void onEntityBlockChange(EntityChangeBlockEvent e){
        protectLobby(e);
    }

    @EventHandler
    //Cancel Hunger
    public void onHungerDrain(FoodLevelChangeEvent e){
        protectLobby(e);
    }

    private void protectLobby(Event e){
        if(gameManager.getGameState() == GameState.LOBBY || gameManager.getGameState() == GameState.FINISHED || gameManager.getGameState() == GameState.PREPARING) {
            ((Cancellable) e).setCancelled(true);
        }
    }

}
