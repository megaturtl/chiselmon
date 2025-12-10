package cc.turtl.cobbleaid.integration.neodaycare;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.pokemon.Pokemon;
import cc.turtl.cobbleaid.api.util.IVsUtil;
import net.minecraft.network.chat.Component;
import java.util.Set;

public class NeoDaycareDummyPokemon extends Pokemon {
    private final NeoDaycareEgg eggData;

    public NeoDaycareDummyPokemon(NeoDaycareEgg eggData) {
        this.eggData = eggData;
        NeoDaycareEgg.Egg egg = eggData.getEgg();

        setNickname(Component.literal("(EGG) " + egg.getSpecies().getName()));
        setSpecies(egg.getSpecies());
        setLevel(egg.getLevel());
        setGender(egg.getGender());
        setShiny(egg.isShiny());
        setScaleModifier(egg.getScaleModifier());
        setNature(egg.getNature());
        setTeraType(egg.getTeraType());
        setAbility$common(egg.getAbility());
        setUuid(egg.getUuid());
        setTradeable(egg.isTradeable());
        setForcedAspects(Set.of(NeoDaycareEgg.DUMMY_ASPECT));

        for (Stat stat : IVsUtil.IVS_LIST) {
            setIV(stat, egg.getIvs().get(stat));
        }

        getMoveSet().clear();
        egg.getMoveSet().getMoves().forEach(getMoveSet()::add);

        if (egg.getCaughtBall() != null) {
            setCaughtBall(egg.getCaughtBall());
        }
    }

    public int getStepsRemaining() {
        return eggData.getStepsRemaining();
    }

    public float getHatchCompletion() {
        return eggData.getHatchCompletion();
    }
}