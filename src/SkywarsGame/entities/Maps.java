package SkywarsGame.entities;

import SkywarsGame.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class Maps {
    public static final String MAPSELECTOR_NAME = "Mapauswahl";

    public static ItemStack getMapSelector(){
        ItemStack mapSelector = new ItemStack(Material.MAP, 1);
        ItemMeta mapSelectorItemMeta = mapSelector.getItemMeta();

        assert mapSelectorItemMeta != null;
        mapSelectorItemMeta.setDisplayName(ChatColor.YELLOW + MAPSELECTOR_NAME);
        mapSelector.setItemMeta(mapSelectorItemMeta);

        return mapSelector;
    }

    List<String> maps = new ArrayList<>();

    public Maps() {
        load();
    }

    public List<String> getMaps() {
        return maps;
    }

    public void load(){
        String pathname = Main.PATH + "/Lobby";
        String filename = "Lobby.yml";
        try {
            InputStream inputStream = new FileInputStream(new File(pathname + "/" + filename));
            Yaml yaml = new Yaml();
            java.util.Map<String, Object> map = yaml.loadAs(inputStream, java.util.Map.class);

            ((ArrayList<String>) map.get("maps")).forEach(mapname -> {
                Main.getJavaPlugin().getLogger().info("Mapname: " + mapname);
                maps.add(mapname);
                Main.getJavaPlugin().getLogger().info("First Mapname FROM Map: " + maps.get(0));
            });
        } catch (Exception e){
            Main.getJavaPlugin().getLogger().info("LOAD Exeption: " + e.getMessage());
        }
    }

    public ItemStack getItem(String mapname){
        ItemStack map = new ItemStack(Material.FILLED_MAP, 1);
        ItemMeta mapItemMeta = map.getItemMeta();

        assert mapItemMeta != null;
        mapItemMeta.setDisplayName(ChatColor.YELLOW + mapname);
        map.setItemMeta(mapItemMeta);

        return map;
    }
}
