package cc.turtl.cobbleaid.feature.pc;

import com.cobblemon.mod.common.client.gui.PokemonGuiUtilsKt;
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState;
import com.cobblemon.mod.common.entity.PoseType;
import com.cobblemon.mod.common.util.math.QuaternionUtilsKt;

import cc.turtl.cobbleaid.integration.neodaycare.NeoDaycareDummyPokemon;
import cc.turtl.cobbleaid.util.ColorUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import org.joml.Vector3f;

public final class PcEggRenderer {

    private PcEggRenderer() {
    }

    private static final float MODEL_SCALE_FACTOR = 5F;

    // Hatch progress bar constants
    private static final int SLOT_WIDTH = 25;
    private static final int BAR_HEIGHT = 2;
    private static final int BAR_Y_OFFSET = 23;
    private static final int BAR_COLOR_BACKGROUND = ColorUtil.alphaColor(ColorUtil.DARK_GRAY,1);
    private static final int BAR_COLOR = ColorUtil.alphaColor(ColorUtil.GREEN,1);;

    public static void renderEggPreviewElements(GuiGraphics context, @NotNull NeoDaycareDummyPokemon dummyPokemon,
            int posX, int posY) {

        renderProgressBar(context, dummyPokemon.getHatchCompletion(), posX, posY);

        final var matrices = context.pose();
        matrices.pushPose();

        matrices.translate(posX + 20F, posY + 8F, 150F);
        matrices.scale(MODEL_SCALE_FACTOR, MODEL_SCALE_FACTOR, 1F);

        // Draws the original egg in the bottom right corner
        PokemonGuiUtilsKt.drawProfilePokemon(
                dummyPokemon.getOriginalPokemon().asRenderablePokemon(),
                matrices,
                QuaternionUtilsKt.fromEulerXYZDegrees(new Quaternionf(), new Vector3f(13F, 35F, 0F)),
                PoseType.PROFILE,
                new FloatingState(),
                0F,
                2.0F,
                true,
                false,
                1F, 1F, 1F, 1F,
                0F,
                0F);

        matrices.popPose();
    }

    private static void renderProgressBar(GuiGraphics context, float hatchCompletion, int posX, int posY) {

        // Clamp the progress to ensure it's between 0.0 and 1.0
        float clampedProgress = Mth.clamp(hatchCompletion, 0.0F, 1.0F);

        // Calculate the width of the filled bar
        int fillWidth = (int) (SLOT_WIDTH * clampedProgress);

        // Draw the background bar (full width of the slot)
        context.fill(
                posX, // Start X
                posY + BAR_Y_OFFSET, // Start Y (23)
                posX + SLOT_WIDTH, // End X (25)
                posY + BAR_Y_OFFSET + BAR_HEIGHT, // End Y (25)
                BAR_COLOR_BACKGROUND);

        // Draw the filled progress bar
        if (fillWidth > 0) {
            context.fill(
                    posX, // Start X
                    posY + BAR_Y_OFFSET, // Start Y (23)
                    posX + fillWidth, // End X (up to fillWidth)
                    posY + BAR_Y_OFFSET + BAR_HEIGHT, // End Y (25)
                    BAR_COLOR);
        }
    }
}