package SkywarsGame.tools;

import SkywarsCore.Kit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class KitGame extends Kit {
    public KitGame(String path) {
        super();
        load(path);
    }

    public void load(String path){
        try {
            InputStream inputStream = new FileInputStream(new File(path));
            Yaml yaml = new Yaml();
            try {
                Map<String, Object> map = yaml.loadAs(inputStream, Map.class);
                Bukkit.broadcastMessage(map.toString());

                if(!((Map<String, Object>) map.get("leftHand")).isEmpty()) {
                    leftHand = new ItemStack(Material.getMaterial(((String) ((Map<String, Object>) map.get("leftHand")).get("type")).toUpperCase(Locale.ROOT)));
                    leftHand.setAmount((Integer) ((Map<String, Object>) map.get("leftHand")).get("amount"));
                    ((Damageable) leftHand.getItemMeta()).setDamage((Integer) ((Map<String, Object>) map.get("leftHand")).get("durability"));

                    leftHandENC = (Map<String, Integer>) map.get("leftHandENC");
                }

                if(!((Map<String, Object>) map.get("helmet")).isEmpty()) {
                    helmet = new ItemStack(Material.getMaterial(((String) ((Map<String, Object>) map.get("helmet")).get("type")).toUpperCase(Locale.ROOT)));
                    helmet.setAmount((Integer) ((Map<String, Object>) map.get("helmet")).get("amount"));
                    ((Damageable) helmet.getItemMeta()).setDamage((Integer) ((Map<String, Object>) map.get("helmet")).get("durability"));

                    helmetENC = (Map<String, Integer>) map.get("helmetENC");
                }

                if(!((Map<String, Object>) map.get("chestplate")).isEmpty()) {
                    chestplate = new ItemStack(Material.getMaterial(((String) ((Map<String, Object>) map.get("chestplate")).get("type")).toUpperCase(Locale.ROOT)));
                    chestplate.setAmount((Integer) ((Map<String, Object>) map.get("chestplate")).get("amount"));
                    ((Damageable) chestplate.getItemMeta()).setDamage((Integer) ((Map<String, Object>) map.get("chestplate")).get("durability"));

                    chestplateENC = (Map<String, Integer>) map.get("chestplateENC");
                }

                if(!((Map<String, Object>) map.get("leggings")).isEmpty()) {
                    leggings = new ItemStack(Material.getMaterial(((String) ((Map<String, Object>) map.get("leggings")).get("type")).toUpperCase(Locale.ROOT)));
                    leggings.setAmount((Integer) ((Map<String, Object>) map.get("leggings")).get("amount"));
                    ((Damageable) leggings.getItemMeta()).setDamage((Integer) ((Map<String, Object>) map.get("leggings")).get("durability"));

                    leggingsENC = (Map<String, Integer>) map.get("leggingsENC");
                }

                if(!((Map<String, Object>) map.get("boots")).isEmpty()) {
                    boots = new ItemStack(Material.getMaterial(((String) ((Map<String, Object>) map.get("boots")).get("type")).toUpperCase(Locale.ROOT)));
                    boots.setAmount((Integer) ((Map<String, Object>) map.get("boots")).get("amount"));
                    ((Damageable) boots.getItemMeta()).setDamage((Integer) ((Map<String, Object>) map.get("boots")).get("durability"));

                    bootsENC = (Map<String, Integer>) map.get("bootsENC");
                }

                inventory = new ItemStack[36];

                int i = 0;
                for(Map<String, Object> itemMap : (List<Map<String, Object>>) map.get("inventory")){
                    if(!itemMap.isEmpty()) {
                        inventory[i] = new ItemStack(Material.getMaterial(((String) itemMap.get("type")).toUpperCase(Locale.ROOT)));
                        inventory[i].setAmount((Integer) itemMap.get("amount"));
                        ((Damageable) inventory[i].getItemMeta()).setDamage((Integer) itemMap.get("durability"));
                    }

                    i++;
                }

                inventoryENC = (ArrayList<Map<String, Integer>>) map.get("inventoryENC");

                visual = new ItemStack(Material.getMaterial(((String) map.get("visual")).toUpperCase(Locale.ROOT)));

            } catch(Exception e){
                Bukkit.broadcastMessage(e.getMessage());
            }
        } catch (FileNotFoundException e){
            Bukkit.broadcastMessage(e.getMessage());
        }
    }
}
