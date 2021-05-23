package SkywarsGame;

import SkywarsGame.Chat.ChatListener;
import SkywarsGame.commands.Cancel;
import SkywarsGame.commands.Start;
import SkywarsGame.game.GameListener;
import SkywarsGame.game.GameManager;
import SkywarsGame.spectator.SpectatorListener;
import SkywarsGame.spectator.SpectatorManager;
import SkywarsGame.util.WorldProtectionListener;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    //TODO: TabCompleter

    private static JavaPlugin javaPlugin;

    private GameManager gameManager;
    private SpectatorManager spectatorManager;

    public void onEnable(){
        getLogger().info("Loading Skywars Plugin.");
        javaPlugin = this;

        //Set GAMERULES of Lobby
        World world = Bukkit.getWorld("Lobby");
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(1000L);
        world.setClearWeatherDuration(Integer.MAX_VALUE);

        gameManager = new GameManager();
        spectatorManager = new SpectatorManager(gameManager);

        getLogger().info("Loading Skywars Commands.");
        registerCommands();

        getLogger().info("Loading Skywars Listeners.");
        registerListeners();

        getLogger().info("Essence primed and ready.");
    }

    private void registerCommands(){
        getCommand("skywars").setExecutor(new Start(gameManager));
        getCommand("skywars").setExecutor(new Cancel(gameManager));
        //getCommand("testcommand").setExecutor(new TestCommand());
    }

    private void registerListeners(){
        getServer().getPluginManager().registerEvents(new ChatListener(gameManager, spectatorManager), this);
        getServer().getPluginManager().registerEvents(new SpectatorListener(spectatorManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new GameListener(spectatorManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new WorldProtectionListener(gameManager, spectatorManager), this);
    }

    public static JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }
}
