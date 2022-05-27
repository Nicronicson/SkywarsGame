package SkywarsGame;

import SkywarsGame.chat.ChatListener;
//import SkywarsGame.commands.Cancel;
import SkywarsGame.commands.ForceMap;
//import SkywarsGame.commands.Start;
//import SkywarsGame.commands.TestCommand;
import SkywarsGame.game.GameListener;
import SkywarsGame.game.GameManager;
import SkywarsGame.lobbyItems.LobbyItemListener;
import SkywarsGame.spectator.SpectatorListener;
import SkywarsGame.spectator.SpectatorManager;
//import SkywarsGame.util.WorldProtectionListener;
import SkywarsGame.util.ProtectionListener;
import net.problemzone.Gringotts.Vaults.SkywarsVault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin {
    //TODO: -Nametags - Mapauswahl

    public static String PATH = "/root/CloudNet/local/templates/SkywarsAdmin/default/plugins/SkyWarsAdmin";

    private static JavaPlugin javaPlugin;

    private GameManager gameManager;
    private SpectatorManager spectatorManager;

    public void onEnable(){
        SkywarsVault skywarsVault = new SkywarsVault();
        skywarsVault.test();

        getLogger().info("Loading Skywars Plugin.");
        javaPlugin = this;

        gameManager = new GameManager();
        spectatorManager = new SpectatorManager(gameManager);

        getLogger().info("Loading Skywars Commands.");
        registerCommands();

        getLogger().info("Loading Skywars Listeners.");
        registerListeners();

        getLogger().info("Skywars startup completed.");
    }

    private void registerCommands(){
        Objects.requireNonNull(getCommand("forcemap")).setExecutor(new ForceMap(gameManager));
        //Objects.requireNonNull(getCommand("testcommand")).setExecutor(new TestCommand());
    }

    private void registerListeners(){
        getServer().getPluginManager().registerEvents(new ChatListener(gameManager, spectatorManager), this);
        getServer().getPluginManager().registerEvents(new SpectatorListener(spectatorManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new GameListener(spectatorManager, gameManager), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(gameManager, spectatorManager), this);
        getServer().getPluginManager().registerEvents(new LobbyItemListener(gameManager), this);
    }

    public static JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }
}
