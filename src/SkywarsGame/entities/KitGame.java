package SkywarsGame.entities;

import SkywarsCore.Kit;
import SkywarsGame.Main;
import SkywarsGame.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

                Main.getJavaPlugin().getLogger().info("KITLOAD-SPECIAL-1");

                name = (String) map.get("name");

                Main.getJavaPlugin().getLogger().info("KITLOAD-SPECIAL-2");

                if (!((Map<String, Object>) map.get("leftHand")).isEmpty()) {
                    leftHand = new ItemStack(Objects.requireNonNull(Material.getMaterial((String) ((Map<String, Object>) map.get("leftHand")).get("type"))));
                    leftHand.setAmount((Integer) ((Map<String, Object>) map.get("leftHand")).get("amount"));
                    ((Damageable) Objects.requireNonNull(leftHand.getItemMeta())).setDamage((Integer) ((Map<String, Object>) map.get("leftHand")).get("durability"));
                    ItemMeta meta = leftHand.getItemMeta();
                    meta.setLore((List<String>) ((Map<String, Object>) map.get("leftHand")).get("lore"));
                    leftHand.setItemMeta(meta);

                    if(leftHand.getItemMeta() instanceof PotionMeta){
                        PotionMeta potionMeta = (PotionMeta) leftHand.getItemMeta();

                        Map<String, Object> potionMetaMap = (Map<String, Object>) ((Map<String, Object>) map.get("leftHand")).get("potionMeta");

                        potionMeta.setBasePotionData(new PotionData(PotionType.valueOf((String) potionMetaMap.get("value")), (boolean) potionMetaMap.get("extended"), (boolean) potionMetaMap.get("upgraded")));

                        leftHand.setItemMeta(potionMeta);
                    }

                    leftHandENC = (Map<String, Integer>) map.get("leftHandENC");
                }

                Main.getJavaPlugin().getLogger().info("KITLOAD-SPECIAL-3");

                if (!((Map<String, Object>) map.get("helmet")).isEmpty()) {
                    helmet = new ItemStack(Objects.requireNonNull(Material.getMaterial((String) ((Map<String, Object>) map.get("helmet")).get("type"))));
                    helmet.setAmount((Integer) ((Map<String, Object>) map.get("helmet")).get("amount"));
                    ((Damageable) Objects.requireNonNull(helmet.getItemMeta())).setDamage((Integer) ((Map<String, Object>) map.get("helmet")).get("durability"));
                    ItemMeta meta = helmet.getItemMeta();
                    meta.setLore((List<String>) ((Map<String, Object>) map.get("helmet")).get("lore"));
                    helmet.setItemMeta(meta);

                    helmetENC = (Map<String, Integer>) map.get("helmetENC");
                }

                Main.getJavaPlugin().getLogger().info("KITLOAD-SPECIAL-4");

                if (!((Map<String, Object>) map.get("chestplate")).isEmpty()) {
                    chestplate = new ItemStack(Objects.requireNonNull(Material.getMaterial((String) ((Map<String, Object>) map.get("chestplate")).get("type"))));
                    chestplate.setAmount((Integer) ((Map<String, Object>) map.get("chestplate")).get("amount"));
                    ((Damageable) Objects.requireNonNull(chestplate.getItemMeta())).setDamage((Integer) ((Map<String, Object>) map.get("chestplate")).get("durability"));
                    ItemMeta meta = chestplate.getItemMeta();
                    meta.setLore((List<String>) ((Map<String, Object>) map.get("chestplate")).get("lore"));
                    chestplate.setItemMeta(meta);

                    chestplateENC = (Map<String, Integer>) map.get("chestplateENC");
                }

                Main.getJavaPlugin().getLogger().info("KITLOAD-SPECIAL-5");

                if (!((Map<String, Object>) map.get("leggings")).isEmpty()) {
                    leggings = new ItemStack(Objects.requireNonNull(Material.getMaterial((String) ((Map<String, Object>) map.get("leggings")).get("type"))));
                    leggings.setAmount((Integer) ((Map<String, Object>) map.get("leggings")).get("amount"));
                    ((Damageable) Objects.requireNonNull(leggings.getItemMeta())).setDamage((Integer) ((Map<String, Object>) map.get("leggings")).get("durability"));
                    ItemMeta meta = leggings.getItemMeta();
                    meta.setLore((List<String>) ((Map<String, Object>) map.get("leggings")).get("lore"));
                    leggings.setItemMeta(meta);

                    leggingsENC = (Map<String, Integer>) map.get("leggingsENC");
                }

                Main.getJavaPlugin().getLogger().info("KITLOAD-SPECIAL-6");

                if (!((Map<String, Object>) map.get("boots")).isEmpty()) {
                    boots = new ItemStack(Objects.requireNonNull(Material.getMaterial((String) ((Map<String, Object>) map.get("boots")).get("type"))));
                    boots.setAmount((Integer) ((Map<String, Object>) map.get("boots")).get("amount"));
                    ((Damageable) Objects.requireNonNull(boots.getItemMeta())).setDamage((Integer) ((Map<String, Object>) map.get("boots")).get("durability"));
                    ItemMeta meta = boots.getItemMeta();
                    meta.setLore((List<String>) ((Map<String, Object>) map.get("boots")).get("lore"));
                    boots.setItemMeta(meta);

                    bootsENC = (Map<String, Integer>) map.get("bootsENC");
                }

                Main.getJavaPlugin().getLogger().info("KITLOAD-SPECIAL-7");

                inventory = new ItemStack[36];

                int i = 0;
                for (Map<String, Object> itemMap : (List<Map<String, Object>>) map.get("inventory")) {
                    if (!itemMap.isEmpty()) {
                        inventory[i] = new ItemStack(Objects.requireNonNull(Material.getMaterial((String) itemMap.get("type"))));
                        inventory[i].setAmount((Integer) itemMap.get("amount"));
                        ((Damageable) Objects.requireNonNull(inventory[i].getItemMeta())).setDamage((Integer) itemMap.get("durability"));
                        ItemMeta meta = Objects.requireNonNull(inventory[i].getItemMeta());
                        meta.setLore((List<String>) itemMap.get("lore"));
                        inventory[i].setItemMeta(meta);

                        if(inventory[i].getItemMeta() instanceof PotionMeta){
                            PotionMeta potionMeta = (PotionMeta) Objects.requireNonNull(inventory[i].getItemMeta());

                            Map<String, Object> potionMetaMap = (Map<String, Object>) itemMap.get("potionMeta");

                            potionMeta.setBasePotionData(new PotionData(PotionType.valueOf((String) potionMetaMap.get("value")), (boolean) potionMetaMap.get("extended"), (boolean) potionMetaMap.get("upgraded")));

                            inventory[i].setItemMeta(potionMeta);
                        }
                    }

                    i++;
                }

                Main.getJavaPlugin().getLogger().info("KITLOAD-SPECIAL-8");

                inventoryENC = (ArrayList<Map<String, Integer>>) map.get("inventoryENC");

                Main.getJavaPlugin().getLogger().info("KITLOAD-SPECIAL-9");

                visual = new ItemStack(Objects.requireNonNull(Material.getMaterial((String) map.get("visual"))));

                Main.getJavaPlugin().getLogger().info("KITLOAD-SPECIAL-10");

                description = (String) map.get("description");

                Main.getJavaPlugin().getLogger().info("KITLOAD-SPECIAL-11");

            } catch (Exception e) {
                Main.getJavaPlugin().getLogger().info("KITLOAD-1 Exeption: " + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            Main.getJavaPlugin().getLogger().info("KITLOAD-2 Exeption: " + e.getMessage());
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

        if (helmet != null) kitDescription.add(ChatColor.YELLOW + "" + helmet.getAmount() + "x " + ChatColor.WHITE + helmet.getType().name().replace('_',' ') + ChatColor.GRAY + " " + getEnchantmentString(helmet));
        if (chestplate != null) kitDescription.add(ChatColor.YELLOW + "" + chestplate.getAmount() + "x " + ChatColor.WHITE + chestplate.getType().name().replace('_',' ') + ChatColor.GRAY + " " + getEnchantmentString(chestplate));
        if (leggings != null) kitDescription.add(ChatColor.YELLOW + "" + leggings.getAmount() + "x " + ChatColor.WHITE + leggings.getType().name().replace('_',' ') + ChatColor.GRAY + " " + getEnchantmentString(leggings));
        if (boots != null) kitDescription.add(ChatColor.YELLOW + "" + boots.getAmount() + "x " + ChatColor.WHITE + boots.getType().name().replace('_',' ') + ChatColor.GRAY + " " + getEnchantmentString(boots));

        if (leftHand != null) kitDescription.add(ChatColor.YELLOW + "" + leftHand.getAmount() + "x " + ChatColor.WHITE + leftHand.getType().name().replace('_',' ') + ChatColor.GRAY + " " + getEnchantmentString(leftHand));

        Arrays.stream(inventory)
                .filter(Objects::nonNull)
                .forEach(item -> kitDescription.add(ChatColor.YELLOW + "" + item.getAmount() + "x " + ChatColor.WHITE + item.getType().name().replace('_',' ') + ChatColor.GRAY + " " + getEnchantmentString(item)));

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
