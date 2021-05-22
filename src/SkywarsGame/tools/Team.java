package SkywarsGame.tools;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Team {
    private int id;
    Set<Player> players;
    int kills;
    ChatColor color;

    public Team(int id) {
        players = new HashSet<>();
        this.id = id;
        while(id >= 16){
            id -= 16;
        }
        color = ChatColor.values()[id];
        kills = 0;
    }

    public void addKill(){
        kills++;
    }

    public int getKills(){
        return kills;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<Player> getPlayers(){
        return players;
    }

    public void addPlayer(Player player){
        players.add(player);
    }

    public ChatColor getColor() {
        return color;
    }

    public boolean isPlayerInTeam(Player player){
        return players.contains(player);
    }
}
