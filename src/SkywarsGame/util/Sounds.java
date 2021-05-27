package SkywarsGame.util;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public enum Sounds {

    DEATH(Sound.BLOCK_ANVIL_LAND, 0.5F),
    KILL(Sound.ENTITY_PLAYER_LEVELUP, 1.5F),
    CLICK_TIMER(Sound.BLOCK_NOTE_BLOCK_HAT, 1),
    CLICK_TIMER_END(Sound.BLOCK_NOTE_BLOCK_HARP, 2),
    WIN(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.1F),
    LOSE(Sound.ENCHANT_THORNS_HIT, 0.5F);

    private final Sound sound;
    private final float pitch;

    Sounds(Sound sound, float pitch) {
        this.sound = sound;
        this.pitch = pitch;
    }

    public void playSoundForPlayer(Player player){
        player.playSound(player.getLocation(), sound, SoundCategory.AMBIENT, 1, pitch);
    }

}
