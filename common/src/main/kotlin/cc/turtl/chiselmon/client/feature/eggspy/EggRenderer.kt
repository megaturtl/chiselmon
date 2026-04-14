package cc.turtl.chiselmon.client.feature.eggspy

import cc.turtl.chiselmon.util.format.ColorUtils
import cc.turtl.turtlshell.api.core.util.toArgb
import com.cobblemon.mod.common.client.gui.drawProfilePokemon
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.util.math.fromEulerXYZDegrees
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.util.Mth
import org.joml.Quaternionf
import org.joml.Vector3f

object EggRenderer {
    // Model positioning
    private const val EGG_SCALE = 5f
    private const val EGG_X_OFFSET = 21f
    private const val EGG_Y_OFFSET = 8f
    private const val EGG_Z_OFFSET = 150f
    private val EGG_ROTATION = Vector3f(13f, 35f, 0f)

    // Profile render parameters
    private const val PROFILE_OFFSET = 0f
    private const val PROFILE_SCALE = 2.0f

    // Progress bar
    private const val BAR_HEIGHT = 2
    private const val BAR_WIDTH = 25
    private const val BAR_Y_OFFSET = 23
    private val BAR_BG_COLOR = toArgb(ColorUtils.DARK_GRAY.rgb, 1f)
    private val BAR_FILL_COLOR = toArgb(ColorUtils.GREEN.rgb, 1f)

    @JvmStatic
    fun renderStorageSlot(context: GuiGraphics, eggDummy: EggDummy, posX: Int, posY: Int) {
        renderProgressBar(context, eggDummy.hatchPercentage, posX, posY)
        renderEggModel(context, eggDummy, posX, posY)
    }

    private fun renderEggModel(context: GuiGraphics, eggDummy: EggDummy, posX: Int, posY: Int) {
        context.pose().pushPose()
        context.pose().translate(posX + EGG_X_OFFSET, posY + EGG_Y_OFFSET, EGG_Z_OFFSET)
        context.pose().scale(EGG_SCALE, EGG_SCALE, 1f)

        val rotation = Quaternionf().fromEulerXYZDegrees(EGG_ROTATION)

        drawProfilePokemon(
            eggDummy.originalRenderablePokemon,
            context.pose(),
            rotation,
            PoseType.PROFILE,
            FloatingState(),
            PROFILE_OFFSET,
            PROFILE_SCALE,
        )

        context.pose().popPose()
    }

    private fun renderProgressBar(context: GuiGraphics, hatchPercentage: Int, posX: Int, posY: Int) {
        val yStart = posY + BAR_Y_OFFSET

        context.fill(posX, yStart, posX + BAR_WIDTH, yStart + BAR_HEIGHT, BAR_BG_COLOR)

        val fillWidth = (BAR_WIDTH * Mth.clamp(hatchPercentage, 0, 100)) / 100
        if (fillWidth > 0) {
            context.fill(posX, yStart, posX + fillWidth, yStart + BAR_HEIGHT, BAR_FILL_COLOR)
        }
    }
}