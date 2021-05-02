package SkywarsGame.tools;

import org.bukkit.entity.Player;

import java.util.Set;

public class Team {
    private int id;
    Set<Player> players;
    int kills;

    public Team(int id) {
        this.id = id;
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
}
