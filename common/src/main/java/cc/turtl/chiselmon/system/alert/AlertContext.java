package cc.turtl.chiselmon.system.alert;

import cc.turtl.chiselmon.system.group.PokemonGroup;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

public record AlertContext(
        PokemonEntity entity,
        PokemonGroup group,
        boolean isMuted,
        AlertConfig config
) {
    public Pokemon pokemon() {
        return entity.getPokemon();
    }

    public AlertConfig.GroupAlertConfig groupConfig() {
        return config.getForGroup(group.id());
    }

    public boolean isBlacklisted() {
        return config.blacklist.contains(pokemon().getSpecies().getName());
    }

    public boolean shouldAlert() {
        return config.masterEnabled && groupConfig().enabled && !isBlacklisted();
    }

    public boolean shouldSound() {
        return shouldAlert() && !isMuted && groupConfig().playSound;
    }

    public boolean shouldMessage() {
        return shouldAlert() && !isMuted && groupConfig().sendChatMessage;
    }

    public boolean shouldHighlight() {
        return shouldAlert() && groupConfig().highlightEntity;
    }

    public float getEffectiveVolume() {
        return (config.masterVolume / 100f) * (groupConfig().volume / 100f);
    }

    public float getEffectivePitch() {
        return (groupConfig().pitch / 100f);
    }
}
