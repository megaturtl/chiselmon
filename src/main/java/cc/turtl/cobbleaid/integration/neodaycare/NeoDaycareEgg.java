package cc.turtl.cobbleaid.integration.neodaycare;

import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Nature;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.moves.MoveSet;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.api.types.tera.TeraType;
import com.cobblemon.mod.common.api.types.tera.TeraTypes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import com.cobblemon.mod.common.util.DataKeys;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.chat.Component;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.api.util.IVsUtil;
import cc.turtl.cobbleaid.config.ModConfig;

import java.util.Set;
import java.util.UUID;

public class NeoDaycareEgg {

    public int cycle;
    public Egg egg;
    public int speciesCycles;
    public int steps;
    public int totalSteps;

    public NeoDaycareEgg() {
    }

    public NeoDaycareEgg(CompoundTag dataTag) {
        this.cycle = dataTag.getInt("Cycle");
        this.speciesCycles = dataTag.getInt("SpeciesCycles");
        this.steps = dataTag.getInt("Steps");
        this.totalSteps = dataTag.getInt("TotalSteps");

        String eggString = dataTag.getString("Egg");

        try {
            CompoundTag eggTag = net.minecraft.nbt.TagParser.parseTag(eggString);
            this.egg = new Egg(eggTag);
        } catch (CommandSyntaxException e) {
            CobbleAid.getLogger().error("Failed to parse Egg NBT string!", e);
        }
    }

    public static class Egg {
        public Ability ability;
        public PokeBall caughtBall;
        public String formId;
        public int friendship;
        public Gender gender;
        public int health;
        public IVs ivs;
        public int level;
        public MoveSet moveSet;
        public Nature nature;
        public float scaleModifier;
        public boolean shiny;
        public Species species;
        public TeraType teraType;
        public boolean tradeable;
        public UUID uuid;
        public int cobblemonDataVersion;

        public Egg() {
        }

        public Egg(CompoundTag eggTag) {
            this.formId = eggTag.getString(DataKeys.POKEMON_FORM_ID);
            this.friendship = eggTag.getInt(DataKeys.POKEMON_FRIENDSHIP);
            this.health = eggTag.getInt(DataKeys.POKEMON_HEALTH);
            this.level = eggTag.getInt(DataKeys.POKEMON_LEVEL);
            this.scaleModifier = eggTag.getFloat(DataKeys.POKEMON_SCALE_MODIFIER);
            this.shiny = eggTag.getBoolean(DataKeys.POKEMON_SHINY);
            this.tradeable = eggTag.getBoolean(DataKeys.POKEMON_TRADEABLE);
            this.cobblemonDataVersion = eggTag.getInt("cobblemon:data_version");
            this.gender = Gender.valueOf(eggTag.getString(DataKeys.POKEMON_GENDER));
            this.species = PokemonSpecies
                    .getByIdentifier(ResourceLocation.parse(eggTag.getString(DataKeys.POKEMON_SPECIES_IDENTIFIER)));
            this.nature = Natures.getNature(ResourceLocation.parse(eggTag.getString(DataKeys.POKEMON_NATURE)));
            this.teraType = TeraTypes.get(ResourceLocation.parse(eggTag.getString(DataKeys.POKEMON_TERA_TYPE)));
            this.caughtBall = PokeBalls
                    .getPokeBall(ResourceLocation.parse(eggTag.getString(DataKeys.POKEMON_CAUGHT_BALL)));

            if (eggTag.hasUUID(DataKeys.POKEMON_UUID)) {
                this.uuid = eggTag.getUUID(DataKeys.POKEMON_UUID);
            } else {
                this.uuid = UUID.randomUUID();
            }

            CompoundTag ivsTag = eggTag.getCompound(DataKeys.POKEMON_IVS);
            CompoundTag ivsCodecTag;
            if (ivsTag.contains("Base")) {
                // Structure is already correct, use it directly.
                ivsCodecTag = ivsTag;
            } else {
                // Structure is the simple format and needs to be wrapped.
                ivsCodecTag = new CompoundTag();
                ivsCodecTag.put("Base", ivsTag);
                ivsCodecTag.put("HyperTrained", new CompoundTag());
            }
            try {

                this.ivs = IVs.Companion.getCODEC().parse(NbtOps.INSTANCE, ivsCodecTag)
                        .resultOrPartial(s -> {
                            throw new IllegalStateException("Failed to decode IVs from NBT: " + s);
                        })
                        .orElseThrow(() -> new IllegalStateException("Failed to decode IVs from NBT"));

            } catch (Exception e) {
                CobbleAid.getLogger().error("Failed to decode IVs from CompoundTag!", e);
            }

            CompoundTag abilityTag = eggTag.getCompound(DataKeys.POKEMON_ABILITY);
            try {
                AbilityTemplate abilityTemplate = Abilities
                        .getOrDummy(abilityTag.getString(DataKeys.POKEMON_ABILITY_NAME));
                this.ability = abilityTemplate.create(abilityTag);
            } catch (Exception e) {
                CobbleAid.getLogger().error("Failed to decode Ability from CompoundTag!", e);
            }

            MoveSet moveSet = new MoveSet();
            this.moveSet = moveSet.loadFromNBT(eggTag);
        }

    }

