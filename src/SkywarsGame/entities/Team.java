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

    public static ItemStack getTeamSelector() {
        ItemStack teamSelector = new ItemStack(Material.WHITE_BED, 1);
        ItemMeta teamSelectorItemMeta = teamSelector.getItemMeta();

        assert teamSelectorItemMeta != null;
        teamSelectorItemMeta.setDisplayName(ChatColor.YELLOW + TEAMSELECTOR_NAME);
        teamSelector.setItemMeta(teamSelectorItemMeta);

        return teamSelector;
    }

    public static ItemStack getTeamSelector(ChatColor color, int teamId) {
        ItemStack teamSelector = new ItemStack(visualColor.get(color), 1);
        ItemMeta teamSelectorItemMeta = teamSelector.getItemMeta();

        assert teamSelectorItemMeta != null;
        teamSelectorItemMeta.setDisplayName(ChatColor.YELLOW + TEAMSELECTOR_NAME + " (" + color + "Team " + teamId + ChatColor.YELLOW + ")");
        teamSelector.setItemMeta(teamSelectorItemMeta);

        return teamSelector;
    }

    private static final HashMap<Integer, ChatColor> colorIdMap = new HashMap<>();
    private static final HashMap<ChatColor, Material> visualColor = new HashMap<>();

    static {
        colorIdMap.put(0, ChatColor.BLUE);
        visualColor.put(ChatColor.BLUE, Material.BLUE_BED);

        colorIdMap.put(4, ChatColor.AQUA);
        visualColor.put(ChatColor.AQUA, Material.LIGHT_BLUE_BED);

        colorIdMap.put(5, ChatColor.DARK_GREEN);
        visualColor.put(ChatColor.DARK_GREEN, Material.GREEN_BED);

        colorIdMap.put(2, ChatColor.GREEN);
        visualColor.put(ChatColor.GREEN, Material.LIME_BED);
        
        colorIdMap.put(1, ChatColor.RED);
        visualColor.put(ChatColor.RED, Material.RED_BED);

        colorIdMap.put(6, ChatColor.DARK_PURPLE);
        visualColor.put(ChatColor.DARK_PURPLE, Material.PURPLE_BED);

        colorIdMap.put(7, ChatColor.LIGHT_PURPLE);
        visualColor.put(ChatColor.LIGHT_PURPLE, Material.PINK_BED);

        colorIdMap.put(8, ChatColor.GOLD);
        visualColor.put(ChatColor.GOLD, Material.ORANGE_BED);

        colorIdMap.put(3, ChatColor.YELLOW);
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
        while (id >= colorIdMap.size()) {
            id -= colorIdMap.size();
        }
        color = colorIdMap.get(id);
        visual = new ItemStack(visualColor.get(color));
        kills = 0;
    }

    public void addKill() {
        kills++;
    }

    public int getKills() {
        return kills;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public ChatColor getColor() {
        return color;
    }

    public boolean isPlayerInTeam(Player player) {
        return players.contains(player);
    }

    public ItemStack getItem() {
        ItemStack itemStack = visual.clone();
        ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(color + "Team " + id);

        itemMeta.setLore(Collections.singletonList(ChatColor.WHITE + "(" + players.size() + "/" + gameManager.getMapGame().getTeamsize() + ")"));

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
