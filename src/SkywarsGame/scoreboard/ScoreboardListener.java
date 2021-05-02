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
    HashMap<Player, Player> lastDamager;

    public ScoreboardListener(GameManager gameManager) {
        this.gameManager = gameManager;
        lastDamager = new HashMap<>();
    }

    @EventHandler
    public void scoreboardPlayerDamage(EntityDamageByEntityEvent event) {
        if(gameManager.getGameState() == GameState.WARM_UP || gameManager.getGameState() == GameState.RUNNING) {
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                if(lastDamager.containsKey(event.getEntity()))
                    lastDamager.replace((Player) event.getEntity(),(Player) event.getDamager());
                else
                    lastDamager.put((Player) event.getEntity(),(Player) event.getDamager());
            }
        }
    }
    @EventHandler
    public void scoreboardPlayerDeath(EntityDamageByEntityEvent event) {
        if(gameManager.getGameState() == GameState.WARM_UP || gameManager.getGameState() == GameState.RUNNING) {
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                if(lastDamager.containsKey(event.getEntity()))
                    gameManager.increaseKillCounter(lastDamager.get(event.getEntity()));
            }
        }
    }
}

