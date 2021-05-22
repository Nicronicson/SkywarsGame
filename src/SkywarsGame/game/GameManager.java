package SkywarsGame.game;

import SkywarsGame.Main;
import SkywarsGame.util.Language;
import SkywarsGame.tools.KitGame;
import SkywarsGame.tools.Lobby;
import SkywarsGame.tools.MapGame;
import SkywarsGame.tools.Team;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.io.File;
import java.util.*;

public class GameManager {

    private final int LOBBY_WAIT_TIME = 30;
    private final int WARM_UP_TIME = 30;
    private final int MIN_PLAYERS = 4;
    private final int ABSOLUTE_MIN_PLAYERS = 2;

    private final String LOBBY_NAME = "Lobby";

    private MapGame mapGame;
    private final Lobby lobby;

    boolean warmupCountdownActive;

    private final Map<Player, Team> playerTeamMap;
    private final Map<Player, KitGame> playerKitMap;
    private final HashMap<Player, Player> lastDamager;
    private GameState gameState;
    private final Team[] teams;
    private List<KitGame> kits;

    public GameManager() {
        warmupCountdownActive = false;
        playerTeamMap = new HashMap<>();
        playerKitMap = new HashMap<>();
        gameState = GameState.PREPARING;
        lobby = new Lobby();
        loadMap(lobby.getStandartMap());
        lastDamager = new HashMap<>();

        kits = new ArrayList<>();
        loadKits();

        teams = new Team[mapGame.getSpawnpoints().size()];
        for(int i = 0; i < mapGame.getSpawnpoints().size() ;i++){
            teams[i] = new Team(i);
        }

        gameState = GameState.LOBBY;
    }

    private void loadKits(){
        String pathname = "./plugins/SkyWarsAdmin/Kit";
        for(File file : new File(pathname).listFiles()) {
            kits.add(new KitGame(file.getAbsolutePath()));
        }
    }

    public void loadMap(String mapName){
        Main.getJavaPlugin().getServer().createWorld(new WorldCreator(mapName));

        //Set GAMERULES
        World world = Bukkit.getWorld(mapName);
        world.setDifficulty(Difficulty.NORMAL);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);

