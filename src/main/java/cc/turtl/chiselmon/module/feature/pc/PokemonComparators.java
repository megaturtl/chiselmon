package cc.turtl.chiselmon.api.comparator;

import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.module.feature.eggpreview.NeoDaycareEggDummy;

import java.util.Comparator;

public final class PokemonComparators {
    private PokemonComparators() {
    }

    public static final Comparator<Pokemon> SIZE_COMPARATOR = Comparator
            .nullsLast(Comparator.comparingDouble(Pokemon::getScaleModifier));
    public static final Comparator<Pokemon> IVS_COMPARATOR = Comparator
            .nullsLast(Comparator.comparingInt(p -> p.getIvs().getEffectiveBattleTotal()));
    public static final Comparator<Pokemon> LEVEL_COMPARATOR = Comparator
            .nullsLast(Comparator.comparingInt(p -> p.getLevel()));
    public static final Comparator<Pokemon> POKEDEX_COMPARATOR = Comparator
            .nullsLast(Comparator.comparingInt(p -> p.getSpecies().getNationalPokedexNumber()));
    public static final Comparator<Pokemon> EGG_DUMMY_COMPARATOR = Comparator
            .nullsLast(Comparator.comparing((Pokemon p) -> p.getAspects().contains(NeoDaycareEggDummy.DUMMY_ASPECT)));
}