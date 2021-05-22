package SkywarsGame.tools;

import SkywarsCore.Map;
import SkywarsGame.util.MapUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MapGame extends Map {

    public MapGame(String mapname) {
        super(mapname);
        load();
    }

    public void start(java.util.Map<Player, Team> playerTeamMap){
        fillChests();
        teleportAllTeams(playerTeamMap);
    }

    public void load(){
        String pathname = "./plugins/SkyWarsAdmin/Map";
        String filename = mapname + "-map" + ".yml";
        try {
            InputStream inputStream = new FileInputStream(new File(pathname + "/" + filename));
            Yaml yaml = new Yaml();
            java.util.Map<String, Object> map = yaml.loadAs(inputStream, java.util.Map.class);

            List<Location> chestLocations = new ArrayList<>();
            for(java.util.Map<String, Object> mapEntry : (List<java.util.Map<String, Object>>) map.get("chests")){
                chestLocations.add(MapUtilities.getLocationFromMap(mapEntry));
            }
            setChests(chestLocations);

            List<Location> middleChestLocations = new ArrayList<>();
            for(java.util.Map<String, Object> mapEntry : (List<java.util.Map<String, Object>>) map.get("middleChests")){
                middleChestLocations.add(MapUtilities.getLocationFromMap(mapEntry));
            }
            setMiddleChests(middleChestLocations);

            setMiddle(MapUtilities.getLocationFromMap((java.util.Map<String, Object>) map.get("middle")));
            setPos1(MapUtilities.getLocationFromMap((java.util.Map<String, Object>) map.get("pos1")));
            setPos2(MapUtilities.getLocationFromMap((java.util.Map<String, Object>) map.get("pos2")));

            List<Location> spawnpoints = new ArrayList<>();
            for(java.util.Map<String, Object> mapEntry : (List<java.util.Map<String, Object>>) map.get("spawnpoints")){
                spawnpoints.add(MapUtilities.getLocationFromMap(mapEntry));
            }
            setSpawnpoints(spawnpoints);

            setTeamsize((Integer) map.get("teamsize"));
        } catch (Exception e){
            Bukkit.broadcastMessage(e.getMessage());
        }
    }

    public void fillChests(){
        World map = Bukkit.getWorld(getMapname());
        ChestGame chests = new ChestGame();
        for(Location chestLocation : getChests()){
            try {
                ((Chest) map.getBlockAt(chestLocation).getState()).getBlockInventory().setContents(chests.getRandomChestContent(false));
            } catch (Exception e){
                Bukkit.broadcastMessage(e.getMessage());
            }
        }
        for(Location chestLocation : getMiddleChests()){
            try {
                BlockData data = map.getBlockAt(chestLocation).getState().getBlockData();
                BlockFace face = ((Directional) data).getFacing();

                map.getBlockAt(chestLocation).setType(Material.CHEST);

                BlockData blockData = map.getBlockAt(chestLocation).getBlockData();
                ((Directional) blockData).setFacing(face);
                map.getBlockAt(chestLocation).setBlockData(blockData);

                ((Chest) map.getBlockAt(chestLocation).getState()).getBlockInventory().setContents(chests.getRandomChestContent(true));
            } catch (Exception e){
                Bukkit.broadcastMessage(e.getMessage());
            }
        }
    }

    private void teleportAllTeams(java.util.Map<Player, Team> playerTeamMap){
        for(Player player : Bukkit.getOnlinePlayers()){
            try {
                teleportPlayer(player, playerTeamMap.get(player).getId());
            } catch (Exception e){}
        }
    }

    private void teleportPlayer(Player player, int spawnpoint){
        Location location = getSpawnpoints().get(spawnpoint);
        location.setWorld(Bukkit.getWorld(getMapname()));
        player.teleport(location);
    }
}
