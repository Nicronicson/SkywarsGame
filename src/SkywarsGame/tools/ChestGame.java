package SkywarsGame.tools;

import SkywarsCore.ChestEntry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class ChestGame {

    final List<ChestEntry> chestEntryList;
    final int maxItems;

    public ChestGame(){
        chestEntryList = new ArrayList<>();
        maxItems = 20;

        String pathname = "./plugins/SkyWarsAdmin/Chest";
        for (File file: new File(pathname).listFiles()) {
            load(file.getAbsolutePath());
        }
    }

    public void load(String fileName){
        try {
            InputStream inputStream = new FileInputStream(new File(fileName));
            Yaml yaml = new Yaml();
            try {
                Map<String, Object> map = yaml.loadAs(inputStream, Map.class);

                ItemStack item = new ItemStack(Material.getMaterial(((String) ((Map<String, Object>) map.get("item")).get("type")).toUpperCase(Locale.ROOT)));
                item.setAmount((Integer) ((Map<String, Object>) map.get("item")).get("amount"));
                ((Damageable) item.getItemMeta()).setDamage((Integer) ((Map<String, Object>) map.get("item")).get("durability"));

                Map<String, Integer> itemENC = (Map<String, Integer>) map.get("itemENC");

                int chance = (Integer) map.get("chance");
                int middleChance = (Integer) map.get("middleChance");
                chestEntryList.add(new ChestEntry(item, itemENC, chance, middleChance));
            } catch(Exception e){
                Bukkit.broadcastMessage(e.getMessage());
            }
        } catch (FileNotFoundException e){
            Bukkit.broadcastMessage(e.getMessage());
        }
    }

    public ItemStack[] getRandomChestContent(boolean middle){
        int chestSize = 27;
        Set<ItemStack> chestContentUnsorted = new HashSet<>();

        for(ChestEntry chestEntry : chestEntryList){
            if(chestContentUnsorted.size() < maxItems && chestContentUnsorted.size() < chestSize){
                //1 = 0.01
                int min = 1;
                int max = 10000;

                Random random = new Random();

                int value = random.nextInt(max + min) + min;
                if (!middle && value <= chestEntry.getChance()) {
                    ItemStack itemStack = chestEntry.getItem();
                    for (Map.Entry<String, Integer> enchantment : chestEntry.getItemENC().entrySet()) {
                        itemStack.addUnsafeEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(enchantment.getKey())), enchantment.getValue());
                    }
                    chestContentUnsorted.add(itemStack);
                }

                value = random.nextInt(max + min) + min;

                if (middle && value <= chestEntry.getMiddleChance()) {
                    ItemStack itemStack = chestEntry.getItem();
                    for (Map.Entry<String, Integer> enchantment : chestEntry.getItemENC().entrySet()) {
                        itemStack.addUnsafeEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(enchantment.getKey())), enchantment.getValue());
                    }
                    chestContentUnsorted.add(itemStack);
                }
            }
        }

        List<Integer> placement = new ArrayList<>();

        while(placement.size() < chestContentUnsorted.size()) {
            int min = 0;
            int max = chestSize - 1;

            Random random = new Random();

            int value = random.nextInt(max + min) + min;

            if(!placement.contains(value))
                placement.add(value);
        }

        ItemStack[] chestContentSorted = new ItemStack[chestSize];

        int i = 0;
        for (ItemStack chestContent : chestContentUnsorted) {
            chestContentSorted[placement.get(i)] = chestContent;
            i++;
        }

        return chestContentSorted;
    }
}
