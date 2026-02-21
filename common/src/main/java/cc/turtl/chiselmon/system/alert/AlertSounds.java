package cc.turtl.chiselmon.system.alert;

import com.cobblemon.mod.common.CobblemonSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum AlertSounds {
    LEVEL_UP("Level Up", SoundEvents.PLAYER_LEVELUP),
    EXP_ORB("Exp Orb", SoundEvents.EXPERIENCE_ORB_PICKUP),
    PLING("Note Block Pling", SoundEvents.NOTE_BLOCK_PLING.value()),
    CHIME("Note Block Chime", SoundEvents.NOTE_BLOCK_CHIME.value()),
    BIT("Note Block 8 Bit", SoundEvents.NOTE_BLOCK_BIT.value()),
    BELL("Bell", SoundEvents.BELL_BLOCK),
    FIREWORK("Firework Launch", SoundEvents.FIREWORK_ROCKET_LAUNCH),
    AMETHYST("Amethyst Shimmer", SoundEvents.AMETHYST_BLOCK_CHIME),
    MACE("Mace Smash", SoundEvents.MACE_SMASH_AIR),
    DRAGON("Dragon Roar", SoundEvents.ENDER_DRAGON_GROWL),
    C("Mace Smash", CobblemonSounds.POKE_BALL_SHINY_SEND_OUT),;

    private final String displayName;
    private final SoundEvent sound;

    AlertSounds(String displayName, SoundEvent sound) {
        this.displayName = displayName;
        this.sound = sound;
    }

    public SoundEvent getSound() {
        return sound;
    }

    @Override
    public String toString() {
        return displayName;
    }
}