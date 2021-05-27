package SkywarsGame.game;

import SkywarsGame.Main;
import SkywarsGame.util.Countdown;
import SkywarsGame.util.Language;
import SkywarsGame.entities.KitGame;
import SkywarsGame.entities.Lobby;
import SkywarsGame.entities.MapGame;
import SkywarsGame.entities.Team;
import SkywarsGame.util.Sounds;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.io.File;
import java.util.*;

public class GameManager {
    private final int LOBBY_WAIT_TIME = 30;
    private final int WARM_UP_TIME = 50;
    private final int MIN_PLAYERS = 4;
    private final int ABSOLUTE_MIN_PLAYERS = 2;

    private final String LOBBY_NAME = "Lobby";

    private MapGame mapGame;

    boolean warmupCountdownActive;

    boolean forcestart;

    private BukkitTask currentScheduledTask;

    private final Map<Player, Team> playerTeamMap;
    private final Map<Player, KitGame> playerKitMap;
    private final HashMap<Player, Player> lastDamager;
    private GameState gameState;
    private final Team[] teams;
    private final Set<KitGame> kits;

    public GameManager() {
        forcestart = false;
        warmupCountdownActive = false;
        playerTeamMap = new HashMap<>();
        playerKitMap = new HashMap<>();
        gameState = GameState.PREPARING;
        loadMap(new Lobby().getStandartMap());
        lastDamager = new HashMap<>();

        kits = new HashSet<>();
        loadKits();

        teams = new Team[mapGame.getSpawnpoints().size()];
        for (int i = 0; i < mapGame.getSpawnpoints().size(); i++) {
            teams[i] = new Team(i, this);
        }

        gameState = GameState.LOBBY;
    }

    //load:

    private void loadKits() {
        String pathname = "./plugins/SkyWarsAdmin/Kit";
        for (File file : Objects.requireNonNull(new File(pathname).listFiles())) {
            kits.add(new KitGame(file.getAbsolutePath()));
        }
    }

