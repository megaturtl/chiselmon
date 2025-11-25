package cc.turtl.cobbleaid.api.filter;

import com.cobblemon.mod.common.pokemon.Pokemon;
import cc.turtl.cobbleaid.api.util.IVsUtil;

import java.util.Comparator;

public interface PokemonComparators {
    public static final Comparator<Pokemon> SIZE_COMPARATOR = Comparator.nullsLast(Comparator.comparingDouble(Pokemon::getScaleModifier));
    public static final Comparator<Pokemon> IVS_COMPARATOR = Comparator.nullsLast(Comparator.comparingInt(p -> IVsUtil.calculateTotalIVs(p.getIvs())));
}