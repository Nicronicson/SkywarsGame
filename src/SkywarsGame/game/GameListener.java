package SkywarsGame.game;

import SkywarsGame.Main;
import SkywarsGame.util.Language;
import SkywarsGame.spectator.SpectatorManager;
import SkywarsGame.util.Sounds;
import net.problemzone.lobbibi.modules.events.GameStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.Arrays;
import java.util.Objects;

public class GameListener implements Listener {

    private final SpectatorManager spectatorManager;
    private final GameManager gameManager;

    public GameListener(SpectatorManager spectatorManager, GameManager gameManager) {
        this.spectatorManager = spectatorManager;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onGameStartEvent(GameStartEvent e){
        gameManager.startWarmUp();
    }

    @EventHandler
    public void onPlayerSpawnLocation(PlayerSpawnLocationEvent e) {
        //Determine PlayerProperties
        switch (gameManager.getGameState()) {
            /*case PREPARING:
                e.getPlayer().kickPlayer(Language.ERR_TRY_TO_REJOIN.getFormattedText());
                break;
            Not needed - look at default branch*/

            case WARM_UP:

            case RUNNING:
                spectatorManager.joinAsSpectator(e);
                break;

            case LOBBY:
                gameManager.joinGame(e.getPlayer());

            /*case FINISHED:
                gameManager.joinLobby(e);
                break;
            */
            default:
                e.getPlayer().kickPlayer(Language.ERR_TRY_TO_REJOIN.getFormattedText());
                break;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        //Custom join message
        if (gameManager.getGameState() == GameState.LOBBY/* || gameManager.getGameState() == GameState.FINISHED*/) {
            e.setJoinMessage(String.format(Language.PLAYER_JOIN.getText(), e.getPlayer().getName()));
        } else {
            e.setJoinMessage("");
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        //Custom leave message
        if (!spectatorManager.isSpectator(e.getPlayer())) {
            e.setQuitMessage(String.format(Language.PLAYER_LEAVE.getText(), e.getPlayer().getName()));
        } else {
            e.setQuitMessage("");
        }

        //Remove Player from all important Game entries
        removePlayer(e.getPlayer(), false);
    }

    @EventHandler
    public void onPlayerGetHurt(EntityDamageEvent e) {
        EntityDamageByEntityEvent eBYe = e instanceof EntityDamageByEntityEvent /* e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK*/ ? (EntityDamageByEntityEvent) e : null;

        //Bukkit.broadcastMessage("Hurt");

        if (e.getEntity() instanceof Player) {
            switch (gameManager.getGameState()) {
                //Cancel damaged received from other players in the Warmup phase
                case WARM_UP:
                    if (eBYe != null && eBYe.getDamager() instanceof Player) {
                        e.setCancelled(true);
                    }

                    //Checking if Player would die
                    checkDying(e);

                    break;

                case RUNNING:
                    //Cancel damage from teammates and Spectators
                    if ((eBYe != null && eBYe.getDamager() instanceof Player && (gameManager.getPlayerTeamMap().get((Player) eBYe.getEntity()).isPlayerInTeam((Player) eBYe.getDamager()) || spectatorManager.isSpectator((Player) eBYe.getDamager()))) ||
                            (e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE && ((Projectile) ((EntityDamageByEntityEvent) e).getDamager()).getShooter() instanceof Player && gameManager.getPlayerTeamMap().get((Player) e.getEntity()).isPlayerInTeam(((Player) ((Projectile) ((EntityDamageByEntityEvent) e).getDamager()).getShooter())))) {
                        e.setCancelled(true);
                        break;
                    }

                    //Save last damager
                    Player player = (Player) e.getEntity();
                    Player damager = null;

                    if (eBYe != null && eBYe.getDamager() instanceof Player) {
                        damager = (Player) eBYe.getDamager();
                    }

                    if (e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                        EntityDamageByEntityEvent eBYp = (EntityDamageByEntityEvent) e;
                        if (((Projectile) eBYp.getDamager()).getShooter() instanceof Player) {
                            damager = (Player) ((Projectile) eBYp.getDamager()).getShooter();
                        }
                    }

                    if (damager != null) {
                        if (gameManager.getLastDamager().containsKey(player)) {
                            gameManager.getLastDamager().replace(player, damager);
                        } else {
                            gameManager.getLastDamager().put(player, damager);
                        }
                    }

                    //Bukkit.broadcastMessage("Check Dying");

                    //Checking if Player would die
                    checkDying(e);

                    break;

                /*case PREPARING:
                    //Cancel everything which happens in this phase
                    e.setCancelled(true);
                    break;

                case LOBBY:
                    //Teleport player to spawn when falling in Lobby
                /*case FINISHED:
                    if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                        e.setCancelled(true);
                        gameManager.teleportToLobby((Player) e.getEntity());
                    }
                    break;
                 */
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        //hinder Players in AntiFallTime from moving
        if (gameManager.isAntiFallTime() && gameManager.getPlayerTeamMap().containsKey(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    private void removePlayer(Player player, boolean spectator) {
        if (!spectator) {
            spectatorManager.removeSpectator(player);
        }
        gameManager.removePlayer(player, spectator);
    }

    private void checkDying(EntityDamageEvent e) {
        Player player = (Player) e.getEntity();
        if (player.getHealth() - e.getFinalDamage() <= 0) {
            //Increase Kill Counter if it was caused by a Player
            String deathMessage = null;
            if (gameManager.getLastDamager().containsKey(player) && gameManager.getPlayerTeamMap().get(player) != null) {
                gameManager.getPlayerTeamMap().get(gameManager.getLastDamager().get(player)).addKill();
                gameManager.updateKillCounter(gameManager.getLastDamager().get(player));

                //Play death sound
                Sounds.DEATH.playSoundForPlayer(player);

                //Play kill sound
                Sounds.KILL.playSoundForPlayer(gameManager.getLastDamager().get(player));

                deathMessage = String.format(Language.DEATH_BY_PLAYER.getFormattedText(),
                        String.format(Language.PLAYER_TEAM_NAME.getText(), gameManager.getPlayerTeamMap().get(player).getColor(), gameManager.getPlayerTeamMap().get(player).getId(), player.getName()),
                        String.format(Language.PLAYER_TEAM_NAME.getText(), gameManager.getPlayerTeamMap().get(gameManager.getLastDamager().get(player)).getColor(), gameManager.getPlayerTeamMap().get(gameManager.getLastDamager().get(player)).getId(), gameManager.getLastDamager().get(player).getName()));

                //Send the health of the killer
                int health = (int) gameManager.getLastDamager().get(player).getHealth();
                StringBuilder healthString = new StringBuilder();
                healthString.append((ChatColor.RED + "♥").repeat(health / 2));
                if(health % 2 != 0){
                    healthString.append(ChatColor.YELLOW + "♥");
                }
                healthString.append((ChatColor.GRAY + "♥").repeat((20 - health) / 2));

                player.sendMessage(String.format(Language.PLAYER_HEALTH.getText(), String.format(Language.PLAYER_TEAM_NAME.getText(), gameManager.getPlayerTeamMap().get(gameManager.getLastDamager().get(player)).getColor(), gameManager.getPlayerTeamMap().get(gameManager.getLastDamager().get(player)).getId(), gameManager.getLastDamager().get(player).getName()), healthString));

            } else {
                deathMessage = String.format(Language.DEATH.getFormattedText(), String.format(Language.PLAYER_TEAM_NAME.getText(), gameManager.getPlayerTeamMap().get(player).getColor(), gameManager.getPlayerTeamMap().get(player).getId(), player.getName()));
            }

            //drop the items
            if(!player.getInventory().getItemInOffHand().getType().isAir())
                player.getWorld().dropItemNaturally(player.getLocation(), player.getInventory().getItemInOffHand());

            if(player.getInventory().getHelmet() != null)
                player.getWorld().dropItemNaturally(player.getLocation(), player.getInventory().getHelmet());

            if(player.getInventory().getChestplate() != null)
                player.getWorld().dropItemNaturally(player.getLocation(), player.getInventory().getChestplate());

            if(player.getInventory().getLeggings() != null)
                player.getWorld().dropItemNaturally(player.getLocation(), player.getInventory().getLeggings());

            if(player.getInventory().getBoots() != null)
                player.getWorld().dropItemNaturally(player.getLocation(), player.getInventory().getBoots());

            Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).forEach(itemStack -> player.getWorld().dropItemNaturally(player.getLocation(), itemStack));

            gameManager.sendMessageToEveryone(deathMessage);

            //Let the player respawn as a Spectator & Remove player when he dies
            saveRespawn(player, e.getCause() == EntityDamageEvent.DamageCause.VOID);

            //Cancel damage from original event
            e.setCancelled(true);
        }
    }

    private void saveRespawn(Player player, boolean voidDamage){
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    //Let the player respawn as a Spectator
                    spectatorManager.RespawnAsSpectator(player, voidDamage);
                    removePlayer(player, true);
                } catch (Exception e){
                    saveRespawn(player, voidDamage);
                }
            }
        }.runTaskLater(Main.getJavaPlugin(), 1L);
    }
}
