package SkywarsGame.game;

import SkywarsGame.Main;
import SkywarsGame.Util.Language;
import SkywarsGame.tools.KitGame;
import SkywarsGame.tools.Lobby;
import SkywarsGame.tools.MapGame;
import SkywarsGame.tools.Team;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GameManager {

    private final int LOBBY_WAIT_TIME = 30;
    private final int WARM_UP_TIME = 30;
    private final int MIN_PLAYERS = 4;
    private final int ABSOLUTE_MIN_PLAYERS = 2;

    private final String LOBBY_NAME = "Lobby";

    private MapGame mapGame;
    private Lobby lobby;
    private String mapName;

    boolean warmupCountdownActive;

    private final Map<Player, Team> playerTeamMap;
    private final Map<Player, KitGame> playerKitMap;
    private GameState gameState;
    private final Team[] teams;

    public GameManager() {
        warmupCountdownActive = false;
        playerTeamMap = new HashMap<>();
        playerKitMap = new HashMap<>();
        gameState = GameState.PREPARING;
        lobby = new Lobby();
        mapGame = new MapGame(lobby.getStandartMap());

        teams = new Team[mapGame.getSpawnpoints().size()];
        for(int i = 0; i < mapGame.getSpawnpoints().size() ;i++){
            teams[i] = new Team(i);
        }

        gameState = GameState.LOBBY;
    }

    public void loadCustomMap(String mapName, Player player){
        try {
            mapGame = new MapGame(mapName);
        } catch (Exception e){
            player.sendMessage(Language.ERR_MAP_NOT_FOUND.getFormattedText());
        }
    }

    public void joinLobby(Player player){
        //Teleport to LobbySpawn
        Location lobbySpawn = Bukkit.getWorld(LOBBY_NAME).getSpawnLocation();
        lobbySpawn.setWorld(Bukkit.getWorld(LOBBY_NAME));

        player.teleport(lobbySpawn);

        setPlayerInLobbyMode(player);

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
        //TODO: Write this Method
    }

    public void setKitOfPlayer(Player player, int teamID){
        //TODO: Write this Method
        teams[teamID].addPlayer(player);
        playerTeamMap.replace(player, teams[teamID]);
    }

    //Starts Warm Up Phase
    private void startWarmUp() {
        //TODO: Alle Spieler erhalten die Sachen von ihrem Kit
        gameState = GameState.PREPARING;

        //Set ScoreboardTeams //TODO: Warscheinlich Schrott --> Selbst Schaden erkennen und Verhindern
        for(Map.Entry<Player, Team> teamEntry : playerTeamMap.entrySet()){
            if(Bukkit.getScoreboardManager().getMainScoreboard().getTeam(Integer.toString(teamEntry.getValue().getId())) == null)
                Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(Integer.toString(teamEntry.getValue().getId()));{
                Bukkit.getScoreboardManager().getMainScoreboard().getTeam(Integer.toString(teamEntry.getValue().getId())).setAllowFriendlyFire(false);
                Bukkit.getScoreboardManager().getMainScoreboard().getTeam(Integer.toString(teamEntry.getValue().getId())).setPrefix("[" + teamEntry.getValue().getId() + "]");
            }
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam(Integer.toString(teamEntry.getValue().getId())).addEntry(teamEntry.getKey().getName());
        }

        //Clear all Inventories
        for(Player player : Bukkit.getOnlinePlayers()){
            player.getInventory().clear();
        }

        distributePlayersOnTeams();
        giveEveryPlayerAKit();
        mapGame.start(playerTeamMap);
        gameState = GameState.WARM_UP;
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
            }.runTaskLater(Main.getJavaPlugin(), 2L);
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
        }.runTaskLater(Main.getJavaPlugin(), 1L);
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
            joinLobby(player);
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
}
