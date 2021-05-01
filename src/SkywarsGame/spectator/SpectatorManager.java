package SkywarsGame.spectator;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SpectatorManager {

    private final List<Player> spectators = new ArrayList<>();

    public void setPlayerAsSpectator(Player player){

        spectators.add(player);

        player.setPlayerListName(ChatColor.GRAY + player.getName() + ChatColor.RED + " ✗");
        player.setInvisible(true);
        player.setInvulnerable(true);
        player.setCollidable(false);
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    public boolean isSpectator(Player player){
        return spectators.contains(player);
    }

}
