package SkywarsGame;

import SkywarsGame.commands.TestCommand;
import SkywarsGame.commands.commands.SkywarsCCT;
import SkywarsGame.game.GameListener;
import SkywarsGame.game.GameManager;
import SkywarsGame.scoreboard.ScoreboardListener;
import SkywarsGame.spectator.SpectatorListener;
import SkywarsGame.spectator.SpectatorManager;
import SkywarsGame.tools.WorldProtectionListener;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    //TODO: TabCompleter

    private static JavaPlugin javaPlugin;

    private final GameManager gameManager = new GameManager();
    private final SpectatorManager spectatorManager = new SpectatorManager(gameManager);

    public void onEnable(){
        getLogger().info("Loading Skywars Plugin.");
        javaPlugin = this;

        getLogger().info("Load Troubles Worlds.");
        loadWorlds();

        getLogger().info("Loading Skywars Commands.");
        registerCommands();

        getLogger().info("Loading Skywars Listeners.");
        registerListeners();

        getLogger().info("Essence primed and ready.");
    }

    private void loadWorlds() {
        getServer().createWorld(new WorldCreator("railroad"));
    }

    private void registerCommands(){
        getCommand("skywars").setExecutor(new SkywarsCCT(gameManager));
        getCommand("testcommand").setExecutor(new TestCommand());
    }

    private void registerListeners(){
        getServer().getPluginManager().registerEvents(new ScoreboardListener(gameManager), this);
        getServer().getPluginManager().registerEvents(new SpectatorListener(spectatorManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new GameListener(spectatorManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new WorldProtectionListener(gameManager), this);
    }

    public static JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }
}
