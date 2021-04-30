package SkywarsGame.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class MapUtilities {
    public static Location getLocationFromMap(java.util.Map<String, Object> map){
        World world = null;
        if(map.get("world") != null){
            world = Bukkit.getWorld((String) map.get("world"));
        }
        return new Location(world, (Double) map.get("x"), (Double) map.get("y"), (Double) map.get("z"), ((Number) map.get("yaw")).floatValue(), ((Number) map.get("pitch")).floatValue());
    }
}
