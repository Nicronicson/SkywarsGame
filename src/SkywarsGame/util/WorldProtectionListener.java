package SkywarsGame.util;

import SkywarsGame.game.GameManager;
import SkywarsGame.game.GameState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;

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

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e){
        protectLobby(e);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e){
        protectLobby(e);
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent e){
        protectLobby(e);
    }

    @EventHandler
    public void onItemMove(InventoryClickEvent e){
        protectLobby(e);
    }

    private void protectLobby(Event e){
        if(gameManager.getGameState() == GameState.LOBBY || gameManager.getGameState() == GameState.FINISHED || gameManager.getGameState() == GameState.PREPARING) {
            ((Cancellable) e).setCancelled(true);
        }
    }

    @EventHandler
    public void onXPGain(PlayerExpChangeEvent e){
        if(gameManager.getGameState() == GameState.LOBBY || gameManager.getGameState() == GameState.FINISHED || gameManager.getGameState() == GameState.PREPARING){
            e.setAmount(0);
        }
    }
}
