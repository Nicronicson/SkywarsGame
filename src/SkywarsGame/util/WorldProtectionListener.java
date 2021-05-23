package SkywarsGame.util;

import SkywarsGame.game.GameManager;
import SkywarsGame.game.GameState;
import SkywarsGame.spectator.SpectatorManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;

public class WorldProtectionListener implements Listener {

    private final GameManager gameManager;
    private final SpectatorManager spectatorManager;

    public WorldProtectionListener(GameManager gameManager, SpectatorManager spectatorManager) {
        this.gameManager = gameManager;
        this.spectatorManager = spectatorManager;
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
    //Cancels all Interactions
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e){
        protectLobby(e);
    }

    @EventHandler
    //Cancels all Block Changes
    public void onEntityBlockChange(EntityChangeBlockEvent e){
        protectLobby(e);
    }

    @EventHandler
    //Cancels all Interactions
    public void onArmorStand(PlayerArmorStandManipulateEvent e){
        protectLobby(e);
    }

    @EventHandler
    //Cancels all Interactions
    public void onHangingBreak(HangingBreakByEntityEvent e){
        protectLobby(e);
    }

    @EventHandler
    //Cancels all Interactions
    public void onEntityInteract(EntityChangeBlockEvent e){
        protectLobby(e);
    }

    @EventHandler
    //Cancels all Interactions
    public void onEntityDamage(EntityDamageEvent e){
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
        if(gameManager.getGameState() == GameState.LOBBY || gameManager.getGameState() == GameState.FINISHED || gameManager.getGameState() == GameState.PREPARING ||
                (e instanceof EntityEvent && ((EntityEvent) e).getEntity() instanceof Player && spectatorManager.isSpectator((Player) ((EntityEvent) e).getEntity())) ||
                (e instanceof PlayerEvent && spectatorManager.isSpectator(((PlayerEvent) e).getPlayer())) ||
                (e instanceof BlockPlaceEvent && spectatorManager.isSpectator(((BlockPlaceEvent) e).getPlayer())) ||
                (e instanceof BlockBreakEvent && spectatorManager.isSpectator(((BlockBreakEvent) e).getPlayer())) ||
                (e instanceof HangingBreakByEntityEvent && ((HangingBreakByEntityEvent) e).getRemover() instanceof Player && spectatorManager.isSpectator((Player) ((HangingBreakByEntityEvent) e).getRemover())) ||
                (e instanceof InventoryInteractEvent && spectatorManager.isSpectator((Player) ((InventoryInteractEvent) e).getWhoClicked()))) {
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
