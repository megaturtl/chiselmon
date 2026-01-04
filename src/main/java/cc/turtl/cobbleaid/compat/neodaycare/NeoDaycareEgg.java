package cc.turtl.cobbleaid.compat.neodaycare;

import com.cobblemon.mod.common.pokemon.*;
import com.cobblemon.mod.common.api.abilities.*;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.api.pokemon.*;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.api.types.tera.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import com.cobblemon.mod.common.util.DataKeys;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.config.ModConfig;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatureProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NeoDaycareEgg {
    private static final String EGG_SPECIES = "neodaycare:egg_species";
    public static final String DUMMY_ASPECT = "neoDaycareEggDummy";

    // Cache of dummy Pokemon by original Pokemon UUID
    private static final Map<UUID, NeoDaycareDummyPokemon> DUMMY_CACHE = new HashMap<>();

    private int cycle;
    private int speciesCycles;
    private int steps;
    private int totalSteps;
    private final Egg egg;
    private final Pokemon originalPokemon;

    private NeoDaycareEgg(Pokemon pokemon) {
        CompoundTag tag = pokemon.getPersistentData();
        this.cycle = tag.getInt("Cycle");
        this.speciesCycles = tag.getInt("SpeciesCycles");
        this.steps = tag.getInt("Steps");
        this.totalSteps = tag.getInt("TotalSteps");
        this.egg = new Egg(parseEggTag(tag.getString("Egg")));
        this.originalPokemon = pokemon;
    }

    private static CompoundTag parseEggTag(String eggString) {
        try {
            return net.minecraft.nbt.TagParser.parseTag(eggString);
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException("Failed to parse egg NBT", e);
        }
    }

    public static boolean isEgg(Pokemon pokemon) {
        return EGG_SPECIES.equals(pokemon.getSpecies().getResourceIdentifier().toString());
    }

    public static boolean isDummy(Pokemon pokemon) {
        return pokemon.getForcedAspects().contains(DUMMY_ASPECT);
    }

    public static NeoDaycareEgg from(Pokemon pokemon) {
        if (!isEgg(pokemon)) {
            throw new IllegalArgumentException("Pokemon is not a NeoDaycare egg");
        }
        return new NeoDaycareEgg(pokemon);
    }

    public static Pokemon getDummyOrOriginal(Pokemon pokemon) {
        if (pokemon == null)
            return null;

        ModConfig config = CobbleAid.services().config().get();
        if (CobbleAid.isDisabled() || !config.pc.showEggPreview || !isEgg(pokemon)) {
            return pokemon;
        }

        // Check cache first - reuse existing dummy if available
        UUID pokemonUuid = pokemon.getUuid();
        NeoDaycareDummyPokemon cached = DUMMY_CACHE.get(pokemonUuid);
        if (cached != null) {
            return cached;
        }

        // Create new dummy and cache it
        NeoDaycareEgg eggData = from(pokemon);
        NeoDaycareDummyPokemon dummy = new NeoDaycareDummyPokemon(eggData);
        DUMMY_CACHE.put(pokemonUuid, dummy);
        return dummy;
    }

    public static void clearCache() {
        DUMMY_CACHE.clear();
    }

    public static void removeCached(UUID pokemonUuid) {
        DUMMY_CACHE.remove(pokemonUuid);
    }

    public float getHatchCompletion() {
        float stepsPerCycle = (float) totalSteps / speciesCycles;
        int completedCycles = speciesCycles - cycle - 1;
        return ((completedCycles * stepsPerCycle) + steps) / totalSteps;
    }

    public int getStepsRemaining() {
        float stepsPerCycle = (float) totalSteps / speciesCycles;
        int completedCycles = speciesCycles - cycle - 1;
        int completedSteps = Math.round((completedCycles * stepsPerCycle) + steps);
        return totalSteps - completedSteps;
    }

    public Egg getEgg() {
        return egg;
    }

    public Pokemon getOriginalPokemon() {
        return originalPokemon;
    }

    public static class Egg {
        private final Ability ability;
        private final PokeBall caughtBall;
        private final Gender gender;
        private final IVs ivs;
        private final int level;
        private final MoveSet moveSet;
        private final Nature nature;
        private final float scaleModifier;
        private final boolean shiny;
        private final Species species;
        private final TeraType teraType;
        private final boolean tradeable;
        private final UUID uuid;
        private final FormData form;
        private final List<SpeciesFeature> features;

        private Egg(CompoundTag tag) {
            this.level = tag.getInt(DataKeys.POKEMON_LEVEL);
            this.scaleModifier = tag.getFloat(DataKeys.POKEMON_SCALE_MODIFIER);
            this.shiny = tag.getBoolean(DataKeys.POKEMON_SHINY);
            this.tradeable = tag.getBoolean(DataKeys.POKEMON_TRADEABLE);
            this.gender = Gender.valueOf(tag.getString(DataKeys.POKEMON_GENDER));
            this.species = PokemonSpecies.getByIdentifier(
                    ResourceLocation.parse(tag.getString(DataKeys.POKEMON_SPECIES_IDENTIFIER)));
            this.nature = Natures.getNature(
                    ResourceLocation.parse(tag.getString(DataKeys.POKEMON_NATURE)));
            this.teraType = TeraTypes.get(
                    ResourceLocation.parse(tag.getString(DataKeys.POKEMON_TERA_TYPE)));
            this.caughtBall = PokeBalls.getPokeBall(
                    ResourceLocation.parse(tag.getString(DataKeys.POKEMON_CAUGHT_BALL)));
            this.uuid = tag.hasUUID(DataKeys.POKEMON_UUID)
                    ? tag.getUUID(DataKeys.POKEMON_UUID)
                    : UUID.randomUUID();
            this.ivs = parseIVs(tag);
            this.ability = parseAbility(tag);
            this.moveSet = new MoveSet().loadFromNBT(tag);
            this.form = this.species.getFormByShowdownId(tag.getString("FormId"));
            this.features = parseFeatures(tag);
        }

        private static IVs parseIVs(CompoundTag tag) {
            CompoundTag ivsTag = tag.getCompound(DataKeys.POKEMON_IVS);
            if (!ivsTag.contains("Base")) {
                CompoundTag wrapped = new CompoundTag();
                wrapped.put("Base", ivsTag);
                wrapped.put("HyperTrained", new CompoundTag());
                ivsTag = wrapped;
            }

            return IVs.Companion.getCODEC().parse(NbtOps.INSTANCE, ivsTag)
                    .resultOrPartial(CobbleAid.getLogger()::error)
                    .orElseThrow(() -> new IllegalStateException("Failed to decode IVs"));
        }

        private static Ability parseAbility(CompoundTag tag) {
            CompoundTag abilityTag = tag.getCompound(DataKeys.POKEMON_ABILITY);
            AbilityTemplate template = Abilities.getOrDummy(
                    abilityTag.getString(DataKeys.POKEMON_ABILITY_NAME));
            return template.create(abilityTag);
        }

        private static List<SpeciesFeature> parseFeatures(CompoundTag tag) {
            List<SpeciesFeature> features = new ArrayList<>();

            // The Features are stored as a list in NBT under the "Features" key
            if (tag.contains("Features")) {
                net.minecraft.nbt.ListTag featuresListTag = tag.getList("Features", 10); // 10 = CompoundTag

                for (int i = 0; i < featuresListTag.size(); i++) {
                    CompoundTag featureTag = featuresListTag.getCompound(i);

                    // Each feature has a "cobblemon:feature_id" key
                    String featureId = featureTag.getString("cobblemon:feature_id");

                    if (!featureId.isEmpty()) {
                        SpeciesFeatureProvider<?> provider = SpeciesFeatures.getFeature(featureId);

                        if (provider != null) {
                            // Use invoke(CompoundTag) to create feature from NBT
                            SpeciesFeature feature = provider.invoke(featureTag);
                            if (feature != null) {
                                features.add(feature);
                            }
                        }
                    }
                }
            }

            return features;
        }

        public Ability getAbility() {
            return ability;
        }

        public PokeBall getCaughtBall() {
            return caughtBall;
        }

        public Gender getGender() {
            return gender;
        }

        public IVs getIvs() {
            return ivs;
        }

        public int getLevel() {
            return level;
        }

        public MoveSet getMoveSet() {
            return moveSet;
        }

        public Nature getNature() {
            return nature;
        }

        public float getScaleModifier() {
            return scaleModifier;
        }

        public boolean isShiny() {
            return shiny;
        }

        public Species getSpecies() {
            return species;
        }

        public TeraType getTeraType() {
            return teraType;
        }

        public boolean isTradeable() {
            return tradeable;
        }

        public UUID getUuid() {
            return uuid;
        }

        public FormData getForm() {
            return form;
        }

        public List<SpeciesFeature> getFeatures() {
            return features;
        }
    }
}
