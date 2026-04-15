package cc.turtl.chiselmon.system.alert

import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents

enum class AlertSounds(private val displayName: String, val sound: SoundEvent) {
    LEVEL_UP("Level Up", SoundEvents.PLAYER_LEVELUP),
    EXP_ORB("Exp Orb", SoundEvents.EXPERIENCE_ORB_PICKUP),
    PLING("Note Block Pling", SoundEvents.NOTE_BLOCK_PLING.value()),
    CHIME("Note Block Chime", SoundEvents.NOTE_BLOCK_CHIME.value()),
    BIT("Note Block 8 Bit", SoundEvents.NOTE_BLOCK_BIT.value()),
    BELL("Bell", SoundEvents.BELL_BLOCK),
    FIREWORK("Firework Launch", SoundEvents.FIREWORK_ROCKET_LAUNCH),
    MACE("Mace Smash", SoundEvents.MACE_SMASH_AIR),
    DRAGON("Dragon Roar", SoundEvents.ENDER_DRAGON_GROWL);

    override fun toString(): String = displayName
}
