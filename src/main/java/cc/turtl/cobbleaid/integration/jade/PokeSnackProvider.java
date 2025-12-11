package cc.turtl.cobbleaid.integration.jade;

import java.util.List;

import com.cobblemon.mod.common.CobblemonItemComponents;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.block.PokeSnackBlock;
import com.cobblemon.mod.common.block.entity.PokeSnackBlockEntity;
import com.cobblemon.mod.common.client.tooltips.SeasoningTooltipHelperKt;
import com.cobblemon.mod.common.item.components.BaitEffectsComponent;
import com.cobblemon.mod.common.item.components.IngredientComponent;

import cc.turtl.cobbleaid.api.format.FormatUtil;
import cc.turtl.cobbleaid.api.util.ColorUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;
import static cc.turtl.cobbleaid.util.MiscUtil.modResource;

public class PokeSnackProvider implements IBlockComponentProvider {
    private PokeSnackProvider() {
    }

    public static final PokeSnackProvider INSTANCE = new PokeSnackProvider();

    public static final String POKESNACK_BLOCK_PARENT_PATH = "pokesnack_block";
    public static final ResourceLocation POKESNACK_BLOCK_ID = modResource(
            POKESNACK_BLOCK_PARENT_PATH);
    public static final ResourceLocation POKESNACK_BLOCK_BITES_ID = modResource(
            POKESNACK_BLOCK_PARENT_PATH + ".bites_remaining");
    public static final ResourceLocation POKESNACK_BLOCK_INGREDIENTS_ID = modResource(
            POKESNACK_BLOCK_PARENT_PATH + ".ingredients");
    public static final ResourceLocation POKESNACK_BLOCK_EFFECTS_ID = modResource(
            POKESNACK_BLOCK_PARENT_PATH + ".effects");

    @Override
    public ResourceLocation getUid() {
        return POKESNACK_BLOCK_ID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlock() instanceof PokeSnackBlock)) {
            return;
        }

        IElementHelper helper = IElementHelper.get();
        BlockState state = accessor.getBlockState();
        PokeSnackBlockEntity entity = (PokeSnackBlockEntity) accessor.getBlockEntity();

        if (config.get(POKESNACK_BLOCK_BITES_ID)) {
            int bites = state.getValue(PokeSnackBlock.Companion.getBITES());
            int bitesRemaining = 9 - bites;
            tooltip.add(FormatUtil.labelledValue("Bites Remaining: ", bitesRemaining));
        }

        if (config.get(POKESNACK_BLOCK_INGREDIENTS_ID)) {
            IngredientComponent ingredientComponent = entity.getIngredientComponent();
            if (ingredientComponent == null) {
                tooltip.add(Component.literal("No Ingredients")
                        .withColor(ColorUtil.RED));
            } else {
                tooltip.add(helper.spacer(0, 0));
                boolean first = true;
                for (ResourceLocation ingredientPath : ingredientComponent.getIngredientIds()) {
                    ItemStack ingredientItem = BuiltInRegistries.ITEM.get(ingredientPath).getDefaultInstance();
                    if (!first)
                        tooltip.append(helper.spacer(1, 0));
                    tooltip.append(helper.item(ingredientItem));
                    first = false;
                }
            }
        }

        if (config.get(POKESNACK_BLOCK_EFFECTS_ID)) {
            ItemStack dummySnackItem = new ItemStack(CobblemonItems.POKE_SNACK);
            BaitEffectsComponent effectsComponent = entity.getBaitEffectsComponent();
            dummySnackItem.set(CobblemonItemComponents.BAIT_EFFECTS, effectsComponent);
            List<Component> combinedTooltipLines = SeasoningTooltipHelperKt
                    .generateAdditionalBaitEffectTooltip(dummySnackItem);
            if (!combinedTooltipLines.isEmpty()) {
                combinedTooltipLines.forEach(tooltip::add);
            }
        }
    }
}