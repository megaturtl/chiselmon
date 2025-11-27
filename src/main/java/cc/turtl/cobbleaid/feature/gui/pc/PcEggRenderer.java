package cc.turtl.cobbleaid.feature.gui.pc;

import com.cobblemon.mod.common.client.gui.PokemonGuiUtilsKt;
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState;
import com.cobblemon.mod.common.entity.PoseType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.math.QuaternionUtilsKt;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import cc.turtl.cobbleaid.api.neodaycare.NeoDaycareEggData;

import org.joml.Quaternionf;

import org.joml.Vector3f;

public final class PcEggRenderer {

    private PcEggRenderer() {
    }

    private static final float MODEL_SCALE_FACTOR = 3.5F;
    
    // Hatch progress bar constants
    private static final int SLOT_WIDTH = 25;
    private static final int BAR_HEIGHT = 1;
    private static final int BAR_Y_OFFSET = 23;
    private static final int BAR_COLOR_BACKGROUND = 0xFF474747;
    private static final int BAR_COLOR = 0xFF009432;

    public static void renderEggPreviewElements(GuiGraphics context, Pokemon pokemon, int posX, int posY, float partialTicks) {
        if (pokemon == null || !NeoDaycareEggData.isNeoDaycareEgg(pokemon)) {
            return;
        }

        NeoDaycareEggData eggData = NeoDaycareEggData.createNeoDaycareEggData(pokemon);
        Pokemon eggDummyPokemon = eggData.createDummyPokemon();

        renderProgressBar(context, eggData, posX, posY);

        if (eggDummyPokemon == null) {
            return;
        }

        final var matrices = context.pose();
        matrices.pushPose();

        matrices.translate(posX + 20.0, posY + 8.0, 0.0);
        matrices.scale(MODEL_SCALE_FACTOR, MODEL_SCALE_FACTOR, 1F);

        // Draws the actual pokemon in the bottom right corner of the egg
        PokemonGuiUtilsKt.drawProfilePokemon(
                eggDummyPokemon.asRenderablePokemon(), // 1. renderablePokemon
                matrices, // 2. matrixStack
                QuaternionUtilsKt.fromEulerXYZDegrees(new Quaternionf(), new Vector3f(13F, 35F, 0F)), // 3. rotation
                PoseType.PROFILE, // 4. poseType (default)
                new FloatingState(), // 5. state (fixed)
                0F, // 6. partialTicks
                2.0F, // 7. scale (fixed)
                true, // 8. applyProfileTransform (default)
                false, // 9. applyBaseScale (default)
                1F, 1F, 1F, 1F, // 10. r, g, b, a (defaults)
                0F, // 11. headYaw (default)
                0F // 12. headPitch (default)
        );

        matrices.popPose();
    }

    private static void renderProgressBar(GuiGraphics context, NeoDaycareEggData eggData, int posX, int posY) {
        // Get the progress decimal (0.0 to 1.0)
        float progress = eggData.getHatchCompletion();

        // Clamp the progress to ensure it's between 0.0 and 1.0
        float clampedProgress = Mth.clamp(progress, 0.0F, 1.0F);

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