package SkywarsGame.spectator;

import SkywarsGame.Main;
import SkywarsGame.game.GameManager;
import SkywarsGame.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SpectatorListener implements Listener {

    private final SpectatorManager spectatorManager;
    private final GameManager gameManager;

    public SpectatorListener(SpectatorManager spectatorManager, GameManager gameManager) {
        this.spectatorManager = spectatorManager;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onSpectatorArrowPickup(PlayerPickupArrowEvent e){
        if(spectatorManager.isSpectator(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onSpectatorItemPickup(EntityPickupItemEvent e){
        if(e.getEntity() instanceof Player && spectatorManager.isSpectator((Player) e.getEntity())) e.setCancelled(true);
    }
}
