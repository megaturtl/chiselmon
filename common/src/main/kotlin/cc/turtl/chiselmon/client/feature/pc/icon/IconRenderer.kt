package cc.turtl.chiselmon.client.feature.pc.icon

import cc.turtl.chiselmon.client.config.category.PCConfig.IconConfig
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.client.gui.GuiGraphics

object IconRenderer {
    // sizes in pixels
    private const val SRC_SIZE = 9
    private const val DEST_SIZE = 5
    private const val MAX_ROWS = 3
    private const val RENDER_Z = 100.0f
    private const val COLUMN_SPACE = 18

    @JvmStatic
    fun renderIcons(context: GuiGraphics, config: IconConfig, pokemon: Pokemon, x: Int, y: Int) {
        val entries = IconRegistry.entries

        if (entries.isEmpty()) return

        context.pose().pushPose()
        context.pose().translate((x + 1).toFloat(), (y + 6).toFloat(), RENDER_Z)

        var count = 0
        for (entry in entries) {
            if (entry.shouldDisplay(config, pokemon)) {
                val col = count / MAX_ROWS
                val row = count % MAX_ROWS

                val renderX = col * COLUMN_SPACE
                val renderY = row * DEST_SIZE

                renderIcon(context, entry, renderX, renderY)
                count++
            }
        }

        context.pose().popPose()
    }

    private fun renderIcon(context: GuiGraphics, entry: IconEntry, x: Int, y: Int) {
        context.blit(
            entry.resource,
            x, y,
            DEST_SIZE, DEST_SIZE,
            0f, 0f,
            SRC_SIZE, SRC_SIZE,
            SRC_SIZE, SRC_SIZE
        )
    }
}
