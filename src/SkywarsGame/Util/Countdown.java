package SkywarsGame.Util;

import SkywarsGame.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.scheduler.BukkitRunnable;

public class Countdown {

    public static void initiateCountdown(int ticks, Language title) {

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.setExp(1);
            player.setLevel(ticks / 20);
        });

        final float division = 1F / ticks;

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getExp() <= division) {
                        player.setExp(0);
                        this.cancel();
                    } else {
                        player.setExp(player.getExp() - division);
                    }
                });
            }
        }.runTaskTimer(Main.getJavaPlugin(), 0, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if(player.getLevel() <= 0){
                        this.cancel();
                    } else {
                        player.setLevel(player.getLevel()-1);
                        if(player.getLevel()<=3 && player.getLevel() > 0){
                            player.sendTitle(title.getText(), ChatColor.GREEN + "" + player.getLevel(), 0, 20, 0);
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 1, 0.5F);
                        }
                    }
                });
            }
        }.runTaskTimer(Main.getJavaPlugin(), 0, 20);
    }

}
