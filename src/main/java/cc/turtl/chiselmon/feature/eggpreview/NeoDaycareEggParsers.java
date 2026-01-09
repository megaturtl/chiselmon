package cc.turtl.chiselmon.feature.eggpreview;

import java.util.ArrayList;
import java.util.List;

import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatureProvider;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Nature;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.util.DataKeys;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import cc.turtl.chiselmon.Chiselmon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;

public class NeoDaycareEggParsers {

    /**
     * Parses the egg NBT string into a CompoundTag
     */
    public static CompoundTag parseEggTag(String eggString) {
        if (eggString == null || eggString.isEmpty()) {
            throw new IllegalStateException("Egg string is null or empty");
        }

        try {
            return TagParser.parseTag(eggString);
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException("Failed to parse egg hatchling NBT", e);
        }
    }

    /**
     * Parses and validates the species from the hatchling tag
     */
    public static Species parseSpecies(CompoundTag hatchlingTag) {
        if (hatchlingTag == null || hatchlingTag.isEmpty()) {
            throw new IllegalStateException("Hatchling tag is null or empty");
        }

        String speciesIdentifier = hatchlingTag.getString(DataKeys.POKEMON_SPECIES_IDENTIFIER);
        if (speciesIdentifier == null || speciesIdentifier.isEmpty()) {
            throw new IllegalStateException("Species identifier is missing");
        }

        ResourceLocation speciesLocation;
        try {
            speciesLocation = ResourceLocation.parse(speciesIdentifier);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid species identifier: " + speciesIdentifier, e);
        }

        Species species = PokemonSpecies.getByIdentifier(speciesLocation);
        if (species == null) {
            throw new IllegalStateException("Species not found: " + speciesIdentifier);
        }

        return species;
    }

    /**
     * Parses the form from the hatchling tag, returns null if not found/invalid
     */
    public static FormData parseForm(CompoundTag hatchlingTag, Species species) {
        if (hatchlingTag == null || species == null) {
            return null;
        }

        String formId = hatchlingTag.getString("FormId");
        if (formId == null || formId.isEmpty()) {
            return null;
        }

        try {
            FormData form = species.getFormByShowdownId(formId);
            if (form == null) {
                Chiselmon.getLogger().warn("Form '{}' not found for species '{}', using default",
                        formId, species.getName());
            }
            return form;
        } catch (Exception e) {
            Chiselmon.getLogger().warn("Error parsing form '{}' for species '{}'",
                    formId, species.getName(), e);
            return null;
        }
    }