    public boolean loadMap(String mapName) {
        if (new File("./" + mapName).exists()) {

            Main.getJavaPlugin().getLogger().info("Map was found");

            Main.getJavaPlugin().getServer().createWorld(new WorldCreator(mapName));

            //Set GAMERULES
            World world = Bukkit.getWorld(mapName);
            assert world != null;
            world.setDifficulty(Difficulty.NORMAL);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);

            mapGame = new MapGame(mapName);

            return true;
        }
        Main.getJavaPlugin().getLogger().info("Map wasn't found");
        return false;
    }

    //TODO: implement Command
    public void loadCustomMap(String mapName, Player player) {
        if (gameState == GameState.LOBBY) {
            if (loadMap(mapName)) {
                player.sendMessage(Language.MAP_CHANGED.getFormattedText());
            } else {
                player.sendMessage(Language.ERR_MAP_NOT_FOUND.getFormattedText());
            }
        }
    }

    //join/teleport to lobby

    public void joinLobby(PlayerSpawnLocationEvent e) {
        Player player = e.getPlayer();

        //Teleport to LobbySpawn
        Location lobbySpawn = Objects.requireNonNull(Bukkit.getWorld(LOBBY_NAME)).getSpawnLocation();
        lobbySpawn.setWorld(Bukkit.getWorld(LOBBY_NAME));

        e.setSpawnLocation(lobbySpawn);

        setPlayerInLobbyMode(player);
    }

    public void teleportToLobby(Player player) {
        //Teleport to LobbySpawn
        Location lobbySpawn = Objects.requireNonNull(Bukkit.getWorld(LOBBY_NAME)).getSpawnLocation();
        lobbySpawn.setWorld(Bukkit.getWorld(LOBBY_NAME));

        player.teleport(lobbySpawn);

        setPlayerInLobbyMode(player);
    }

    //join game

    public void joinGame(Player player) {
        //Stuff which is needed before a game starts
        playerTeamMap.put(player, null);
        playerKitMap.put(player, null);

        //Check if there are enough players to start
        if (enoughPlayers()) {
            if (currentScheduledTask == null || currentScheduledTask.isCancelled()) {
                forcestart = false;
                initiateWarmupCountdown();
            }
        } else {
            broadcastNeededPlayers();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    setInitialScoreboard(player);
                    setPlayerCount();
                } catch (Exception e){
                    Bukkit.broadcastMessage(e.getMessage());
                }
            }
        }.runTaskLater(Main.getJavaPlugin(), 2L);

        player.getInventory().setItem(0, KitGame.getKitSelector());

        player.getInventory().setItem(1, Team.getTeamSelector());
    }

    //Teams:

    private void distributePlayersOnTeams() {
        for (Map.Entry<Player, Team> teamEntry : playerTeamMap.entrySet()) {
            if (teamEntry.getValue() == null) {
                boolean ready = false;
                int i = 0;
                while (!ready) {
                    try {
                        if (teams[i].getPlayers().size() < mapGame.getTeamsize()) {
                            setTeamOfPlayer(teamEntry.getKey(), i);
                            ready = true;
                        }
                    } catch (Exception e) {
                        Bukkit.broadcastMessage(Language.GENERAL_ERROR.getFormattedText());
                        return;
                    }
                    i++;
                }
            }
        }
    }

    public boolean setTeamOfPlayer(Player player, int teamID) {
        if(teams[teamID].getPlayers().size() < mapGame.getTeamsize()) {
            if (playerTeamMap.get(player) != null)
                playerTeamMap.get(player).removePlayer(player);

            teams[teamID].addPlayer(player);
            playerTeamMap.replace(player, teams[teamID]);
            return true;
        }
        return false;
    }

    //Kits:

    public void giveEveryPlayerAKit() {
        playerKitMap.forEach((player, kit) -> {
            String standardKit = ChatColor.RED + "Standart";

            if (kit == null) {
                assert kits.isEmpty();
                setKitOfPlayer(player, kitsContains(standardKit) ? kitsGetByName(standardKit) : (KitGame) kits.toArray()[0]);
            }
        });
    }

    public void setKitOfPlayer(Player player, KitGame kit) {
        playerKitMap.replace(player, kit);
        setKitDisplay(player);
    }

    //Starts Warm Up Phase
    public void startWarmUp() {
        gameState = GameState.PREPARING;

        //Disable Lobby Plugin:
        if (Bukkit.getPluginManager().getPlugin("Lobbibi") != null)
            Bukkit.getPluginManager().disablePlugin(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Lobbibi")));

        distributePlayersOnTeams();

        giveEveryPlayerAKit();

        //For every Player
        playerTeamMap.forEach((player, team) -> {
            //Set InitialScoreboards
            //setInitialScoreboard(player);

            //Clear all Inventories
            player.getInventory().clear();

            //Set everyone's color
            player.setPlayerListName(String.format(Language.PLAYER_TEAM_NAME.getText(), team.getColor(), team.getId(), player.getName()));
            //player.setPlayerListName("[" + team.getColor() + "T" + team.getId() + ChatColor.WHITE + "] " + ChatColor.GREEN + player.getName());

            setPlayerInGameMode(player);

            //Set XP for the case that the countdown fails
            player.setLevel(0);
            player.setExp(0);
        });

        mapGame.start(playerTeamMap);

        distributeKitItems();

        gameState = GameState.WARM_UP;

        initiateStartCountdown();
    }

    private void distributeKitItems() {
        try {
            for (Map.Entry<Player, KitGame> playerKitEntry : playerKitMap.entrySet()) {
                if (playerKitEntry.getValue() != null) {

                    ItemStack helmet = playerKitEntry.getValue().getHelmet();
                    if (helmet != null) {
                        playerKitEntry.getValue().getHelmetENC().forEach((key, value) -> helmet.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(key))), value));
                        playerKitEntry.getKey().getInventory().setHelmet(helmet);
                    }

                    ItemStack chestplate = playerKitEntry.getValue().getChestplate();
                    if (chestplate != null) {
                        playerKitEntry.getValue().getChestplateENC().forEach((key, value) -> chestplate.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(key))), value));
                        playerKitEntry.getKey().getInventory().setChestplate(chestplate);
                    }

                    ItemStack leggings = playerKitEntry.getValue().getLeggings();
                    if (leggings != null) {
                        playerKitEntry.getValue().getLeggingsENC().forEach((key, value) -> leggings.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(key))), value));
                        playerKitEntry.getKey().getInventory().setLeggings(leggings);
                    }

                    ItemStack boots = playerKitEntry.getValue().getBoots();
                    if (boots != null) {
                        playerKitEntry.getValue().getBootsENC().forEach((key, value) -> boots.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(key))), value));
                        playerKitEntry.getKey().getInventory().setBoots(boots);
                    }

                    ItemStack leftHand = playerKitEntry.getValue().getLeftHand();
                    if (leftHand != null) {
                        playerKitEntry.getValue().getLeftHandENC().forEach((key, value) -> leftHand.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(key))), value));
                        playerKitEntry.getKey().getInventory().setItemInOffHand(leftHand);
                    }

                    int i = 0;
                    for (ItemStack itemSlot : playerKitEntry.getValue().getInventory()) {
                        if (itemSlot != null) {
                            playerKitEntry.getValue().getInventoryENC().get(i).forEach((key, value) -> itemSlot.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(key))), value));
                            playerKitEntry.getKey().getInventory().setItem(i, itemSlot);
                        }
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.broadcastMessage(e.getMessage());
        }
    }

    //Starts Action Phase
    private void startAction() {
        gameState = GameState.RUNNING;
    }

    //Player needed for starting:

    public boolean enoughPlayers() {
        if (isForcestart())
            return playerTeamMap.size() >= ABSOLUTE_MIN_PLAYERS;
        else
            return playerTeamMap.size() >= MIN_PLAYERS;
    }

    private int neededPlayers() {
        return MIN_PLAYERS - Bukkit.getOnlinePlayers().size();
    }

    public void broadcastNeededPlayers() {
        new BukkitRunnable() {
            @Override
            public void run() {
                int neededPlayers = neededPlayers();
                if (neededPlayers == 1) {
                    Bukkit.broadcastMessage(Language.PLAYERS_NEEDED_ONE.getFormattedText());
                } else if (neededPlayers > 1) {
                    Bukkit.broadcastMessage(String.format(Language.PLAYERS_NEEDED.getFormattedText(), neededPlayers));
                }
            }
        }.runTaskLater(Main.getJavaPlugin(), 2L);
    }

    //Countdowns:

    private void initiateWarmupCountdown() {
        initiateWarmupCountdown(LOBBY_WAIT_TIME);
    }

    public void initiateWarmupCountdown(int seconds) {
        if (gameState != GameState.LOBBY)
            return;

        if (currentScheduledTask != null && !currentScheduledTask.isCancelled()) currentScheduledTask.cancel();

        currentScheduledTask = new BukkitRunnable() {
            @Override
            public void run() {
                startWarmUp();
            }
        }.runTaskLater(Main.getJavaPlugin(), seconds * 20L);

        Countdown.createLevelCountdown(seconds, Language.GAME_START_TITLE);
        Countdown.createXpBarCountdown(seconds);
        Countdown.createChatCountdown(seconds, Language.GAME_START);
    }

    private void initiateStartCountdown() {
        int seconds = WARM_UP_TIME;

        if (currentScheduledTask != null && !currentScheduledTask.isCancelled()) currentScheduledTask.cancel();

        currentScheduledTask = new BukkitRunnable() {
            @Override
            public void run() {
                startAction();
            }
        }.runTaskLater(Main.getJavaPlugin(), seconds * 20L);

        Countdown.createChatCountdown(seconds, Language.WARM_UP);
    }

    public void stopWarmupCountdown() {
        if (gameState != GameState.LOBBY)
            return;

        //Cancel Scheduler
        if (currentScheduledTask != null && !currentScheduledTask.isCancelled()) currentScheduledTask.cancel();

        //Cancel Countdown
        Countdown.cancelLevelCountdown();
        Countdown.cancelXpBarCountdown();
        Countdown.cancelChatCountdown();
    }

    //Winning Related Methods:

    public void removePlayer(Player player) {
        playerTeamMap.remove(player);
        playerKitMap.remove(player);

        switch (gameState) {
            case WARM_UP:
            case RUNNING:
                setPlayerCount();
                checkForWin();
                break;
            case LOBBY:
                if (!enoughPlayers()) {
                    broadcastNeededPlayers();
                    stopWarmupCountdown();
                }
        }
    }

    private void checkForWin() {
        Team team = null;
        boolean anderesTeam = false;
        for (Map.Entry<Player, Team> mapEntry : playerTeamMap.entrySet()) {
            if (team == null) {
                team = mapEntry.getValue();
            } else if (mapEntry.getValue() != team) {
                anderesTeam = true;
            }
        }
        if (!anderesTeam) {
            assert team != null;
            announceWin(team);
        }
    }

    private void announceWin(Team team) {
        gameState = GameState.PREPARING;

        //Teleports everybody back in the Lobby and Sets everyone in LobbyMode
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.getInventory().clear();
            teleportToLobby(player);
        });

        Bukkit.broadcastMessage(Language.GAME_FINISHED.getText());

        StringBuilder playerNames = new StringBuilder();
        team.getPlayers().forEach(player -> {
            playerNames.append(String.format(Language.PLAYER_TEAM_NAME.getText(), playerTeamMap.get(player).getColor(), playerTeamMap.get(player).getId(), player.getName()));
            playerNames.append(", ");
        });
        playerNames.delete(playerNames.lastIndexOf(","), playerNames.lastIndexOf(",") + 1);

        if (team.getPlayers().size() == 1) {
            Bukkit.broadcastMessage(String.format(Language.ANNOUNCE_WIN_PLAYER.getFormattedText(), playerNames));
        } else {
            Bukkit.broadcastMessage(String.format(Language.ANNOUNCE_WIN_PLAYERS.getFormattedText(), playerNames));
        }

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendTitle(String.format(Language.ANNOUNCE_WIN_TEAM.getTitleText(), team.getColor(), team.getId()), "", 10, 60, 10);

            if (team.getPlayers().contains(player)) {
                //Win sound
                Sounds.WIN.playSoundForPlayer(player);
                //player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.AMBIENT, 1, 1.1F);
            } else {
                //Lose sound
                Sounds.LOSE.playSoundForPlayer(player);
                //player.playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, SoundCategory.AMBIENT, 1, 0.5F);
            }

        });
        gameState = GameState.FINISHED;

        new BukkitRunnable() {
            @Override
            public void run() {
                //Bukkit.getOnlinePlayers().forEach(player -> Bukkit.getServer().dispatchCommand(player, "hub")); //TODO: doesn't work that way
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop");
            }
        }.runTaskLater(Main.getJavaPlugin(), 10 * 20L);
    }

    //Player Modes:

    public void setPlayerInLobbyMode(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setInvisible(false);

        //Everything which needs to be set after spawning
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setAllowFlight(false);
                player.setFlying(false);
                player.setInvulnerable(true);
                player.setCollidable(true);
            }
        }.runTaskLater(Main.getJavaPlugin(), 2L);
    }

    public void setPlayerInGameMode(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setInvisible(false);
        player.setInvulnerable(false);
        player.setCollidable(true);
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    //Scoreboard:

    public void setInitialScoreboard(Player player) {

        Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        Objective obj = board.registerNewObjective("Infos", "dummy", ChatColor.AQUA + "" + ChatColor.BOLD + "SKYWARS");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore("").setScore(9);

        Score roleName = obj.getScore(ChatColor.WHITE + "Spieler:");
        roleName.setScore(8);
        org.bukkit.scoreboard.Team roleCounter = board.registerNewTeam("playerCounter");
        roleCounter.addEntry(ChatColor.RED + "" + ChatColor.WHITE);
        roleCounter.setPrefix(Integer.toString(playerTeamMap.size()));
        obj.getScore(ChatColor.RED + "" + ChatColor.WHITE).setScore(7);

        obj.getScore(" ").setScore(6);

        Score kills = obj.getScore(ChatColor.WHITE + "Kills:");
        kills.setScore(5);
        org.bukkit.scoreboard.Team killCounter = board.registerNewTeam("killCounter");
        killCounter.addEntry(ChatColor.GREEN + "" + ChatColor.WHITE);
        killCounter.setPrefix(ChatColor.RED + "0");
        obj.getScore(ChatColor.GREEN + "" + ChatColor.WHITE).setScore(4);

        obj.getScore("  ").setScore(3);

        Score kit = obj.getScore(ChatColor.WHITE + "Kit:");
        kit.setScore(2);
        org.bukkit.scoreboard.Team kitDisplay = board.registerNewTeam("kitDisplay");
        kitDisplay.addEntry(ChatColor.YELLOW + "" + ChatColor.WHITE);
        kitDisplay.setPrefix(playerKitMap.get(player) != null ? ChatColor.GRAY + playerKitMap.get(player).getName() : "None");
        obj.getScore(ChatColor.YELLOW + "" + ChatColor.WHITE).setScore(1);

        player.setScoreboard(board);
    }

    public void setPlayerCount() {
        playerTeamMap.forEach((player, value) -> {
            Scoreboard board = player.getScoreboard();
            Objects.requireNonNull(board.getTeam("playerCounter")).setPrefix(ChatColor.RED + "" + playerTeamMap.size());
        });
    }

    public void updateKillCounter(Player player) {
        playerTeamMap.get(player).getPlayers().forEach(pointedAtPlayer -> {
            Scoreboard board = pointedAtPlayer.getScoreboard();
            Objects.requireNonNull(board.getTeam("killCounter")).setPrefix(ChatColor.RED + "" + playerTeamMap.get(pointedAtPlayer).getKills());
        });
    }

    public void setKitDisplay(Player player){
        Scoreboard board = player.getScoreboard();
        Objects.requireNonNull(board.getTeam("kitDisplay")).setPrefix(ChatColor.GRAY + "" + playerKitMap.get(player).getName());
    }

    //Chat:

    public void sendMessageToEveryone(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
    }

    public void sendMessageToTeam(Team team, String message) {
        team.getPlayers().forEach(player -> player.sendMessage(message));
    }

    //Kits:
    public boolean kitsContains(String kitName) {
        return kits.stream().anyMatch(kit -> (ChatColor.RED + kit.getName()).equals(kitName));
    }

    public KitGame kitsGetByName(String kitName) {
        return kits.stream().filter(kit -> (ChatColor.RED + kit.getName()).equals(kitName)).findFirst().orElse(null);
    }

    public int kitsSize() {
        return kits.size();
    }

    //Getter:

    public Team[] getTeams() {
        return teams;
    }

    public Set<KitGame> getKits() {
        return kits;
    }

    public HashMap<Player, Player> getLastDamager() {
        return lastDamager;
    }

    public int getABSOLUTE_MIN_PLAYERS() {
        return ABSOLUTE_MIN_PLAYERS;
    }

    public Map<Player, Team> getPlayerTeamMap() {
        return playerTeamMap;
    }

    public MapGame getMapGame() {
        return mapGame;
    }

    public GameState getGameState() {
        return gameState;
    }

    public boolean isForcestart() {
        return forcestart;
    }

    //Setter:

    public void setForcestart(boolean forcestart) {
        this.forcestart = forcestart;
    }
}
