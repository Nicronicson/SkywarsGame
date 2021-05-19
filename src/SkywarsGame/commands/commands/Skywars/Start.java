package SkywarsGame.commands.commands.Skywars;

import SkywarsGame.commands.CCT;
import SkywarsGame.game.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Start{
    private final GameManager gameManager;

    public Start(GameManager gameManager){
        this.gameManager = gameManager;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length == 0)
        {
            gameManager.startWarmUp();
        }
        return false;
    }
}