    /**
     * Parses gender from the hatchling tag, defaults to MALE if invalid
     */
    public static Gender parseGender(CompoundTag hatchlingTag) {
        if (hatchlingTag == null) {
            return Gender.MALE;
        }

        String genderStr = hatchlingTag.getString(DataKeys.POKEMON_GENDER);
        if (genderStr == null || genderStr.isEmpty()) {
            return Gender.MALE;
        }

        try {
            return Gender.valueOf(genderStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            Chiselmon.getLogger().warn("Invalid gender '{}', using MALE", genderStr);
            return Gender.MALE;
        }
    }

    /**
     * Parses nature from the hatchling tag, defaults to HARDY if invalid
     */
    public static Nature parseNature(CompoundTag hatchlingTag) {
        if (hatchlingTag == null) {
            return Natures.HARDY;
        }

        String natureStr = hatchlingTag.getString(DataKeys.POKEMON_NATURE);
        if (natureStr == null || natureStr.isEmpty()) {
            return Natures.HARDY;
        }

        try {
            Nature nature = Natures.getNature(ResourceLocation.parse(natureStr));
            if (nature != null) {
                return nature;
            } else {
                Chiselmon.getLogger().warn("Nature '{}' not found, using Hardy", natureStr);
                return Natures.HARDY;
            }
        } catch (Exception e) {
            Chiselmon.getLogger().warn("Invalid nature '{}', using Hardy", natureStr, e);
            return Natures.HARDY;
        }
    }

    /**
     * Parses pokeball from the hatchling tag, defaults to POKE_BALL if invalid
     */
    public static PokeBall parsePokeBall(CompoundTag hatchlingTag) {
        PokeBall defaultBall = PokeBalls.getPokeBall(new ResourceLocation("cobblemon", "poke_ball"));

        if (hatchlingTag == null) {
            return defaultBall;
        }

        String ballStr = hatchlingTag.getString(DataKeys.POKEMON_CAUGHT_BALL);
        if (ballStr == null || ballStr.isEmpty()) {
            return defaultBall;
        }

        try {
            PokeBall ball = PokeBalls.getPokeBall(ResourceLocation.parse(ballStr));
            if (ball != null) {
                return ball;
            } else {
                Chiselmon.getLogger().warn("Pokeball '{}' not found, using POKE_BALL", ballStr);
                return defaultBall;
            }
        } catch (Exception e) {
            Chiselmon.getLogger().warn("Invalid pokeball '{}', using POKE_BALL", ballStr, e);
            return defaultBall;
        }
    }

    /**
     * Parses level from the hatchling tag, defaults to 1 if invalid
     */
    public static int parseLevel(CompoundTag hatchlingTag) {
        if (hatchlingTag == null) {
            return 1;
        }

        int level = hatchlingTag.getInt(DataKeys.POKEMON_LEVEL);
        return Math.max(1, level);
    }

    /**
     * Parses scale modifier from the hatchling tag, defaults to 1.0f if missing
     */
    public static float parseScaleModifier(CompoundTag hatchlingTag) {
        if (hatchlingTag == null || !hatchlingTag.contains(DataKeys.POKEMON_SCALE_MODIFIER)) {
            return 1.0f;
        }

        return hatchlingTag.getFloat(DataKeys.POKEMON_SCALE_MODIFIER);
    }

    /**
     * Parses shiny status from the hatchling tag
     */
    public static boolean parseShiny(CompoundTag hatchlingTag) {
        if (hatchlingTag == null) {
            return false;
        }

        return hatchlingTag.getBoolean(DataKeys.POKEMON_SHINY);
    }

    /**
     * Loads moves into a MoveSet from the hatchling tag
     */
    public static void loadMoves(CompoundTag hatchlingTag, MoveSet moveSet) {
        if (hatchlingTag == null || moveSet == null) {
            return;
        }

        try {
            moveSet.loadFromNBT(hatchlingTag);
        } catch (Exception e) {
            Chiselmon.getLogger().error("Failed to load moves", e);
        }
    }

    /**
     * Parses IVs from the hatchling tag
     */
    public static IVs parseIVs(CompoundTag tag) {
        if (tag == null || !tag.contains(DataKeys.POKEMON_IVS)) {
            Chiselmon.getLogger().warn("IVs tag missing, returning null");
            return null;
        }

        try {
            CompoundTag ivsTag = tag.getCompound(DataKeys.POKEMON_IVS);
            if (ivsTag == null || ivsTag.isEmpty()) {
                return null;
            }

            if (!ivsTag.contains("Base")) {
                CompoundTag wrapped = new CompoundTag();
                wrapped.put("Base", ivsTag);
                wrapped.put("HyperTrained", new CompoundTag());
                ivsTag = wrapped;
            }

            return IVs.Companion.getCODEC().parse(NbtOps.INSTANCE, ivsTag)
                    .resultOrPartial(Chiselmon.getLogger()::error)
                    .orElse(null);
        } catch (Exception e) {
            Chiselmon.getLogger().error("Failed to parse IVs", e);
            return null;
        }
    }

    /**
     * Parses ability from the hatchling tag
     */
    public static Ability parseAbility(CompoundTag tag) {
        if (tag == null || !tag.contains(DataKeys.POKEMON_ABILITY)) {
            Chiselmon.getLogger().warn("Ability tag missing, returning null");
            return null;
        }

        try {
            CompoundTag abilityTag = tag.getCompound(DataKeys.POKEMON_ABILITY);
            if (abilityTag == null || abilityTag.isEmpty()) {
                return null;
            }

            String abilityName = abilityTag.getString(DataKeys.POKEMON_ABILITY_NAME);
            if (abilityName == null || abilityName.isEmpty()) {
                return null;
            }

            AbilityTemplate template = Abilities.getOrDummy(abilityName);
            if (template == null) {
                return null;
            }

            return template.create(abilityTag);
        } catch (Exception e) {
            Chiselmon.getLogger().error("Failed to parse ability", e);
            return null;
        }
    }

    /**
     * Parses features from the hatchling tag
     */
    public static List<SpeciesFeature> parseFeatures(CompoundTag tag) {
        List<SpeciesFeature> features = new ArrayList<>();

        if (tag == null || !tag.contains("Features")) {
            return features;
        }

        try {
            net.minecraft.nbt.ListTag featuresListTag = tag.getList("Features", 10); // 10 = CompoundTag
            if (featuresListTag == null) {
                return features;
            }

            for (int i = 0; i < featuresListTag.size(); i++) {
                try {
                    CompoundTag featureTag = featuresListTag.getCompound(i);
                    if (featureTag == null || featureTag.isEmpty()) {
                        continue;
                    }

                    String featureId = featureTag.getString("cobblemon:feature_id");
                    if (featureId == null || featureId.isEmpty()) {
                        continue;
                    }

                    SpeciesFeatureProvider<?> provider = SpeciesFeatures.getFeature(featureId);
                    if (provider == null) {
                        continue;
                    }

                    SpeciesFeature feature = provider.invoke(featureTag);
                    if (feature != null) {
                        features.add(feature);
                    }
                } catch (Exception e) {
                    Chiselmon.getLogger().warn("Failed to parse feature at index {}", i, e);
                }
            }
        } catch (Exception e) {
            Chiselmon.getLogger().error("Failed to parse features list", e);
        }

        return features;
    }
}