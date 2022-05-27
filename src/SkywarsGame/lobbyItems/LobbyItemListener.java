package SkywarsGame.lobbyItems;

import SkywarsGame.entities.KitGame;
import SkywarsGame.entities.Maps;
import SkywarsGame.entities.Team;
import SkywarsGame.game.GameManager;
import SkywarsGame.game.GameState;
import SkywarsGame.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LobbyItemListener implements Listener {
    GameManager gameManager;

    public LobbyItemListener(GameManager gameManager){
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onUseLobbyItem(PlayerInteractEvent e) {
        if (gameManager.getGameState() == GameState.LOBBY && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)) {
            //Kit:
            if (e.getItem() != null && e.getItem().getItemMeta() != null && e.getItem().getItemMeta().getDisplayName().equals(ChatColor.YELLOW + KitGame.KITSELECTOR_NAME)) {
                openKitInventory(e.getPlayer());
            }
            //Team:
            if (e.getItem() != null && e.getItem().getItemMeta() != null &&
                    e.getItem().getItemMeta().getDisplayName().length() >= (ChatColor.YELLOW + Team.TEAMSELECTOR_NAME).length() &&
                    e.getItem().getItemMeta().getDisplayName().startsWith(ChatColor.YELLOW + Team.TEAMSELECTOR_NAME)) {
                openTeamInventory(e.getPlayer());
            }
            //Map:
            if (e.getItem() != null && e.getItem().getItemMeta() != null && e.getItem().getItemMeta().getDisplayName().equals(ChatColor.YELLOW + Maps.MAPSELECTOR_NAME)) {
                openMapInventory(e.getPlayer());
            }
        }
    }

    private void openKitInventory(Player player){
        int invSize = 9;

        while(gameManager.kitsSize() > invSize)
            invSize += 9;

        Inventory inv = Bukkit.createInventory(null, invSize, ChatColor.YELLOW + KitGame.KITSELECTOR_NAME);

        gameManager.getKits().forEach(kit -> inv.addItem(kit.getItem()));

        player.openInventory(inv);
    }

    private void openTeamInventory(Player player){
        int invSize = 9;

        while(gameManager.getTeams().length > invSize)
            invSize += 9;

        Inventory inv = Bukkit.createInventory(null, invSize, ChatColor.YELLOW + Team.TEAMSELECTOR_NAME);

        Arrays.stream(gameManager.getTeams()).forEachOrdered(team -> inv.addItem(team.getItem()));

        player.openInventory(inv);
    }

    private void openMapInventory(Player player){
        int invSize = 9;

        Inventory inv = Bukkit.createInventory(null, invSize, ChatColor.YELLOW + Maps.MAPSELECTOR_NAME);

        List<ItemStack> maps = new ArrayList<>();
        gameManager.getMaps().getMaps().forEach(mapname -> maps.add(gameManager.getMaps().getItem(mapname)));
        Collections.shuffle(maps);

        inv.setItem(0, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));

        inv.setItem(2, maps.get(0));

        inv.setItem(3, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));

        if(maps.size() > 1){
            inv.setItem(4, maps.get(1));
        } else {
            inv.setItem(4, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }

        inv.setItem(5, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));

        if(maps.size() > 2){
            inv.setItem(6, maps.get(2));
        } else {
            inv.setItem(6, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }

        inv.setItem(7, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        inv.setItem(8, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (gameManager.getGameState() == GameState.LOBBY && e.getCurrentItem() != null) {
            if (e.getView().getTitle().equals(ChatColor.YELLOW + KitGame.KITSELECTOR_NAME)) {
                Player player = (Player) e.getWhoClicked();
                KitGame kit = gameManager.kitsGetByName(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName());
                gameManager.setKitOfPlayer(player, kit);
                player.closeInventory();
                player.sendMessage(Language.KIT_CHANGE.getFormattedText());

                //TODO: Kit Speichern
                if(kit.getDescription() != null) player.sendMessage(String.format(Language.CUSTOM.getFormattedText(), kit.getDescription()));
            }

            if (e.getView().getTitle().equals(ChatColor.YELLOW + Team.TEAMSELECTOR_NAME)) {
                Player player = (Player) e.getWhoClicked();

                int teamId = Integer.parseInt(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName().substring((ChatColor.YELLOW + "Team ").length()));

                if(gameManager.setTeamOfPlayer(player, teamId)){

                    //Set coloured KitSelector
                    player.getInventory().setItem(1, Team.getTeamSelector(gameManager.getTeams()[teamId].getColor(), teamId));

                    player.sendMessage(Language.TEAM_CHANGE.getFormattedText());
                }
                else player.sendMessage(Language.TEAM_FULL.getFormattedText());

                player.closeInventory();

            }
        }
    }
}
