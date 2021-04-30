package SkywarsGame;

import SkywarsGame.commands.TestCommand;
import SkywarsGame.commands.commands.SkywarsCCT;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    //TODO: TabCompleter
    public void onEnable(){
        getLogger().info("Loading Skywars Plugin.");
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
        //getServer().getPluginManager().registerEvents(new SkywarsMapbuildListener(), this);
    }
}
