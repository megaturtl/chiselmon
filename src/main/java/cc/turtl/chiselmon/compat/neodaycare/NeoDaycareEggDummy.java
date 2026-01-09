package cc.turtl.chiselmon.compat.neodaycare;

import java.util.Set;

import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.DataKeys;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class NeoDaycareEggDummy extends Pokemon {
    private static final String EGG_SPECIES = "neodaycare:egg_species";
    public static final String DUMMY_ASPECT = "neoDaycareEggDummy";

    private final Pokemon originalEggPokemon;

    private NeoDaycareEggDummy(Pokemon pokemon) {
        CompoundTag eggTag = pokemon.getPersistentData();
        CompoundTag hatchlingTag = eggTag.getCompound("Egg");

        this.originalEggPokemon = pokemon;

        // hatchling data
        setUuid(originalEggPokemon.getUuid());
        setSpecies(PokemonSpecies.getByIdentifier(
                ResourceLocation.parse(hatchlingTag.getString(DataKeys.POKEMON_SPECIES_IDENTIFIER))));
        setForm(getSpecies().getFormByShowdownId(hatchlingTag.getString("FormId")));

        setNickname(Component.literal("(EGG) " + PokemonSpecies.getByIdentifier(
                ResourceLocation.parse(hatchlingTag.getString(DataKeys.POKEMON_SPECIES_IDENTIFIER))).getName()));
        setLevel(hatchlingTag.getInt(DataKeys.POKEMON_LEVEL));
        setScaleModifier(hatchlingTag.getFloat(DataKeys.POKEMON_SCALE_MODIFIER));
        setShiny(hatchlingTag.getBoolean(DataKeys.POKEMON_SHINY));
        setGender(Gender.valueOf(hatchlingTag.getString(DataKeys.POKEMON_GENDER)));
        setNature(Natures.getNature(
                ResourceLocation.parse(hatchlingTag.getString(DataKeys.POKEMON_NATURE))));
        setCaughtBall(PokeBalls.getPokeBall(
                ResourceLocation.parse(hatchlingTag.getString(DataKeys.POKEMON_CAUGHT_BALL))));
        getMoveSet().loadFromNBT(hatchlingTag);

        IVs parsedIvs = NeoDaycareEggParsers.parseIVs(hatchlingTag);
        getIvs().doWithoutEmitting(() -> {
            for (Stat stat : Stats.Companion.getPERMANENT()) {
                getIvs().set(stat, parsedIvs.get(stat));
            }
            return null;
        });

        setAbility$common(NeoDaycareEggParsers.parseAbility(hatchlingTag));
        setFeatures(NeoDaycareEggParsers.parseFeatures(hatchlingTag));
        setForcedAspects(Set.of(DUMMY_ASPECT));
    }

    public static boolean isEgg(Pokemon pokemon) {
        return EGG_SPECIES.equals(pokemon.getSpecies().getResourceIdentifier().toString());
    }

    public static boolean isDummy(Pokemon pokemon) {
        return pokemon.getForcedAspects().contains(DUMMY_ASPECT);
    }

    public static NeoDaycareEggDummy createEggFrom(Pokemon pokemon) {
        if (!isEgg(pokemon)) {
            throw new IllegalArgumentException("Pokemon is not a NeoDaycare egg");
        }
        return new NeoDaycareEggDummy(pokemon);
    }

    public float getHatchCompletion() {
        CompoundTag eggTag = originalEggPokemon.getPersistentData();
        int cycle = eggTag.getInt("Cycle");
        int speciesCycles = eggTag.getInt("SpeciesCycles");
        int steps = eggTag.getInt("Steps");
        int totalSteps = eggTag.getInt("TotalSteps");

        float stepsPerCycle = (float) totalSteps / speciesCycles;
        int completedCycles = speciesCycles - cycle - 1;
        return ((completedCycles * stepsPerCycle) + steps) / totalSteps;
    }

    public int getStepsRemaining() {
        CompoundTag eggTag = originalEggPokemon.getPersistentData();
        int cycle = eggTag.getInt("Cycle");
        int speciesCycles = eggTag.getInt("SpeciesCycles");
        int steps = eggTag.getInt("Steps");
        int totalSteps = eggTag.getInt("TotalSteps");

        float stepsPerCycle = (float) totalSteps / speciesCycles;
        int completedCycles = speciesCycles - cycle - 1;
        int completedSteps = Math.round((completedCycles * stepsPerCycle) + steps);
        return totalSteps - completedSteps;
    }

    public Pokemon getOriginalEggPokemon() {
        return originalEggPokemon;
    }
}
