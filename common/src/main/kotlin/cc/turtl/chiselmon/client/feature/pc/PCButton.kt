package cc.turtl.chiselmon.client.feature.pc

import cc.turtl.chiselmon.client.util.renderCenteredText
import cc.turtl.turtlshell.api.core.format.ColorLib
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class PCButton private constructor(builder: Builder) : Button(
    builder.x, builder.y, builder.width, builder.height,
    builder.text ?: Component.empty(),
    builder.onPress,
    DEFAULT_NARRATION
), CobblemonRenderable {

    private val sprite: ResourceLocation = builder.sprite
    private val textureWidth: Int = builder.textureWidth
    private val textureHeight: Int = builder.textureHeight
    private val buttonHeight: Int = builder.height
    private val renderText: Boolean = builder.text != null
    private val textColor: Int = builder.textColor
    private val textMargin: Int = builder.textMargin
    private val activeTooltip: Tooltip? = builder.activeTooltip
    private val inactiveTooltip: Tooltip? = builder.inactiveTooltip
    private var toggled: Boolean = builder.initialActive

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        updateTooltip()

        val textureYOffset = if (toggled) {
            if (isHovered) 0 else buttonHeight
        } else {
            if (isHovered) buttonHeight else 0
        }

        context.blit(sprite, x, y, 0F, textureYOffset.toFloat(), width, height, textureWidth, textureHeight)

        if (renderText) {
            renderCenteredText(context, message, textColor, x + width / 2, y + height / 2, width - textMargin)
        }
    }

    override fun playDownSound(soundManager: SoundManager) {
        soundManager.play(SimpleSoundInstance.forUI(CobblemonSounds.PC_CLICK, 1.0f))
    }

    private fun updateTooltip() {
        if (activeTooltip != null && inactiveTooltip != null) {
            tooltip = if (toggled) activeTooltip else inactiveTooltip
        } else {
            inactiveTooltip?.let { tooltip = it }
        }
    }

    fun setActive(toggled: Boolean) {
        this.toggled = toggled
    }

    fun toggleActive() {
        toggled = !toggled
    }

    class Builder(
        val x: Int,
        val y: Int,
        val sprite: ResourceLocation,
        val textureWidth: Int,
        val textureHeight: Int,
        val onPress: OnPress
    ) {
        var width: Int = textureWidth
        var height: Int = textureHeight / 2
        var text: Component? = null
        var textColor: Int = ColorLib.WHITE.rgb
        var textMargin: Int = 5
        var activeTooltip: Tooltip? = null
        var inactiveTooltip: Tooltip? = null
        var initialActive: Boolean = false

        fun dimensions(width: Int, height: Int) = apply { this.width = width; this.height = height }
        fun text(text: Component) = apply { this.text = text }
        fun textStyle(color: Int, margin: Int) = apply { textColor = color; textMargin = margin }
        fun tooltip(tooltip: Tooltip) = apply { inactiveTooltip = tooltip }
        fun tooltips(active: Tooltip, inactive: Tooltip) = apply { activeTooltip = active; inactiveTooltip = inactive }
        fun active(active: Boolean) = apply { initialActive = active }
        fun build() = PCButton(this)
    }
}