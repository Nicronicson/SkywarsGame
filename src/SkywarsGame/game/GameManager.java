package SkywarsGame.game;

import SkywarsGame.Main;
import SkywarsGame.util.Countdown;
import SkywarsGame.util.Language;
import SkywarsGame.entities.KitGame;
import SkywarsGame.entities.Maps;
import SkywarsGame.entities.MapGame;
import SkywarsGame.entities.Team;
import SkywarsGame.util.Sounds;
import net.problemzone.lobbibi.modules.events.GameFinishEvent;
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
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class GameManager {
    private MapGame mapGame;

    boolean forcemap;

    boolean antiFallTime;

    private BukkitTask currentScheduledTask;

    private final Map<Player, Team> playerTeamMap;
    private final Map<Player, KitGame> playerKitMap;
    private final Map<Player, Player> lastDamager;

    private GameState gameState;

    private final Team[] teams;
    private final Set<KitGame> kits;
    Maps maps;

    public GameManager() {
        antiFallTime = false;
        forcemap = false;
        playerTeamMap = new HashMap<>();
        playerKitMap = new HashMap<>();

        maps = new Maps();
        //Select Random Map
        List<String> mapList = maps.getMaps();
        Collections.shuffle(mapList);
        Main.getJavaPlugin().getLogger().info("Loading " + mapList.get(0));
        loadMap(mapList.get(0));
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
        Main.getJavaPlugin().getLogger().info("Load Kits:");
        String pathname = Main.PATH + "/Kit";
        for (File file : Objects.requireNonNull(new File(pathname).listFiles())) {
            Main.getJavaPlugin().getLogger().info("Kit: " + file.getAbsolutePath());
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
            world.setGameRule(GameRule.DO_TILE_DROPS, true);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);

            mapGame = new MapGame(mapName);

            return true;
        }
        Main.getJavaPlugin().getLogger().warning("Map wasn't found");
        return false;
    }

    public void loadCustomMap(String mapName, Player player) {
        if (gameState == GameState.LOBBY) {
            Main.getJavaPlugin().getLogger().info("Loading " + mapName);
            if (loadMap(mapName)) {
                forcemap = true;
                player.sendMessage(Language.MAP_CHANGED.getFormattedText());
            } else {
                player.sendMessage(Language.ERR_MAP_NOT_FOUND.getFormattedText());
            }
        }
    }

    //join game

    public void joinAsPlayer(Player player) {
        //Stuff which is needed before a game starts
        playerTeamMap.put(player, null);
        playerKitMap.put(player, null);

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

        //Lobby stuff
        player.getInventory().setItem(0, KitGame.getKitSelector());

        player.getInventory().setItem(1, Team.getTeamSelector());

        player.getInventory().setItem(4, Maps.getMapSelector());
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

            //Set List-Name
            player.setPlayerListName(String.format(Language.PLAYER_TEAM_NAME.getText(), teams[teamID].getColor(), teamID, player.getName()));

            teams[teamID].addPlayer(player);
            playerTeamMap.replace(player, teams[teamID]);
            return true;
        }
        return false;
    }

    //Kits:

    public void giveEveryPlayerAKit() {
        playerKitMap.forEach((player, kit) -> {
            String standardKit = ChatColor.RED + "Standard";

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

    //Starts Game (Warm Up Phase)
    public void startGame() {
        //TODO: load choosen Map
        if(!forcemap){

        }

        distributePlayersOnTeams();

        giveEveryPlayerAKit();

        //For every Player
        playerTeamMap.forEach((player, team) -> {
            player.getInventory().clear();
            setPlayerInGameMode(player);
        });

        mapGame.startAndTeleport(playerTeamMap);

        distributeKitItems();

        gameState = GameState.WARM_UP;

        //initiate starting the game
        startAntiFallTime();
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

    //Starts AntiFallTime
    private void startAntiFallTime(){
        antiFallTime = true;
        initiateAntiFallCountdown();
    }

    //Starts Action Phase
    private void startAction() {
        gameState = GameState.RUNNING;
    }

    public void initiateAntiFallCountdown(){ //is apparent in the WarnUp Time
        int ANTI_FALL_TIME = 4;
        int seconds = ANTI_FALL_TIME;

        if (currentScheduledTask != null && !currentScheduledTask.isCancelled()) currentScheduledTask.cancel();

        currentScheduledTask = new BukkitRunnable() {
            @Override
            public void run() {
                antiFallTime = false;
                initiateStartCountdown();
            }
        }.runTaskLater(Main.getJavaPlugin(), seconds * 20L);

        Countdown.createTitleCountdown(seconds, Language.ANTI_FALL);
    }

    private void initiateStartCountdown() {
        //private final int LOBBY_WAIT_TIME = 30;
        int WARM_UP_TIME = 50;
        int seconds = WARM_UP_TIME;

        if (currentScheduledTask != null && !currentScheduledTask.isCancelled()) currentScheduledTask.cancel();

        currentScheduledTask = new BukkitRunnable() {
            @Override
            public void run() {
                startAction();
            }
        }.runTaskLater(Main.getJavaPlugin(), seconds * 20L);

        Countdown.createChatCountdown(seconds, Language.WARM_UP, Language.WARM_UP_FINAL);
    }

    //Winning Related Methods:

    public void removePlayer(Player player, boolean spectator) {
        if(!spectator && playerTeamMap.get(player) != null) playerTeamMap.get(player).removePlayer(player);

        playerTeamMap.remove(player);
        playerKitMap.remove(player);

        setPlayerCount();

        if (gameState == GameState.WARM_UP || gameState == GameState.RUNNING) {
            checkForWin();
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
        Countdown.cancelChatCountdown();
        Countdown.cancelXpBarCountdown();
        Countdown.cancelLevelCountdown();
        Countdown.cancelTitleCountdown();

        if(currentScheduledTask != null && !currentScheduledTask.isCancelled()){
            currentScheduledTask.cancel();
        }

        //Show All Players:
        Bukkit.getOnlinePlayers().forEach(playerToShow -> Bukkit.getOnlinePlayers().forEach(player -> player.showPlayer(Main.getJavaPlugin(), playerToShow)));

        //Sets everyone in LobbyMode and set Velocity to 0
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.setVelocity(new Vector());
            player.getInventory().clear();
            player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
            setPlayerInLobbyMode(player);
        });

        // Call Lobbibi
        Bukkit.getPluginManager().callEvent(new GameFinishEvent());

        new BukkitRunnable() {
            @Override
            public void run() {
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
                    } else {
                        //Lose sound
                        Sounds.LOSE.playSoundForPlayer(player);
                    }

                });
                gameState = GameState.LOBBY;
            }
        }.runTaskLater(Main.getJavaPlugin(), 2L);
    }

    //Player Modes:

    public void setPlayerInLobbyMode(Player player) {
        player.setInvisible(false);

        //Everything which needs to be set after spawning
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setAllowFlight(false);
                player.setFlying(false);
                player.setInvulnerable(true);
                player.setCollidable(true);
                player.setHealth(20);
                player.setFoodLevel(20);
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

    public Maps getMaps() {
        return maps;
    }

    public Team[] getTeams() {
        return teams;
    }

    public Set<KitGame> getKits() {
        return kits;
    }

    public Map<Player, Player> getLastDamager() {
        return lastDamager;
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

    public boolean isAntiFallTime() {
        return antiFallTime;
    }
}