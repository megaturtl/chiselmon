package cc.turtl.chiselmon.client.feature.battle

import cc.turtl.chiselmon.client.config.ChiselmonConfig
import cc.turtl.chiselmon.core.api.calc.computeMatchups
import cc.turtl.turtlshell.api.core.format.ColorLib
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleActionSelection
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style

object MoveTooltipRenderer {
    private const val TOOLTIP_MAX_WIDTH = 200

    @JvmStatic
    fun render(
        context: GuiGraphics,
        actionSelection: BattleActionSelection?,
        mouseX: Int,
        mouseY: Int,
    ) {
        if (ChiselmonConfig.general.modDisabled || !ChiselmonConfig.general.moveDetail) return

        val moveSelection = actionSelection as? BattleMoveSelection ?: return
        val tile =
            moveSelection.moveTiles.firstOrNull {
                it.isHovered(mouseX.toDouble(), mouseY.toDouble())
            } ?: return

        val moveTemplate = tile.moveTemplate
        val lines = mutableListOf<Component>()
        lines += createHeader(tile)
        lines += createDescription(moveTemplate.description)
        lines += createEffectivenessLines(tile)

        context.renderComponentTooltip(Minecraft.getInstance().font, lines, mouseX, mouseY)
    }

    private fun createHeader(tile: BattleMoveSelection.MoveTile): MutableComponent {
        val moveTemplate = tile.moveTemplate
        val power =
            moveTemplate.power
                .takeUnless { it == 0.0 }
                ?.toInt()
                ?.toString() ?: "—"
        val accuracy =
            moveTemplate.accuracy
                .takeUnless { it == -1.0 }
                ?.toInt()
                ?.let { "$it%" } ?: "—"

        return Component
            .empty()
            .append(moveTemplate.displayName.copy().withColor(tile.elementalType.hue))
            .append(Component.literal(" » ").withColor(ColorLib.LIGHT_GRAY.rgb))
            .append(Component.literal("⚡").withStyle { it.withBold(true).withColor(ColorLib.RED.rgb) })
            .append(Component.literal(" $power").withStyle { it.withBold(false) })
            .append(Component.literal(" • ").withColor(ColorLib.DARK_GRAY.rgb))
            .append(Component.literal("🎯").withStyle { it.withBold(true).withColor(ColorLib.GREEN.rgb) })
            .append(Component.literal(" $accuracy").withStyle { it.withBold(false) })
            .also { header ->
                moveTemplate.effectChances.firstOrNull()?.let { chance ->
                    header.append(Component.literal(" • ").withColor(ColorLib.DARK_GRAY.rgb))
                    header.append(Component.literal("⚗").withStyle { it.withBold(false).withColor(ColorLib.YELLOW.rgb) })
                    header.append(Component.literal(" ${chance.toInt()}%").withStyle { it.withBold(false) })
                }
                if (moveTemplate.priority != 0) {
                    header.append(Component.literal(" • ").withColor(ColorLib.DARK_GRAY.rgb))
                    header.append(Component.literal("⌛").withStyle { it.withBold(false).withColor(ColorLib.AQUA.rgb) })
                    header.append(
                        Component
                            .literal(
                                " ${if (moveTemplate.priority > 0) "+" else ""}${moveTemplate.priority}",
                            ).withStyle { it.withBold(false) },
                    )
                }
                if (moveTemplate.critRatio != 1.0) {
                    header.append(Component.literal(" • ").withColor(ColorLib.DARK_GRAY.rgb))
                    header.append(Component.literal("💥").withStyle { it.withBold(true).withColor(ColorLib.PURPLE.rgb) })
                    header.append(Component.literal(" ${moveTemplate.critRatio}x").withStyle { it.withBold(false) })
                }
            }
    }

    private fun createDescription(description: Component): List<MutableComponent> =
        Minecraft
            .getInstance()
            .font.splitter
            .splitLines(description, TOOLTIP_MAX_WIDTH, Style.EMPTY)
            .map { Component.literal(it.string).withColor(ColorLib.LIGHT_GRAY.rgb) }

    private fun createEffectivenessLines(tile: BattleMoveSelection.MoveTile): List<MutableComponent> {
        val targets =
            tile.targetList
                ?: tile.moveSelection.request.activePokemon
                    .getMultiTargetList(tile.move.target)
                ?: return emptyList()

        val lines =
            targets
                .mapNotNull { target ->
                    val battlePokemon = (target as? ActiveClientBattlePokemon)?.battlePokemon ?: return@mapNotNull null
                    val defender = battlePokemon.properties.create()
                    val multiplier = computeMatchups(defender.types).multiplierMap[tile.elementalType] ?: 1f
                    multiplier.takeUnless { it == 1f }?.let {
                        createEffectivenessLine(defender.species.name, it)
                    }
                }.toMutableList()

        return lines.takeIf { it.isNotEmpty() }?.apply { add(0, Component.empty()) } ?: emptyList()
    }

    private fun createEffectivenessLine(
        speciesName: String,
        multiplier: Float,
    ): MutableComponent {
        val color =
            when ((multiplier * 100).toInt()) {
                400 -> ColorLib.PINK.rgb
                200 -> ColorLib.GREEN.rgb
                50 -> ColorLib.ORANGE.rgb
                25 -> ColorLib.YELLOW.rgb
                0 -> ColorLib.RED.rgb
                else -> ColorLib.WHITE.rgb
            }
        val formattedMultiplier = if (multiplier % 1f == 0f) multiplier.toInt() else multiplier

        return Component
            .empty()
            .append(Component.literal("» ").withColor(ColorLib.DARK_GRAY.rgb))
            .append(Component.literal("Deals ${formattedMultiplier}x to ").withColor(color))
            .append(Component.literal(speciesName).withColor(color))
    }
}
