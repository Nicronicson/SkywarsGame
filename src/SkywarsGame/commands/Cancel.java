package SkywarsGame.commands;

import SkywarsGame.game.GameManager;
import SkywarsGame.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

public class Cancel implements CommandExecutor {

    private final GameManager gameManager;

    public Cancel(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {

        if (args.length != 0) return false;
        gameManager.stopWarmupCountdown();
        Bukkit.broadcastMessage(Language.GAME_START_CANCEL.getFormattedText());
        return true;
    }

}
