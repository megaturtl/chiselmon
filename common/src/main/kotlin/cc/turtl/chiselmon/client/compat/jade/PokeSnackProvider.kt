package cc.turtl.chiselmon.client.compat.jade

import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.chiselmon.core.util.modResource
import cc.turtl.chiselmon.util.format.ColorUtils
import cc.turtl.chiselmon.util.format.ComponentUtils
import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.block.PokeSnackBlock
import com.cobblemon.mod.common.block.entity.PokeSnackBlockEntity
import com.cobblemon.mod.common.client.tooltips.generateAdditionalBaitEffectTooltip
import com.cobblemon.mod.common.item.components.IngredientComponent
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.ui.IElementHelper

/**
 * Jade tooltip provider for PokeSnack blocks.
 * Displays bites remaining, ingredients, and effects based on config.
 */
object PokeSnackProvider : IBlockComponentProvider {

    val BITES: ResourceLocation = modResource("pokesnack_block.bites")
    val INGREDIENTS: ResourceLocation = modResource("pokesnack_block.ingredients")
    val EFFECTS: ResourceLocation = modResource("pokesnack_block.effects")

    private val UID: ResourceLocation = modResource("pokesnack_block")
    private const val MAX_BITES = 9

    override fun getUid(): ResourceLocation = UID

    override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
        if (ChiselmonConfig.general.modDisabled) return
        if (accessor.block !is PokeSnackBlock) return

        val state = accessor.blockState
        val entity = accessor.blockEntity as? PokeSnackBlockEntity ?: return

        if (config.get(BITES)) {
            val remaining = MAX_BITES - state.getValue(PokeSnackBlock.BITES)
            tooltip.add(
                ComponentUtils.labelled(
                    Component.translatable("chiselmon.ui.label.pokesnack_block.bites_remaining"),
                    remaining
                )
            )
        }

        if (config.get(INGREDIENTS)) {
            val ingredients = entity.ingredientComponent
            if (ingredients == null) {
                tooltip.add(
                    Component.translatable("chiselmon.ui.label.pokesnack_block.no_ingredients")
                        .withColor(ColorUtils.RED.rgb)
                )
            } else {
                addIngredientIcons(tooltip, ingredients)
            }
        }

        if (config.get(EFFECTS)) {
            val dummySnack = ItemStack(CobblemonItems.POKE_SNACK).also {
                it.set(CobblemonItemComponents.BAIT_EFFECTS, entity.baitEffectsComponent)
            }
            generateAdditionalBaitEffectTooltip(dummySnack)
                .forEach(tooltip::add)
        }
    }

    private fun addIngredientIcons(tooltip: ITooltip, ingredients: IngredientComponent) {
        val helper = IElementHelper.get()
        tooltip.add(helper.spacer(0, 0))
        ingredients.ingredientIds.forEachIndexed { index, id ->
            if (index > 0) tooltip.append(helper.spacer(1, 0))
            tooltip.append(helper.item(BuiltInRegistries.ITEM.get(id).defaultInstance))
        }
    }
}