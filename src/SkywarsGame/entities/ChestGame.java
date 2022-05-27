package SkywarsGame.entities;

import SkywarsCore.ChestEntry;
import SkywarsCore.Rarity;
import SkywarsGame.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChestGame {
    //ItemCount:
    final int itemCount = 15;

    //ItemChance (have to add up to 100):
    final int commonItemChance = 60;
    final int uncommonItemChance = 30;
    final int epicItemChance = 10;
    final int legendaryItemChance = 0;

    //ItemChanceMiddle (have to add up to 100):
    final int commonItemChanceMiddle = 20;
    final int uncommonItemChanceMiddle = 30;
    final int epicItemChanceMiddle = 30;
    final int legendaryItemChanceMiddle = 20;

    final Map<Rarity , List<ChestEntry>> chestEntryLists;

    public ChestGame(){
        if(commonItemChance + uncommonItemChance + epicItemChance + legendaryItemChance != 100 || commonItemChanceMiddle + uncommonItemChanceMiddle + epicItemChanceMiddle + legendaryItemChanceMiddle != 100){
            Main.getJavaPlugin().getLogger().warning("You are stupid");
        }

        chestEntryLists = new HashMap<>();


        Arrays.stream(Rarity.values()).forEach(rarity -> {
            chestEntryLists.put(rarity, new ArrayList<>());

            String pathname = Main.PATH + "/Chest/" + rarity.getValue();

            for (File file: Objects.requireNonNull(new File(pathname).listFiles())) {
                load(file.getAbsolutePath(), rarity);
            }
        });
    }

    public void load(String fileName, Rarity rarity){
        try {
            InputStream inputStream = new FileInputStream(new File(fileName));
            Yaml yaml = new Yaml();
            try {
                Map<String, Object> map = yaml.loadAs(inputStream, Map.class);

                ItemStack item = new ItemStack(Material.getMaterial(((String) ((Map<String, Object>) map.get("item")).get("type"))));
                item.setAmount((Integer) ((Map<String, Object>) map.get("item")).get("amount"));
                ((Damageable) item.getItemMeta()).setDamage((Integer) ((Map<String, Object>) map.get("item")).get("durability"));
                ItemMeta meta = item.getItemMeta();
                meta.setLore((List<String>) ((Map<String, Object>) map.get("item")).get("lore"));
                item.setItemMeta(meta);

                if(item.getItemMeta() instanceof PotionMeta){
                    PotionMeta potionMeta = (PotionMeta) item.getItemMeta();

                    Map<String, Object> potionMetaMap = (Map<String, Object>) ((Map<String, Object>) map.get("item")).get("potionMeta");

                    potionMeta.setBasePotionData(new PotionData(PotionType.valueOf((String) potionMetaMap.get("value")), (boolean) potionMetaMap.get("extended"), (boolean) potionMetaMap.get("upgraded")));

                    item.setItemMeta(potionMeta);
                }

                Map<String, Integer> itemENC = (Map<String, Integer>) map.get("itemENC");

                chestEntryLists.get(rarity).add(new ChestEntry(item, itemENC, rarity));
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

        AtomicInteger possibleItemCount = new AtomicInteger();
        Arrays.stream(Rarity.values()).forEach(rarity -> possibleItemCount.getAndAdd(chestEntryLists.get(rarity).size()));

        if(itemCount > possibleItemCount.get()){
            Main.getJavaPlugin().getLogger().info("There are to less items to distribute");
            return new ItemStack[chestSize];
        }

        for(int i = 0; i < itemCount; i++){

            int min = 1;
            int max = 100;

            Random random = new Random();

            int raritySelector = random.nextInt(max + min) + min;

            if (!middle) {
                Rarity rarity;
                if(raritySelector <= commonItemChance){
                    rarity = Rarity.COMMON;
                } else if(raritySelector <= commonItemChance + uncommonItemChance){
                    rarity = Rarity.UNCOMMON;
                } else if(raritySelector <= commonItemChance + uncommonItemChance + epicItemChance){
                    rarity = Rarity.EPIC;
                } else {
                    rarity = Rarity.LEGENDARY;
                }

                int itemSelector = random.nextInt(chestEntryLists.get(rarity).size());

                ItemStack itemStack = chestEntryLists.get(rarity).get(itemSelector).getItem();
                for (Map.Entry<String, Integer> enchantment : chestEntryLists.get(rarity).get(itemSelector).getItemENC().entrySet()) {
                    itemStack.addUnsafeEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(enchantment.getKey())), enchantment.getValue());
                }
                chestContentUnsorted.add(itemStack);

                if(!chestContentUnsorted.contains(itemStack))
                    i--;
            } else {
                Rarity rarity;
                if(raritySelector <= commonItemChanceMiddle){
                    rarity = Rarity.COMMON;
                } else if(raritySelector <= commonItemChanceMiddle + uncommonItemChanceMiddle){
                    rarity = Rarity.UNCOMMON;
                } else if(raritySelector <= commonItemChanceMiddle + uncommonItemChanceMiddle + epicItemChanceMiddle){
                    rarity = Rarity.EPIC;
                } else {
                    rarity = Rarity.LEGENDARY;
                }

                int itemSelector = random.nextInt(chestEntryLists.get(rarity).size());

                ItemStack itemStack = chestEntryLists.get(rarity).get(itemSelector).getItem();
                for (Map.Entry<String, Integer> enchantment : chestEntryLists.get(rarity).get(itemSelector).getItemENC().entrySet()) {
                    itemStack.addUnsafeEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(enchantment.getKey())), enchantment.getValue());
                }
                chestContentUnsorted.add(itemStack);
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