    public static boolean isNeoDaycareEgg(Pokemon pokemon) {
        String speciesResourceName = pokemon.getSpecies().getResourceIdentifier().toString();
        return (speciesResourceName.equals("neodaycare:egg_species"));
    }

    public static boolean isNeoDaycareEggDummy(Pokemon pokemon) {
        return pokemon.getForcedAspects().contains("neoDaycareEggDummy");
    }

    public static NeoDaycareEgg createNeoDaycareEggData(Pokemon pokemon) {
        if (!isNeoDaycareEgg(pokemon)) {
            throw new IllegalArgumentException("Tried to unpack a non NeoDaycare egg!");
        }
        return new NeoDaycareEgg(pokemon.getPersistentData());
    }

    public float getHatchCompletion() {
        float stepsPerCycle = (float) this.totalSteps / this.speciesCycles; // should always be 256 but just in case
        int completedCycles = this.speciesCycles - this.cycle - 1;
        float totalStepsTaken = (completedCycles * stepsPerCycle) + this.steps;
        return totalStepsTaken / this.totalSteps;
    }

    public int getStepsRemaining() {
        float stepsPerCycle = (float) this.totalSteps / this.speciesCycles;
        int completedCycles = this.speciesCycles - this.cycle - 1;
        float totalStepsTakenFloat = (completedCycles * stepsPerCycle) + this.steps;
        int completedSteps = Math.round(totalStepsTakenFloat);
        return this.totalSteps - completedSteps;
    }

    public Pokemon createDummyPokemon() {
        Pokemon dummy = new Pokemon();
        dummy.setNickname(Component.literal("(EGG) " + this.egg.species.getName()));
        dummy.setSpecies(this.egg.species);
        dummy.setLevel(this.egg.level);
        dummy.setGender(this.egg.gender);
        dummy.setShiny(this.egg.shiny);
        dummy.setScaleModifier(this.egg.scaleModifier);
        dummy.setNature(this.egg.nature);
        dummy.setTeraType(this.egg.teraType);
        dummy.setAbility$common(this.egg.ability);
        dummy.setUuid(this.egg.uuid);
        dummy.setTradeable(this.egg.tradeable);
        dummy.setForcedAspects(Set.of("neoDaycareEggDummy"));

        for (Stat stat : IVsUtil.IVS_LIST) {
            dummy.setIV(stat, this.egg.ivs.get(stat));
        }

        MoveSet dummyMoveSet = dummy.getMoveSet();
        dummyMoveSet.clear();
        this.egg.moveSet.getMoves().forEach(move -> {
            dummy.getMoveSet().add(move);
        });

        if (this.egg.caughtBall != null) {
            dummy.setCaughtBall(this.egg.caughtBall);
        }

        return dummy;
    }

    /**
     * Retrieves a preview Pokemon for an egg, or returns the original Pokemon if
     * preview is not applicable.
     * 
     * @param pokemon the Pokemon to create a preview for, or to return if preview
     *                is not applicable.
     * @return a dummy Pokemon preview if the input is a Neo Daycare egg and preview
     *         is enabled, otherwise returns the input Pokemon as-is.
     */
    public static Pokemon getEggPreviewPokemon(Pokemon pokemon) {
        if (pokemon == null) return null;

        ModConfig config = CobbleAid.getInstance().getConfig();

        if (!config.modDisabled && config.showEggPreview && NeoDaycareEgg.isNeoDaycareEgg(pokemon)) {
            return NeoDaycareEgg.createNeoDaycareEggData(pokemon).createDummyPokemon();
        } else {
            return pokemon;
        }
    }
}