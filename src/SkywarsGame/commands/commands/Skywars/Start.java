package SkywarsGame.commands.commands.Skywars;

import SkywarsGame.commands.CCT;
import SkywarsGame.game.GameManager;
import SkywarsGame.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Start{
    private final GameManager gameManager;

    public Start(GameManager gameManager){
        this.gameManager = gameManager;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length == 0 && gameManager.getGameState() == GameState.LOBBY && Bukkit.getOnlinePlayers().size() >= gameManager.getABSOLUTE_MIN_PLAYERS()) {
            Bukkit.broadcastMessage("This command is just for demonstration purpose only and can crash everything.");
            gameManager.startWarmUp();
        }
        return false;
    }
}
