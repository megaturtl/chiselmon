package cc.turtl.chiselmon.compat.jade;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.util.format.ColorUtils;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import com.cobblemon.mod.common.CobblemonItemComponents;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.block.PokeSnackBlock;
import com.cobblemon.mod.common.block.entity.PokeSnackBlockEntity;
import com.cobblemon.mod.common.client.tooltips.SeasoningTooltipHelperKt;
import com.cobblemon.mod.common.item.components.BaitEffectsComponent;
import com.cobblemon.mod.common.item.components.IngredientComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

import java.util.List;

import static cc.turtl.chiselmon.util.MiscUtil.modResource;

/**
 * Jade tooltip provider for PokeSnack blocks.
 * Displays bites remaining, ingredients, and effects based on config.
 */
public enum PokeSnackProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation BITES = modResource("pokesnack_block.bites");
    public static final ResourceLocation INGREDIENTS = modResource("pokesnack_block.ingredients");
    public static final ResourceLocation EFFECTS = modResource("pokesnack_block.effects");

    private static final ResourceLocation UID = modResource("pokesnack_block");
    private static final int MAX_BITES = 9;

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (ChiselmonConfig.get().general.modDisabled) return;
        if (!(accessor.getBlock() instanceof PokeSnackBlock)) {
            return;
        }

        BlockState state = accessor.getBlockState();
        PokeSnackBlockEntity entity = (PokeSnackBlockEntity) accessor.getBlockEntity();

        addBitesRemaining(tooltip, config, state);
        addIngredients(tooltip, config, entity);
        addEffects(tooltip, config, entity);
    }

    private void addBitesRemaining(ITooltip tooltip, IPluginConfig config, BlockState state) {
        if (!config.get(BITES)) {
            return;
        }

        int bites = state.getValue(PokeSnackBlock.Companion.getBITES());
        int remaining = MAX_BITES - bites;

        MutableComponent label = Component.translatable("chiselmon.ui.label.pokesnack_block.bites_remaining");
        tooltip.add(ComponentUtils.labelled(label, remaining));
    }

    private void addIngredients(ITooltip tooltip, IPluginConfig config, PokeSnackBlockEntity entity) {
        if (!config.get(INGREDIENTS)) {
            return;
        }

        IngredientComponent ingredients = entity.getIngredientComponent();

        if (ingredients == null) {
            addNoIngredientsWarning(tooltip);
            return;
        }

        addIngredientIcons(tooltip, ingredients);
    }

    private void addNoIngredientsWarning(ITooltip tooltip) {
        MutableComponent warning = Component.translatable("chiselmon.ui.label.pokesnack_block.no_ingredients");
        tooltip.add(warning.withColor(ColorUtils.RED.getRGB()));
    }

    private void addIngredientIcons(ITooltip tooltip, IngredientComponent ingredients) {
        IElementHelper helper = IElementHelper.get();
        tooltip.add(helper.spacer(0, 0));

        boolean first = true;
        for (ResourceLocation ingredientId : ingredients.getIngredientIds()) {
            if (!first) {
                tooltip.append(helper.spacer(1, 0));
            }

            ItemStack ingredientItem = BuiltInRegistries.ITEM.get(ingredientId).getDefaultInstance();
            tooltip.append(helper.item(ingredientItem));
            first = false;
        }
    }

    private void addEffects(ITooltip tooltip, IPluginConfig config, PokeSnackBlockEntity entity) {
        if (!config.get(EFFECTS)) {
            return;
        }

        BaitEffectsComponent effects = entity.getBaitEffectsComponent();
        List<Component> effectLines = generateEffectTooltip(effects);

        if (!effectLines.isEmpty()) {
            effectLines.forEach(tooltip::add);
        }
    }

    private List<Component> generateEffectTooltip(BaitEffectsComponent effects) {
        ItemStack dummySnack = new ItemStack(CobblemonItems.POKE_SNACK);
        dummySnack.set(CobblemonItemComponents.BAIT_EFFECTS, effects);
        return SeasoningTooltipHelperKt.generateAdditionalBaitEffectTooltip(dummySnack);
    }
}