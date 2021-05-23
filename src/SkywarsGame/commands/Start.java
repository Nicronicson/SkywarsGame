package SkywarsGame.commands;

import SkywarsGame.game.GameManager;
import SkywarsGame.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nonnull;

public class Start implements CommandExecutor {

    private final static int START_COMMAND_TIME = 10;

    private final GameManager gameManager;

    public Start(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(gameManager.getPlayerTeamMap().size() < gameManager.getABSOLUTE_MIN_PLAYERS()){
            Bukkit.broadcastMessage(Language.ERR_NOT_ENOUGH_PLAYERS.getFormattedText());
            return true;
        }

        if (args.length == 0) {
            gameManager.initiateWarmupCountdown(START_COMMAND_TIME);
            gameManager.setForcestart(true);
            return true;
        }

        if (args.length != 1) return false;
        if (!NumberUtils.isParsable(args[0])) return false;

        int time = Integer.parseInt(args[0]);
        gameManager.initiateWarmupCountdown(Math.max(time, 5));
        gameManager.setForcestart(true);
        return true;
    }
}
