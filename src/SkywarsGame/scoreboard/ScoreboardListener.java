package SkywarsGame.scoreboard;

import SkywarsGame.game.GameManager;
import SkywarsGame.game.GameState;
import SkywarsGame.tools.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ScoreboardListener implements Listener {

    GameManager gameManager;

    public ScoreboardListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /*
    @EventHandler
    public void scoreboardPlayerDamage(EntityDamageByEntityEvent event) { // save last damager
        if(gameManager.getGameState() == GameState.WARM_UP || gameManager.getGameState() == GameState.RUNNING) {
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                if(gameManager.getLastDamager().containsKey(event.getEntity()))
                    gameManager.getLastDamager().replace((Player) event.getEntity(),(Player) event.getDamager());
                else
                    gameManager.getLastDamager().put((Player) event.getEntity(),(Player) event.getDamager());
            }
        }
    }
    */
}

