package cc.turtl.chiselmon.compat.neodaycare;

import java.util.ArrayList;
import java.util.List;

import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatureProvider;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.util.DataKeys;

import cc.turtl.chiselmon.Chiselmon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public class NeoDaycareEggParsers {
    public static IVs parseIVs(CompoundTag tag) {
        CompoundTag ivsTag = tag.getCompound(DataKeys.POKEMON_IVS);
        if (!ivsTag.contains("Base")) {
            CompoundTag wrapped = new CompoundTag();
            wrapped.put("Base", ivsTag);
            wrapped.put("HyperTrained", new CompoundTag());
            ivsTag = wrapped;
        }

        return IVs.Companion.getCODEC().parse(NbtOps.INSTANCE, ivsTag)
                .resultOrPartial(Chiselmon.getLogger()::error)
                .orElseThrow(() -> new IllegalStateException("Failed to decode IVs"));
    }

    public static Ability parseAbility(CompoundTag tag) {
        CompoundTag abilityTag = tag.getCompound(DataKeys.POKEMON_ABILITY);
        AbilityTemplate template = Abilities.getOrDummy(
                abilityTag.getString(DataKeys.POKEMON_ABILITY_NAME));
        return template.create(abilityTag);
    }

    public static List<SpeciesFeature> parseFeatures(CompoundTag tag) {
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
}
