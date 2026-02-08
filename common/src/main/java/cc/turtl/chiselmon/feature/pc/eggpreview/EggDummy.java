package cc.turtl.chiselmon.feature.pc.eggpreview;

import cc.turtl.chiselmon.ChiselmonConstants;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class EggDummy extends Pokemon {
    public static final String DUMMY_ASPECT = "EggDummy";
    public static final ResourceLocation EGG_SPECIES_ID = ResourceLocation.fromNamespaceAndPath("neodaycare", "egg_species");

    private final Pokemon originalEgg;
    private int cyclesRemaining;
    private int totalCycles;

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

            if (Minecraft.getInstance().level == null) return Optional.empty();
            RegistryAccess registries = Minecraft.getInstance().level.registryAccess();

            // 2. Pass the registries to the load method
            // Note: The method signature is usually loadFromNBT(RegistryAccess, CompoundTag)
            // in recent Cobblemon versions.
            dummy.loadFromNBT(registries, hatchlingNbt);

            // Sync identity and hatch progress
            dummy.setUuid(egg.getUuid());
            dummy.updateHatchProgress(egg);
            dummy.getForcedAspects().add(DUMMY_ASPECT);

            return Optional.of(dummy);
        } catch (Exception e) {
            ChiselmonConstants.LOGGER.error("Failed to parse hatchling for egg: {}", egg.getUuid(), e);
            return Optional.empty();
        }
    }

    public Pokemon getOriginalEgg() {
        return originalEgg;
    }

    public void updateHatchProgress(Pokemon egg) {
        CompoundTag tag = egg.getPersistentData();
        this.cyclesRemaining = tag.contains("Cycle") ? tag.getInt("Cycle") : 0;
        this.totalCycles = tag.contains("SpeciesCycles") ? tag.getInt("SpeciesCycles") : 40;
    }

    public float getHatchCompletion() {
        if (totalCycles <= 0) return 1.0f;
        return Math.max(0f, 1f - ((float) cyclesRemaining / totalCycles));
    }

    public int getCyclesCompleted() {
        return totalCycles - cyclesRemaining;
    }

    public int getTotalCycles() {
        return totalCycles;
    }
}