package SkywarsGame.Chat;

import SkywarsGame.game.GameManager;
import SkywarsGame.util.Language;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class ChatListener implements Listener {
    GameManager gameManager;

    public ChatListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerUseChat(AsyncPlayerChatEvent e){

    }
}
