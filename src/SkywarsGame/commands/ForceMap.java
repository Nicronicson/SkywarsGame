package SkywarsGame.commands;

import SkywarsGame.game.GameManager;
import SkywarsGame.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class ForceMap implements CommandExecutor {

    private final GameManager gameManager;

    public ForceMap(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(commandSender instanceof Player && args.length == 1) {
            gameManager.loadCustomMap(args[0], (Player) commandSender);
            return true;
        }
        return false;
    }
}
