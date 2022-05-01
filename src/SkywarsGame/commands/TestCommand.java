package SkywarsGame.commands;

import net.minecraft.server.v1_16_R3.Potions;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.Locale;
import java.util.Map;

public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        Player player = (Player) commandSender;


        player.setHealth(1);
        /*
        player.setPlayerListName(ChatColor.GRAY + player.getName() + ChatColor.RED + " ✗");
        player.setInvisible(true);
        player.setInvulnerable(true);
        player.setCollidable(false);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().clear();
        player.setFireTicks(0);
         */

        /*
        ItemStack item = new ItemStack(Material.getMaterial("SPLASH_POTION"));
        item.setAmount(3);
        ((Damageable) item.getItemMeta()).setDamage(0);
        PotionMeta potionMeta = ((PotionMeta) item.getItemMeta());
        potionMeta.setBasePotionData(new PotionData(PotionType.valueOf("INSTANT_HEAL"), false, false));
        item.setItemMeta(potionMeta);

        player.getInventory().setItem(7, item);

         */

        return true;
    }
}
