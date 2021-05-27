package SkywarsGame.entities;

import SkywarsCore.Kit;
import SkywarsGame.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class KitGame extends Kit {
    public static String KITSELECTOR_NAME = "Kitauswahl";
    public static ItemStack getKitSelector(){
        ItemStack kitSelector = new ItemStack(Material.CHEST, 1);
        ItemMeta kitSelectorItemMeta = kitSelector.getItemMeta();

        assert kitSelectorItemMeta != null;
        kitSelectorItemMeta.setDisplayName(ChatColor.YELLOW + KITSELECTOR_NAME);
        kitSelector.setItemMeta(kitSelectorItemMeta);

        return kitSelector;
    }

    private String description = null;

    public KitGame(String path) {
        super();
        load(path);
    }

    @SuppressWarnings("unchecked")
    public void load(String path) {
        try {
            InputStream inputStream = new FileInputStream(new File(path));
            Yaml yaml = new Yaml();
            try {
                Map<String, Object> map = yaml.loadAs(inputStream, Map.class);

                name = (String) map.get("name");

                if (!((Map<String, Object>) map.get("leftHand")).isEmpty()) {
                    leftHand = new ItemStack(Objects.requireNonNull(Material.getMaterial(((String) ((Map<String, Object>) map.get("leftHand")).get("type")).toUpperCase(Locale.ROOT))));
                    leftHand.setAmount((Integer) ((Map<String, Object>) map.get("leftHand")).get("amount"));
                    ((Damageable) Objects.requireNonNull(leftHand.getItemMeta())).setDamage((Integer) ((Map<String, Object>) map.get("leftHand")).get("durability"));

                    leftHandENC = (Map<String, Integer>) map.get("leftHandENC");
                }

                if (!((Map<String, Object>) map.get("helmet")).isEmpty()) {
                    helmet = new ItemStack(Objects.requireNonNull(Material.getMaterial(((String) ((Map<String, Object>) map.get("helmet")).get("type")).toUpperCase(Locale.ROOT))));
                    helmet.setAmount((Integer) ((Map<String, Object>) map.get("helmet")).get("amount"));
                    ((Damageable) Objects.requireNonNull(helmet.getItemMeta())).setDamage((Integer) ((Map<String, Object>) map.get("helmet")).get("durability"));

                    helmetENC = (Map<String, Integer>) map.get("helmetENC");
                }

                if (!((Map<String, Object>) map.get("chestplate")).isEmpty()) {
                    chestplate = new ItemStack(Objects.requireNonNull(Material.getMaterial(((String) ((Map<String, Object>) map.get("chestplate")).get("type")).toUpperCase(Locale.ROOT))));
                    chestplate.setAmount((Integer) ((Map<String, Object>) map.get("chestplate")).get("amount"));
                    ((Damageable) Objects.requireNonNull(chestplate.getItemMeta())).setDamage((Integer) ((Map<String, Object>) map.get("chestplate")).get("durability"));

                    chestplateENC = (Map<String, Integer>) map.get("chestplateENC");
                }

                if (!((Map<String, Object>) map.get("leggings")).isEmpty()) {
                    leggings = new ItemStack(Objects.requireNonNull(Material.getMaterial(((String) ((Map<String, Object>) map.get("leggings")).get("type")).toUpperCase(Locale.ROOT))));
                    leggings.setAmount((Integer) ((Map<String, Object>) map.get("leggings")).get("amount"));
                    ((Damageable) Objects.requireNonNull(leggings.getItemMeta())).setDamage((Integer) ((Map<String, Object>) map.get("leggings")).get("durability"));

                    leggingsENC = (Map<String, Integer>) map.get("leggingsENC");
                }

                if (!((Map<String, Object>) map.get("boots")).isEmpty()) {
                    boots = new ItemStack(Objects.requireNonNull(Material.getMaterial(((String) ((Map<String, Object>) map.get("boots")).get("type")).toUpperCase(Locale.ROOT))));
                    boots.setAmount((Integer) ((Map<String, Object>) map.get("boots")).get("amount"));
                    ((Damageable) Objects.requireNonNull(boots.getItemMeta())).setDamage((Integer) ((Map<String, Object>) map.get("boots")).get("durability"));

                    bootsENC = (Map<String, Integer>) map.get("bootsENC");
                }

                inventory = new ItemStack[36];

                int i = 0;
                for (Map<String, Object> itemMap : (List<Map<String, Object>>) map.get("inventory")) {
                    if (!itemMap.isEmpty()) {
                        inventory[i] = new ItemStack(Objects.requireNonNull(Material.getMaterial(((String) itemMap.get("type")).toUpperCase(Locale.ROOT))));
                        inventory[i].setAmount((Integer) itemMap.get("amount"));
                        ((Damageable) Objects.requireNonNull(inventory[i].getItemMeta())).setDamage((Integer) itemMap.get("durability"));
                    }

                    i++;
                }

                inventoryENC = (ArrayList<Map<String, Integer>>) map.get("inventoryENC");

                visual = new ItemStack(Objects.requireNonNull(Material.getMaterial(((String) map.get("visual")).toUpperCase(Locale.ROOT))));

                description = (String) map.get("description");

            } catch (Exception e) {
                Bukkit.broadcastMessage(e.getMessage());
            }
        } catch (FileNotFoundException e) {
            Bukkit.broadcastMessage(e.getMessage());
        }
    }

    public String getDescription() {
        return description;
    }

    public ItemStack getItem() {
        ItemStack itemStack = visual.clone();
        ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(ChatColor.RED + name);
        itemMeta.setLore(getItemDescription());

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public List<String> getItemDescription(){
        List<String> kitDescription = new ArrayList<>();

        if (helmet != null) kitDescription.add(ChatColor.YELLOW + "" + helmet.getAmount() + "x " + ChatColor.WHITE + helmet.getType().name() + ChatColor.GRAY + " " + getEnchantmentString(helmet));
        if (chestplate != null) kitDescription.add(ChatColor.YELLOW + "" + chestplate.getAmount() + "x " + ChatColor.WHITE + chestplate.getType().name() + ChatColor.GRAY + " " + getEnchantmentString(chestplate));
        if (leggings != null) kitDescription.add(ChatColor.YELLOW + "" + leggings.getAmount() + "x " + ChatColor.WHITE + leggings.getType().name() + ChatColor.GRAY + " " + getEnchantmentString(leggings));
        if (boots != null) kitDescription.add(ChatColor.YELLOW + "" + boots.getAmount() + "x " + ChatColor.WHITE + boots.getType().name() + ChatColor.GRAY + " " + getEnchantmentString(boots));

        if (leftHand != null) kitDescription.add(ChatColor.YELLOW + "" + leftHand.getAmount() + "x " + ChatColor.WHITE + leftHand.getType().name() + ChatColor.GRAY + " " + getEnchantmentString(leftHand));

        Arrays.stream(inventory)
                .filter(Objects::nonNull)
                .forEach(item -> kitDescription.add(ChatColor.YELLOW + "" + item.getAmount() + "x " + ChatColor.WHITE + item.getType().name() + ChatColor.GRAY + " " + getEnchantmentString(item)));

        return kitDescription;
    }

    private String getEnchantmentString(ItemStack itemStack){
        StringBuilder enchantmentString = new StringBuilder();
        itemStack.getEnchantments().forEach((ench, level) -> enchantmentString.append(ench.getKey().getNamespace()).append(" lvl:").append(level).append(", "));

        if(enchantmentString.lastIndexOf(",") != -1) { //If item has enchantments
            enchantmentString.delete(enchantmentString.lastIndexOf(","), enchantmentString.lastIndexOf(",") + 1);
            enchantmentString.insert(0, '(');
            enchantmentString.append(')');
        }

        return enchantmentString.toString();
    }
}
