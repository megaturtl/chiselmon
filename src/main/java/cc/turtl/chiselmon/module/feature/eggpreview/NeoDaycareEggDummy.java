package cc.turtl.chiselmon.module.feature.eggpreview;

import java.util.Set;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.Chiselmon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class NeoDaycareEggDummy extends Pokemon {
    private static final String EGG_SPECIES = "neodaycare:egg_species";
    public static final String DUMMY_ASPECT = "neoDaycareEggDummy";

    private final Pokemon originalEggPokemon;
    private int cycle;
    private int speciesCycles;

    private NeoDaycareEggDummy(Pokemon pokemon) {
        this.originalEggPokemon = pokemon;

        CompoundTag persistentTag = pokemon.getPersistentData();

        // Set hatch progress info
        updateHatchProgress(pokemon);

        // Get hatchling info
        String eggString = persistentTag.getString("Egg");
        CompoundTag hatchlingTag = NeoDaycareEggParsers.parseEggTag(eggString);
        loadFromHatchlingData(hatchlingTag);
    }

    private void loadFromHatchlingData(CompoundTag hatchlingTag) {
        try {
            // Parse and set species
            var species = NeoDaycareEggParsers.parseSpecies(hatchlingTag);
            setUuid(originalEggPokemon.getUuid());
            setSpecies(species);

            // Parse and set form
            var form = NeoDaycareEggParsers.parseForm(hatchlingTag, species);
            if (form != null) {
                setForm(form);
            }

            // Set nickname
            setNickname(Component.literal("(EGG) " + species.getName()));

            // Set basic properties
            setLevel(NeoDaycareEggParsers.parseLevel(hatchlingTag));
            setScaleModifier(NeoDaycareEggParsers.parseScaleModifier(hatchlingTag));
            setShiny(NeoDaycareEggParsers.parseShiny(hatchlingTag));
            setGender(NeoDaycareEggParsers.parseGender(hatchlingTag));
            setNature(NeoDaycareEggParsers.parseNature(hatchlingTag));
            setCaughtBall(NeoDaycareEggParsers.parsePokeBall(hatchlingTag));

            // Load moves
            NeoDaycareEggParsers.loadMoves(hatchlingTag, getMoveSet());

            // Parse and set IVs
            IVs parsedIvs = NeoDaycareEggParsers.parseIVs(hatchlingTag);
            if (parsedIvs != null) {
                getIvs().doWithoutEmitting(() -> {
                    for (Stat stat : Stats.Companion.getPERMANENT()) {
                        getIvs().set(stat, parsedIvs.get(stat));
                    }
                    return null;
                });
            }

            // Parse and set ability
            var ability = NeoDaycareEggParsers.parseAbility(hatchlingTag);
            if (ability != null) {
                setAbility$common(ability);
            }

            // Parse and set features
            var features = NeoDaycareEggParsers.parseFeatures(hatchlingTag);
            if (!features.isEmpty()) {
                setFeatures(features);
            }

            setForcedAspects(Set.of(DUMMY_ASPECT));

        } catch (IllegalStateException e) {
            // Re-throw parsing errors with context
            Chiselmon.getLogger().error("Failed to load egg data for {}: {}",
                    originalEggPokemon.getUuid(), e.getMessage());
            throw e;
        } catch (Exception e) {
            Chiselmon.getLogger().error("Unexpected error loading egg data for {}",
                    originalEggPokemon.getUuid(), e);
            throw new IllegalStateException("Failed to load egg hatchling data", e);
        }
    }

    public void updateHatchProgress(Pokemon pokemon) {

        if (!NeoDaycareEggDummy.isEgg(pokemon)) {
            return;
        }

        CompoundTag tag = pokemon.getPersistentData();

        if (tag == null) {
            return;
        }

        this.cycle = tag.contains("Cycle") ? tag.getInt("Cycle") : 40;
        this.speciesCycles = tag.contains("SpeciesCycles") ? tag.getInt("SpeciesCycles") : 40;
    }

    public float getHatchCompletion() {
        int completedCycles = Math.max(0, speciesCycles - cycle);
        return Math.max(0.0f, Math.min(1.0f, completedCycles / (float) speciesCycles));
    }

    public int getCyclesCompleted() {
        return Math.max(0, speciesCycles - cycle);
    }

    public Pokemon getOriginalEggPokemon() {
        return originalEggPokemon;
    }

    public static boolean isEgg(Pokemon pokemon) {
        if (pokemon == null) {
            return false;
        }
        try {
            var species = pokemon.getSpecies();
            if (species == null) {
                return false;
            }
            var identifier = species.getResourceIdentifier();
            if (identifier == null) {
                return false;
            }
            return EGG_SPECIES.equals(identifier.toString());
        } catch (Exception e) {
            Chiselmon.getLogger().error("Error checking if pokemon is egg", e);
            return false;
        }
    }

    public static boolean isDummy(Pokemon pokemon) {
        if (pokemon == null) {
            return false;
        }
        try {
            var aspects = pokemon.getForcedAspects();
            return aspects != null && aspects.contains(DUMMY_ASPECT);
        } catch (Exception e) {
            return false;
        }
    }

    public static NeoDaycareEggDummy createEggFrom(Pokemon pokemon) {
        if (pokemon == null) {
            throw new IllegalArgumentException("Pokemon cannot be null");
        }
        if (!isEgg(pokemon)) {
            throw new IllegalArgumentException("Pokemon is not a NeoDaycare egg");
        }
        return new NeoDaycareEggDummy(pokemon);
    }
}