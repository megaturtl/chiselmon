package cc.turtl.cobbleaid.integration.neodaycare;

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

        setNickname(Component.literal("(EGG) " + egg.getSpecies().getName()));
        setForm(egg.getForm());
        setSpecies(egg.getSpecies());
        setLevel(egg.getLevel());
        setGender(egg.getGender());
        setShiny(egg.isShiny());
        setScaleModifier(egg.getScaleModifier());
        setNature(egg.getNature());
        setTeraType(egg.getTeraType());
        setAbility$common(egg.getAbility());
        setTradeable(egg.isTradeable());
        setForcedAspects(Set.of(NeoDaycareEgg.DUMMY_ASPECT));

        // keep uuid of the original egg pokemon
        setUuid(eggData.getOriginalPokemon().getUuid());

        for (Stat stat : Stats.Companion.getPERMANENT()) {
            setIV(stat, egg.getIvs().get(stat));
        }

        getMoveSet().clear();
        egg.getMoveSet().getMoves().forEach(getMoveSet()::add);

        if (egg.getCaughtBall() != null) {
            setCaughtBall(egg.getCaughtBall());
        }
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