        mapGame = new MapGame(mapName);
    }

    public void loadCustomMap(String mapName, Player player){
        //TODO: implement Command
        try {
            mapGame = new MapGame(mapName);
        } catch (Exception e){
            player.sendMessage(Language.ERR_MAP_NOT_FOUND.getFormattedText());
        }
    }

    public void joinLobby(PlayerSpawnLocationEvent e){
        Player player = e.getPlayer();

        //Teleport to LobbySpawn
        Location lobbySpawn = Bukkit.getWorld(LOBBY_NAME).getSpawnLocation();
        lobbySpawn.setWorld(Bukkit.getWorld(LOBBY_NAME));

        e.setSpawnLocation(lobbySpawn);

        setPlayerInLobbyMode(player);
    }

    public void teleportToLobby(Player player){
        //Teleport to LobbySpawn
        Location lobbySpawn = Bukkit.getWorld(LOBBY_NAME).getSpawnLocation();
        lobbySpawn.setWorld(Bukkit.getWorld(LOBBY_NAME));

        player.teleport(lobbySpawn);

        setPlayerInLobbyMode(player);
    }

    public void joinGame(Player player){
        //Stuff which is needed before a game starts
        if(gameState == GameState.LOBBY){
            playerTeamMap.put(player, null);
            playerKitMap.put(player, null);
            //TODO: give Players the option to choose Kits and their Teams
        }
    }

    private void distributePlayersOnTeams(){
        for(Map.Entry<Player, Team> teamEntry : playerTeamMap.entrySet()){
            if(teamEntry.getValue() == null){
                boolean ready = false;
                int i = 0;
                while(!ready){
                    try {
                        if (teams[i].getPlayers().size() < mapGame.getTeamsize()) {
                            setTeamOfPlayer(teamEntry.getKey(), i);
                            ready = true;
                        }
                    } catch (Exception e){
                        Bukkit.broadcastMessage(Language.GENERAL_ERROR.getFormattedText());
                        return;
                    }
                    i++;
                }
            }
        }
    }

    public void setTeamOfPlayer(Player player, int teamID){
        teams[teamID].addPlayer(player);
        playerTeamMap.replace(player, teams[teamID]);
    }

    public void giveEveryPlayerAKit(){
        for(Map.Entry<Player, Team> playerEntry : playerTeamMap.entrySet()){
            setKitOfPlayer(playerEntry.getKey(), !kits.isEmpty() ? kits.get(0) : null);
        }
    }

    public void setKitOfPlayer(Player player, KitGame kit){
        playerKitMap.replace(player, kit);
    }

    //Starts Warm Up Phase
    public void startWarmUp() {
        gameState = GameState.PREPARING;

        distributePlayersOnTeams();

        //For every Player
        for(Map.Entry<Player, Team> entrySet : playerTeamMap.entrySet()){
            //Set InitialScoreboards
            setInitialScoreboard(entrySet.getKey());

            //Clear all Inventories
            entrySet.getKey().getInventory().clear();

            //Set everyone's color
            entrySet.getKey().setPlayerListName("[" + entrySet.getValue().getColor() + "T" + entrySet.getValue().getId() + ChatColor.WHITE + "] " + ChatColor.GREEN + entrySet.getKey().getName());
        }

        giveEveryPlayerAKit();

        mapGame.start(playerTeamMap);

        distributeKitItems();

        gameState = GameState.WARM_UP;

        initiateStartCountdown();
    }

    private void distributeKitItems(){
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

                    Bukkit.broadcastMessage("Brummsen");

                    int i = 0; //TODO: fixen () Kein Kit
                    ItemStack[] inventory = new ItemStack[36];
                    for (ItemStack itemSlot : playerKitEntry.getValue().getInventory()) {
                        if(itemSlot != null) {
                            playerKitEntry.getValue().getInventoryENC().get(i).forEach((key, value) -> itemSlot.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(key))), value));
                        }
                        inventory[i] = itemSlot;
                        i++;
                    }
                    Bukkit.broadcastMessage(inventory.toString());
                    playerKitEntry.getKey().getInventory().setStorageContents(inventory);

                    Bukkit.broadcastMessage("Brammsen");
                }
            }
        } catch (Exception e){
            Bukkit.broadcastMessage(e.getMessage());
        }
    }

    private void initiateStartCountdown(){
        startCountdown(1);
    }

    private void startCountdown(int iteration){
        if(iteration == WARM_UP_TIME){
            startAction();
            return;
        }

        int leftOverSeconds = LOBBY_WAIT_TIME - iteration;
        if(leftOverSeconds == 10 || leftOverSeconds == 5 || leftOverSeconds == 4 || leftOverSeconds == 3 || leftOverSeconds == 2 || leftOverSeconds == 1){
            Bukkit.broadcastMessage(String.format(Language.TITLE_START_FIGHT.getFormattedText(), leftOverSeconds));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                startCountdown(iteration + 1);
            }
        }.runTaskLater(Main.getJavaPlugin(), 20L); //1 Second
    }

    /*
    private void broadcastStartCountdown(int time){
        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendMessage(String.format(Language.TITLE_START_FIGHT.getFormattedText(), time));
        }
    }
     */

    //Starts Action Phase
    private void startAction() {
        gameState = GameState.RUNNING;
    }

    public boolean enoughPlayers(){
        return Bukkit.getOnlinePlayers().size() >= MIN_PLAYERS;
    }

    private int neededPlayers(){
        return MIN_PLAYERS - Bukkit.getOnlinePlayers().size();
    }

    public void broadcastNeededPlayers(){
        new BukkitRunnable() {
            @Override
            public void run() {
                int neededPlayers = neededPlayers();
                if(neededPlayers == 1) {
                    Bukkit.broadcastMessage(Language.PLAYERS_NEEDED_ONE.getFormattedText());
                } else if(neededPlayers > 1) {
                    Bukkit.broadcastMessage(String.format(Language.PLAYERS_NEEDED.getFormattedText(), neededPlayers));
                }
            }
        }.runTaskLater(Main.getJavaPlugin(), 5);

    }

    public void stopCountdown(){
        warmupCountdownActive = false;
    }

    //Starts Lobby Countdown
    public void initiateWarmupCountdown() {
        if(!warmupCountdownActive) {
            warmupCountdownActive = true;
            warmupCountdown(1);
        }
    }

    //TODO: Write Command
    //Starts Lobby Countdown
    public void initiateWarmupCountdown(int leftOverSeconds) {
        if(!warmupCountdownActive) {
            warmupCountdownActive = true;
            warmupCountdown(LOBBY_WAIT_TIME - leftOverSeconds);
        } else {
            warmupCountdownActive = false;
            new BukkitRunnable() {
                @Override
                public void run() {
                    initiateWarmupCountdown(leftOverSeconds);
                }
            }.runTaskLater(Main.getJavaPlugin(), 30L);//1 Second
        }
    }

    private void warmupCountdown(int iteration){
        if(!warmupCountdownActive)
            return;
        if(iteration == LOBBY_WAIT_TIME){
            warmupCountdownActive = false;
            startWarmUp();
            return;
        }

        int leftOverSeconds = LOBBY_WAIT_TIME - iteration;
        if(leftOverSeconds == 10 || leftOverSeconds == 5 || leftOverSeconds == 4 || leftOverSeconds == 3 || leftOverSeconds == 2 || leftOverSeconds == 1){
            Bukkit.broadcastMessage(String.format(Language.TITLE_START.getFormattedText(), leftOverSeconds));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                warmupCountdown(iteration + 1);
            }
        }.runTaskLater(Main.getJavaPlugin(), 20L); //1 Second
    }


    //Winning Related Methods
    public void removePlayer(Player player) {
        playerTeamMap.remove(player);
        playerKitMap.remove(player);
        if(gameState == GameState.WARM_UP || gameState == GameState.RUNNING)
            checkForWin();
    }

    private void checkForWin() {
        Team team = null;
        boolean anderesTeam = false;
        for(Map.Entry<Player, Team> mapEntry : playerTeamMap.entrySet()){
            if (team == null) {
                team = mapEntry.getValue();
            } else if(mapEntry.getValue() != team){
                anderesTeam = true;
            }
        }
        if(!anderesTeam) {
            announceWin(team);
        }
    }

    private void announceWin(Team team) {
        gameState = GameState.PREPARING;

        //Teleports everybody back in the Lobby and Sets everyone in LobbyMode
        for(Player player : Bukkit.getOnlinePlayers()){
            teleportToLobby(player);
        }

        Bukkit.broadcastMessage(String.format(Language.ANNOUNCE_WIN_TEAM.getFormattedText(), team.getId()));

        StringBuilder playerNames = new StringBuilder("");
        for(Player player : team.getPlayers()){
            playerNames.append(player.getName());
            playerNames.append(", ");
        }
        playerNames.delete(playerNames.lastIndexOf(","), playerNames.lastIndexOf(",") + 1);
        Bukkit.broadcastMessage(String.format(Language.ANNOUNCE_WIN_PLAYERS.getFormattedText(), playerNames));

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendTitle(String.format(Language.ANNOUNCE_WIN_TEAM.getTitleText(), team.getId()), "", 10, 60, 10);

            if(team.getPlayers().contains(player)) {
                //Win sound
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.AMBIENT, 1, 1.1F);
            } else {
                //Lose sound
                player.playSound(player.getLocation(), Sound.ENCHANT_THORNS_HIT, SoundCategory.AMBIENT, 1, 0.5F);
            }

        });
        gameState = GameState.FINISHED;

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"stop");
            }
        }.runTaskLater(Main.getJavaPlugin(), 10 * 20L);
    }

    public void setPlayerInLobbyMode(Player player){
        player.setGameMode(GameMode.SURVIVAL);
        player.setPlayerListName(player.getName());
        player.setInvisible(false);
        player.setInvulnerable(true);
        player.setCollidable(true);
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    public MapGame getMapGame() {
        return mapGame;
    }

    //Public getter methods
    public GameState getGameState() {
        return gameState;
    }

    //Scoreboard
    public void setInitialScoreboard(Player player) {

        Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        Objective obj = board.registerNewObjective("Infos", "dummy", ChatColor.AQUA + "Skywars");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore("").setScore(6);

        Score roleName = obj.getScore(ChatColor.WHITE + "Spieler:");
        roleName.setScore(5);
        org.bukkit.scoreboard.Team roleCounter = board.registerNewTeam("playerCounter");
        roleCounter.addEntry(ChatColor.RED + "" + ChatColor.WHITE);
        roleCounter.setPrefix(Integer.toString(playerTeamMap.size()));
        obj.getScore(ChatColor.RED + "" + ChatColor.WHITE).setScore(4);

        obj.getScore(" ").setScore(3);

        Score kills = obj.getScore(ChatColor.WHITE + "Kills:");
        kills.setScore(2);
        org.bukkit.scoreboard.Team killCounter = board.registerNewTeam("killCounter");
        killCounter.addEntry(ChatColor.GREEN + "" + ChatColor.WHITE);
        killCounter.setPrefix(ChatColor.RED + "0");
        obj.getScore(ChatColor.GREEN + "" + ChatColor.WHITE).setScore(1);

        player.setScoreboard(board);
    }

    public void setPlayerCount() {
        for(Map.Entry<Player, Team> entrySet : playerTeamMap.entrySet()){
            Player player = entrySet.getKey();
            Scoreboard board = player.getScoreboard();
            Objects.requireNonNull(board.getTeam("playerCounter")).setPrefix(ChatColor.RED + "" + playerTeamMap.size());
        }
    }

    public void increaseKillCounter(Player player) {
        for(Player pointedAtPlayer : playerTeamMap.get(player).getPlayers()){
            Scoreboard board = pointedAtPlayer.getScoreboard();
            Objects.requireNonNull(board.getTeam("killCounter")).setPrefix(ChatColor.RED + "" + playerTeamMap.get(pointedAtPlayer).getKills());
        }
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

    public Map<Player, KitGame> getPlayerKitMap() {
        return playerKitMap;
    }
}
