package SkywarsGame.entities;

import SkywarsGame.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Team {
    public static String TEAMSELECTOR_NAME = "Teamauswahl";
    public static ItemStack getTeamSelector(){
        ItemStack teamSelector = new ItemStack(Material.BLUE_BED, 1);
        ItemMeta teamSelectorItemMeta = teamSelector.getItemMeta();

        assert teamSelectorItemMeta != null;
        teamSelectorItemMeta.setDisplayName(ChatColor.YELLOW + TEAMSELECTOR_NAME);
        teamSelector.setItemMeta(teamSelectorItemMeta);

        return teamSelector;
    }

    private static final HashMap<ChatColor, Material> visualColor = new HashMap<>();

    static {
        visualColor.put(ChatColor.BLUE, Material.BLUE_BED);
        visualColor.put(ChatColor.GREEN, Material.GREEN_BED);
        visualColor.put(ChatColor.AQUA, Material.LIGHT_BLUE_BED);
        visualColor.put(ChatColor.RED, Material.RED_BED);
        visualColor.put(ChatColor.LIGHT_PURPLE, Material.PINK_BED);
        visualColor.put(ChatColor.YELLOW, Material.YELLOW_BED);
    }

    private int id;
    Set<Player> players;
    int kills;
    ChatColor color;
    ItemStack visual;
    GameManager gameManager;

    public Team(int id, GameManager gameManager) {
        this.gameManager = gameManager;
        players = new HashSet<>();
        this.id = id;
        while(id >= 6){
            id -= 6;
        }
        id += 9;
        color = ChatColor.values()[id];
        visual = new ItemStack(visualColor.get(color));
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

    public void removePlayer(Player player){
        players.remove(player);
    }

    public ChatColor getColor() {
        return color;
    }

    public boolean isPlayerInTeam(Player player){
        return players.contains(player);
    }

    public ItemStack getItem() {
        ItemStack itemStack = visual.clone();
        ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(ChatColor.RED + "Team " + id);

        itemMeta.setLore(Collections.singletonList("(" + players.size() + "/" + gameManager.getMapGame().getTeamsize() + ")"));

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
