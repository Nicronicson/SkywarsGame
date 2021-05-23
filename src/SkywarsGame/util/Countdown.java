package SkywarsGame.util;

import SkywarsGame.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Countdown {

    private static final Set<Integer> EXACT_CHAT_CALLS = new HashSet<>(Arrays.asList(60, 30, 20, 10, 5, 3, 2, 1));
    private static final int XP_BAR_TICK_SPEED = 4;

    private static BukkitTask levelCountdown;
    private static BukkitTask xpBarCountdown;
    private static BukkitTask chatCountdown;


    public static void createLevelCountdown(int seconds, Language title){

        cancelLevelCountdown();

        Bukkit.getOnlinePlayers().forEach(player -> player.setLevel(seconds));

        levelCountdown = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getLevel() <= 0) {
                        if (!this.isCancelled()) this.cancel();
                        return;
                    }

                    player.setLevel(player.getLevel() - 1);

                    if (player.getLevel() <= 3) {
                        player.sendTitle(title.getText(), ChatColor.GREEN + "" + player.getLevel(), 0, 20, 0);
                    }
                });
            }
        }.runTaskTimer(Main.getJavaPlugin(), 20, 20);
    }

    public static void createXpBarCountdown(int seconds) {

        cancelXpBarCountdown();

        Bukkit.getOnlinePlayers().forEach(player -> player.setExp(1));
        final float division = XP_BAR_TICK_SPEED / (seconds*20F);

        xpBarCountdown = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getExp() <= division) {
                        if (!this.isCancelled()) this.cancel();
                        player.setExp(0);
                        return;
                    }
                    player.setExp(player.getExp() - division);
                });
            }
        }.runTaskTimer(Main.getJavaPlugin(), 0, XP_BAR_TICK_SPEED);
    }

    public static void createChatCountdown(int seconds, Language text) {

        cancelChatCountdown();

        AtomicInteger remaining = new AtomicInteger(seconds);
        chatCountdown = new BukkitRunnable() {
            @Override
            public void run() {
                if (remaining.get() <= 0){
                    Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, SoundCategory.AMBIENT, 1, 2F));
                    if (!this.isCancelled()) this.cancel();
                    return;
                }
                if (EXACT_CHAT_CALLS.contains(remaining.get())) {
                    Bukkit.broadcastMessage(String.format(text.getFormattedText(), remaining.get()));
                    Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.AMBIENT, 1, 1));
                }
                remaining.getAndDecrement();
            }
        }.runTaskTimer(Main.getJavaPlugin(), 0, 20);
    }

    public static void cancelLevelCountdown(){
        if(levelCountdown==null)return;
        if(!levelCountdown.isCancelled()) levelCountdown.cancel();
        Bukkit.getOnlinePlayers().forEach(player -> player.setLevel(0));
    }

    public static void cancelXpBarCountdown(){
        if(xpBarCountdown==null)return;
        if(!xpBarCountdown.isCancelled()) xpBarCountdown.cancel();
        Bukkit.getOnlinePlayers().forEach(player -> player.setExp(0));
    }

    public static void cancelChatCountdown(){
        if(chatCountdown==null)return;
        if(!chatCountdown.isCancelled()) chatCountdown.cancel();
    }

}