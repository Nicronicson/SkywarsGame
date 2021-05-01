package SkywarsGame.game;

import SkywarsGame.Main;
import SkywarsGame.Util.Countdown;
import SkywarsGame.Util.Language;
import SkywarsGame.tools.Lobby;
import SkywarsGame.tools.MapGame;
import SkywarsGame.tools.Team;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GameManager {

    private final int LOBBY_WAIT_TIME = 30;
    private final int WARM_UP_TIME = 30;
    private final int MIN_PLAYERS_TO_START = 4;
    private final int MIN_PLAYERS = 2;

    private MapGame mapGame;
    private Lobby lobby;
    private String mapName;

    boolean warmupCountdown;

    private final Map<Player, Team> playerTeamMap;
    private GameState gameState;

    public GameManager() {
        warmupCountdown = false;
        playerTeamMap = new HashMap<>();
        gameState = GameState.PREPARING;
        lobby = new Lobby();
        mapGame = new MapGame(lobby.getStandartMap());
        gameState = GameState.LOBBY;
    }

    public void loadCustomMap(String mapName, Player player){
        try {
            mapGame = new MapGame(mapName);
        } catch (Exception e){
            player.sendMessage(Language.ERR_MAP_NOT_FOUND.getFormattedText());
        }
    }

    //Starts Warm Up Phase
    private void startWarmUp() {
        //TODO: Alle Spieler werden auf die Inseln tp't und können keinen Angreifen
        mapGame.start();
        gameState = GameState.WARM_UP;
    }

    //Starts Action Phase
    private void startAction() {
        //TODO: Alle spieler können sich nun angreifen
        gameState = GameState.RUNNING;
    }

    //Starts Lobby Countdown
    private void initiateWarmupCountdown() {
        warmupCountdown(1);
    }

    //Starts Lobby Countdown
    private void initiateWarmupCountdown(int leftOverSeconds) {
        warmupCountdown(LOBBY_WAIT_TIME - leftOverSeconds);
    }

    private void warmupCountdown(int iteration){
        if(!warmupCountdown)
            return;
        if(iteration == LOBBY_WAIT_TIME){
            gameState = GameState.PREPARING;
            startWarmUp();
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
        if(!anderesTeam)
            announceWin(team);
    }

    private void announceWin(Team team) {
        gameState = GameState.FINISHED;

        //TODO: Teleport Players to Lobby
        //TODO: Rewrite Method

        Bukkit.broadcastMessage(String.format(Language.ROLE_WIN.getFormattedText(), role.getRoleName().getText()));
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendTitle(String.format(Language.ROLE_WIN.getText(), role.getRoleName().getText()), "", 10, 60, 10);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 1, 1F);
        });
    }


    //Public getter methods
    public GameState getGameState() {
        return gameState;
    }
}
