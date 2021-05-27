package SkywarsGame.chat;

import SkywarsGame.game.GameManager;
import SkywarsGame.game.GameState;
import SkywarsGame.spectator.SpectatorManager;
import SkywarsGame.util.Language;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    GameManager gameManager;
    SpectatorManager spectatorManager;

    public ChatListener(GameManager gameManager, SpectatorManager spectatorManager) {
        this.gameManager = gameManager;
        this.spectatorManager = spectatorManager;
    }

    @EventHandler
    public void onPlayerUseChat(AsyncPlayerChatEvent e) {
        //ALL Chat
        if ((gameManager.getGameState() == GameState.WARM_UP || gameManager.getGameState() == GameState.RUNNING) && !spectatorManager.isSpectator(e.getPlayer())) {
            if (e.getMessage().charAt(0) == '@' && e.getMessage().charAt(1) == 'a' && e.getMessage().charAt(2) == ' ') {
                String message = e.getMessage().substring(3);
                gameManager.sendMessageToEveryone(
                        ChatColor.GRAY + "["+ ChatColor.WHITE + "ALL"+ ChatColor.GRAY +"] " + ChatColor.WHITE + e.getPlayer().getName() + ChatColor.DARK_GRAY + " : " + ChatColor.GRAY + message);
            } else {

                //Team Chat
                gameManager.sendMessageToTeam(gameManager.getPlayerTeamMap().get(e.getPlayer()),
                        String.format(Language.PLAYER_TEAM_NAME.getText(),
                                gameManager.getPlayerTeamMap().get(e.getPlayer()).getColor(),
                                gameManager.getPlayerTeamMap().get(e.getPlayer()).getId(),
                                e.getPlayer().getName()) + ChatColor.DARK_GRAY + " : " + ChatColor.GRAY + e.getMessage());
            }
            e.setCancelled(true);

            //Spectator Chat
        } else if ((gameManager.getGameState() == GameState.WARM_UP || gameManager.getGameState() == GameState.RUNNING) && spectatorManager.isSpectator(e.getPlayer())) {
            spectatorManager.sendMessageToSpectators(ChatColor.GRAY + "["+ ChatColor.WHITE + "SPECTATOR"+ ChatColor.GRAY +"] " + ChatColor.WHITE + e.getPlayer().getName() + ChatColor.DARK_GRAY + " : " + ChatColor.GRAY + e.getMessage());
            e.setCancelled(true);
        }
    }
}
