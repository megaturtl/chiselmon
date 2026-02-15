package cc.turtl.chiselmon.system.alert;

import cc.turtl.chiselmon.api.filter.RuntimeFilter;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

public record AlertContext(
        PokemonEntity entity,
        RuntimeFilter filter,
        boolean isMuted,
        AlertConfig config
) {
    public Pokemon pokemon() {
        return entity.getPokemon();
    }


    public boolean isBlacklisted() {
        return config.blacklist.contains(pokemon().getSpecies().getName());
    }

    public boolean shouldAlert() {
        return config.masterEnabled && !isBlacklisted();
    }

    public boolean shouldSound() {
        return shouldAlert() && !isMuted;
    }

    public boolean shouldMessage() {
        return shouldAlert() && !isMuted;
    }

    public boolean shouldHighlight() {
        return shouldAlert();
    }

    public float getEffectiveVolume() {
        return (config.masterVolume / 100f) * 100 / 100f;
    }

    public float getEffectivePitch() {
        return (1 / 100f);
    }
}
