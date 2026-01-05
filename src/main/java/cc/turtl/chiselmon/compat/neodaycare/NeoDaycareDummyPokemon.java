package cc.turtl.chiselmon.compat.neodaycare;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.minecraft.network.chat.Component;

import java.util.Set;

public class NeoDaycareDummyPokemon extends Pokemon {
    private final Pokemon originalPokemon;

    public NeoDaycareDummyPokemon(NeoDaycareEgg eggData) {
        this.originalPokemon = eggData.getOriginalPokemon();
        NeoDaycareEgg.Egg egg = eggData.getEgg();

        // Follow the ordering from Pokemon.copyFrom()

        // Keep uuid of the original egg pokemon
        setUuid(originalPokemon.getUuid());

        // set features first maybe?
        setFeatures(egg.getFeatures());

        // Set ability before species/form
        setAbility$common(egg.getAbility());

        // Set species, then form
        setSpecies(egg.getSpecies());
        setForm(egg.getForm());

        setNickname(Component.literal("(EGG) " + egg.getSpecies().getName()));
        setLevel(egg.getLevel());

        // Set IVs (using doWithoutEmitting pattern like copyFrom)
        getIvs().doWithoutEmitting(() -> {
            for (Stat stat : Stats.Companion.getPERMANENT()) {
                getIvs().set(stat, egg.getIvs().get(stat));
            }
            return null;
        });

        setGender(egg.getGender());
        getMoveSet().doWithoutEmitting(() -> {
            egg.getMoveSet().forEach(move -> {
                if (move != null) {
                    getMoveSet().add(move.copy());
                }
            });
            return null;
        });
        setScaleModifier(egg.getScaleModifier());
        setShiny(egg.isShiny());

        if (egg.getCaughtBall() != null) {
            setCaughtBall(egg.getCaughtBall());
        }

        setNature(egg.getNature());
        setTeraType(egg.getTeraType());
        setTradeable(egg.isTradeable());

        setForcedAspects(Set.of(NeoDaycareEgg.DUMMY_ASPECT));
        updateAspects();
    }

    public int getStepsRemaining() {
        return NeoDaycareEgg.from(originalPokemon).getStepsRemaining();
    }

    public float getHatchCompletion() {
        return NeoDaycareEgg.from(originalPokemon).getHatchCompletion();
    }

    public Pokemon getOriginalPokemon() {
        return originalPokemon;
    }
}