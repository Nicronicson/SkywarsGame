package SkywarsGame;

import SkywarsGame.commands.TestCommand;
import SkywarsGame.commands.commands.SkywarsCCT;
import SkywarsGame.game.GameListener;
import SkywarsGame.game.GameManager;
import SkywarsGame.scoreboard.ScoreboardListener;
import SkywarsGame.scoreboard.ScoreboardManager;
import SkywarsGame.spectator.SpectatorListener;
import SkywarsGame.spectator.SpectatorManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    //TODO: TabCompleter

    private static JavaPlugin javaPlugin;

    private final ScoreboardManager scoreboardManager = new ScoreboardManager();
    private final GameManager gameManager = new GameManager();
    private final SpectatorManager spectatorManager = new SpectatorManager(gameManager);

    public void onEnable(){
        getLogger().info("Loading Skywars Plugin.");
        javaPlugin = this;

        getLogger().info("Loading Skywars Commands.");
        registerCommands();

        getLogger().info("Loading Skywars Listeners.");
        registerListeners();

        getLogger().info("Essence primed and ready.");
    }

    private void registerCommands(){
        getCommand("skywars").setExecutor(new SkywarsCCT());
        getCommand("testcommand").setExecutor(new TestCommand());
    }

    private void registerListeners(){
        getServer().getPluginManager().registerEvents(new ScoreboardListener(scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new SpectatorListener(spectatorManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new GameListener(spectatorManager, gameManager), this);
    }

    public static JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }
}
