package SkywarsGame.tools;

import SkywarsGame.Util.MapUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Lobby {
    Location spawn;
    String standartMap;

    public Lobby() {
        load();
    }

    private void setSpawn(Location spawn){
        this.spawn = spawn;
    }

    public Location getSpawn() {
        return spawn;
    }

    public String getStandartMap() {
        return standartMap;
    }

    private void setStandartMap(String standartMap) {
        this.standartMap = standartMap;
    }

    public void load(){
        String pathname = "./plugins/SkyWarsAdmin/Lobby";
        String filename = "Lobby.yml";
        try {
            InputStream inputStream = new FileInputStream(new File(pathname + "/" + filename));
            Yaml yaml = new Yaml();
            java.util.Map<String, Object> map = yaml.loadAs(inputStream, java.util.Map.class);

            setSpawn(MapUtilities.getLocationFromMap((java.util.Map<String, Object>) map.get("spawn")));
            setStandartMap((String) map.get("standartMap"));
        } catch (Exception e){
            Bukkit.broadcastMessage(e.getMessage());
        }
    }
}
