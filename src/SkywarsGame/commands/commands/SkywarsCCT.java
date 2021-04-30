package SkywarsGame.commands.commands;

import SkywarsGame.commands.CCT;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SkywarsCCT extends CCT implements CommandExecutor{
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length != 0)
        {
            /*if(strings[0].equals("map")) {
                return new MapCCT().onCommand(commandSender, command, s, removeCommand(strings));
            }*/
        }
        return false;
    }
}