package cc.turtl.chiselmon.feature.pc.eggspy;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.duck.DuckPreviewPokemon;
import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeature;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.RenderablePokemon;
import com.cobblemon.mod.common.pokemon.Species;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Representation of the pokemon a NeoDaycare egg will hatch into.
 *<p>
 * To most methods, will look like a regular pokemon object, only with
 * a forced aspect "EggDummy" for recognition by Chiselmon methods.
 */
public class EggDummy extends Pokemon {
    public static final String DUMMY_ASPECT = "EggDummy";
    public static final ResourceLocation EGG_SPECIES_ID = ResourceLocation.fromNamespaceAndPath("neodaycare", "egg_species");
    public static final String HATCH_PERCENTAGE_FEATURE = "hatch_percentage";

    private final Pokemon originalEgg;
    private int totalSteps;

    public EggDummy(Pokemon originalEgg) {
        this.originalEgg = originalEgg;
    }

    // Static factory method: The "New Parser"
    public static Optional<EggDummy> from(Pokemon egg) {
        String eggData = egg.getPersistentData().getString("Egg");
        if (eggData.isEmpty()) return Optional.empty();

        try {
            CompoundTag hatchlingNbt = TagParser.parseTag(eggData);
            EggDummy dummy = new EggDummy(egg);

            dummy.totalSteps = egg.getPersistentData().getInt("TotalSteps");

            if (Minecraft.getInstance().level == null) return Optional.empty();
            RegistryAccess registries = Minecraft.getInstance().level.registryAccess();

            // Pass the registries to the load method
            dummy.loadFromNBT(registries, hatchlingNbt);

            // Sync identity and hatch progress
            dummy.setUuid(egg.getUuid());
            Set<String> aspects = new HashSet<>(dummy.getForcedAspects());
            aspects.add(DUMMY_ASPECT);
            dummy.setForcedAspects(aspects);

            return Optional.of(dummy);
        } catch (Exception e) {
            ChiselmonConstants.LOGGER.error("Failed to parse hatchling for egg: {}", egg.getUuid(), e);
            return Optional.empty();
        }
    }

    public Pokemon getOriginalEgg() {
        return originalEgg;
    }

    /**
     * Pulled from the original Egg Pokemon object's 'features' list.
     * Returns 0 if no hatch percentage feature is found.
     */
    public int getHatchPercentage() {
        IntSpeciesFeature hatchFeature = this.originalEgg.getFeature(HATCH_PERCENTAGE_FEATURE);
        if (hatchFeature != null) {
            return hatchFeature.getValue();
        }
        return 0;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public int getStepsRemaining() {
        return totalSteps - (totalSteps * getHatchPercentage() / 100);
    }

    public RenderablePokemon getOriginalRenderablePokemon() {
        return ((DuckPreviewPokemon) originalEgg).chiselmon$getRawRenderablePokemon();
    }
}