package SkywarsGame.commands;

import SkywarsGame.tools.MapGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
            new MapGame(strings[0]);
        return true;
    }
}
