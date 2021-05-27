package SkywarsGame.lobbyItems;

import SkywarsGame.entities.KitGame;
import SkywarsGame.entities.Team;
import SkywarsGame.game.GameManager;
import SkywarsGame.game.GameState;
import SkywarsGame.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.Objects;

public class LobbyItemListener implements Listener {
    GameManager gameManager;

    public LobbyItemListener(GameManager gameManager){
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onUseLobbyItem(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            //Kit:
            if (e.getItem() != null && e.getItem().getItemMeta() != null && e.getItem().getItemMeta().getDisplayName().equals(ChatColor.YELLOW + KitGame.KITSELECTOR_NAME)) {
                openKitInventory(e.getPlayer());
            }
            //Team:
            if (e.getItem() != null && e.getItem().getItemMeta() != null && e.getItem().getItemMeta().getDisplayName().equals(ChatColor.YELLOW + Team.TEAMSELECTOR_NAME)) {
                openTeamInventory(e.getPlayer());
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

        Inventory inv = Bukkit.createInventory(null, invSize, ChatColor.YELLOW + KitGame.KITSELECTOR_NAME);

        Arrays.stream(gameManager.getTeams()).forEach(team -> inv.addItem(team.getItem()));

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

                //TODO: Kit bestätigen & Speichern --> Nachricht beim Auswählen senden
                if(kit.getDescription() != null) player.sendMessage(String.format(Language.CUSTOM.getFormattedText(), kit.getDescription()));
            }

            if (e.getView().getTitle().equals(ChatColor.YELLOW + Team.TEAMSELECTOR_NAME)) {
                Player player = (Player) e.getWhoClicked();
                int teamId = Character.getNumericValue(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName().charAt(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName().length() - 1));

                if(gameManager.setTeamOfPlayer(player, teamId)) player.sendMessage(Language.TEAM_CHANGE.getFormattedText());
                else player.sendMessage(Language.TEAM_VOLL.getFormattedText());

                player.closeInventory();

            }
        }
    }
}
