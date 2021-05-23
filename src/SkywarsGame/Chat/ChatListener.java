package SkywarsGame.Chat;

import SkywarsGame.game.GameManager;
import SkywarsGame.game.GameState;
import SkywarsGame.spectator.SpectatorListener;
import SkywarsGame.spectator.SpectatorManager;
import SkywarsGame.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class ChatListener implements Listener {
    GameManager gameManager;
    SpectatorManager spectatorManager;

    public ChatListener(GameManager gameManager, SpectatorManager spectatorManager) {
        this.gameManager = gameManager;
        this.spectatorManager = spectatorManager;
    }

    @EventHandler
    public void onPlayerUseChat(AsyncPlayerChatEvent e){
        if(gameManager.getGameState() == GameState.LOBBY || gameManager.getGameState() == GameState.FINISHED){
            gameManager.sendMessageToEveryone(ChatColor.WHITE + e.getPlayer().getName() + ChatColor.DARK_GRAY + " » " + ChatColor.GRAY + e.getMessage());
        } else if (gameManager.getGameState() == GameState.WARM_UP || gameManager.getGameState() == GameState.RUNNING && !spectatorManager.isSpectator(e.getPlayer())) {
            gameManager.sendMessageToTeam(gameManager.getPlayerTeamMap().get(e.getPlayer()),
                    String.format(Language.PLAYER_TEAM_NAME.getText(),
                            gameManager.getPlayerTeamMap().get(e.getPlayer()).getColor(),
                            gameManager.getPlayerTeamMap().get(e.getPlayer()).getId(),
                            e.getPlayer().getName()) + ChatColor.DARK_GRAY + " » " + ChatColor.GRAY + e.getMessage());
        } else if(spectatorManager.isSpectator(e.getPlayer())){
            spectatorManager.sendMessageToSpectators(ChatColor.WHITE + e.getPlayer().getName() + ChatColor.DARK_GRAY + " » " + ChatColor.GRAY + e.getMessage());
        }
        e.setCancelled(true);
    }
}
