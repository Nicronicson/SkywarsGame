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
        mapGame = new MapGame(lobby.getStandartMap());
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
                    if(teams[i].getPlayers().size() < mapGame.getTeamsize()){
                        setTeamOfPlayer(teamEntry.getKey(), i);
                        ready = true;
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

        //Set ScoreboardTeams //TODO: Warscheinlich Schrott --> Selbst Schaden erkennen und Verhindern
        for(Map.Entry<Player, Team> teamEntry : playerTeamMap.entrySet()){
            if(Bukkit.getScoreboardManager().getMainScoreboard().getTeam(Integer.toString(teamEntry.getValue().getId())) == null)
                Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(Integer.toString(teamEntry.getValue().getId()));{
                Bukkit.getScoreboardManager().getMainScoreboard().getTeam(Integer.toString(teamEntry.getValue().getId())).setAllowFriendlyFire(false);
            }
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam(Integer.toString(teamEntry.getValue().getId())).addEntry(teamEntry.getKey().getName());
        }

        //Set InitialScoreboards && Clear all Inventories
        for(Map.Entry<Player, Team> entrySet : playerTeamMap.entrySet()){
            setInitialScoreboard(entrySet.getKey());
            entrySet.getKey().getInventory().clear();
        }

        distributePlayersOnTeams();
        giveEveryPlayerAKit();
        mapGame.start(playerTeamMap);

        distributeKitItems();

        gameState = GameState.WARM_UP;
        initiateStartCountdown();
    }

    private void distributeKitItems(){
        for(Map.Entry<Player, KitGame> playerKitEntry : playerKitMap.entrySet()){
            if(playerKitEntry.getValue() != null) {
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
                ItemStack[] inventory = new ItemStack[36];
                for (ItemStack itemSlot : playerKitEntry.getValue().getInventory()) {
                    playerKitEntry.getValue().getInventoryENC().get(i).forEach((key, value) -> itemSlot.addUnsafeEnchantment(Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(key))), value));
                    inventory[i] = itemSlot;
                    i++;
                }
                playerKitEntry.getKey().getInventory().setStorageContents(inventory);
            }
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

    private void broadcastStartCountdown(int time){
        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendMessage(String.format(Language.TITLE_START_FIGHT.getFormattedText(), time));
        }
    }

    //Starts Action Phase
    private void startAction() {
        gameState = GameState.RUNNING;
    }

    public boolean enoughPlayers(){
        return Bukkit.getOnlinePlayers().size() >= MIN_PLAYERS;
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
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 1, 1F);
        });
        gameState = GameState.FINISHED;

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"stop");
            }
        }.runTaskLater(Main.getJavaPlugin(), 10L);
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
}
