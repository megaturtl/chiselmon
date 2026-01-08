package cc.turtl.chiselmon.compat.neodaycare;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.network.chat.Component;
import java.util.Set;

public class NeoDaycareDummyPokemon extends Pokemon {
    private final Pokemon originalPokemon;
    private final NeoDaycareEgg eggData;

    public NeoDaycareDummyPokemon(NeoDaycareEgg eggData) {
        this.eggData = eggData;
        this.originalPokemon = eggData.getOriginalPokemon();
        NeoDaycareEgg.Egg egg = eggData.getEgg();

        setUuid(originalPokemon.getUuid());
        setFeatures(egg.getFeatures());
        setAbility$common(egg.getAbility());
        
        setSpecies(egg.getSpecies());
        setForm(egg.getForm());

        setNickname(Component.literal("(EGG) " + egg.getSpecies().getName()));
        setLevel(egg.getLevel());
        setScaleModifier(egg.getScaleModifier());
        setShiny(egg.isShiny());
        setGender(egg.getGender());
        setNature(egg.getNature());
        setTeraType(egg.getTeraType());

        getIvs().doWithoutEmitting(() -> {
            for (Stat stat : Stats.Companion.getPERMANENT()) {
                getIvs().set(stat, egg.getIvs().get(stat));
            }
            return null;
        });

        getMoveSet().doWithoutEmitting(() -> {
            egg.getMoveSet().forEach(move -> {
                if (move != null) {
                    getMoveSet().add(move.copy());
                }
            });
            return null;
        });

        if (egg.getCaughtBall() != null) {
            setCaughtBall(egg.getCaughtBall());
        }
        setTradeable(egg.isTradeable());

        // Aspect for compatibility with other features
        setForcedAspects(Set.of(NeoDaycareEgg.DUMMY_ASPECT));
        updateAspects();
    }

    public int getStepsRemaining() {
        return eggData.getStepsRemaining();
    }

    public float getHatchCompletion() {
        return eggData.getHatchCompletion();
    }

    public Pokemon getOriginalPokemon() {
        return originalPokemon;
    }
    
    public NeoDaycareEgg getEggData() {
        return eggData;
    }
}