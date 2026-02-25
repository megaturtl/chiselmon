package cc.turtl.chiselmon.feature.pc.eggspy;

import cc.turtl.chiselmon.util.format.ColorUtils;
import com.cobblemon.mod.common.client.gui.PokemonGuiUtilsKt;
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState;
import com.cobblemon.mod.common.entity.PoseType;
import com.cobblemon.mod.common.util.math.QuaternionUtilsKt;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class EggRenderer {
    // Model rendering constants
    private static final float EGG_SCALE = 5F;
    private static final float EGG_X_OFFSET = 21F;
    private static final float EGG_Y_OFFSET = 8F;
    private static final float EGG_Z_OFFSET = 150F;
    // Model rotation
    private static final Vector3f EGG_ROTATION = new Vector3f(13F, 35F, 0F);
    // Model rendering parameters
    private static final float PROFILE_OFFSET = 0F;
    private static final float PROFILE_SCALE = 2.0F;
    // Progress bar constants
    private static final int BAR_HEIGHT = 2;
    private static final int BAR_WIDTH = 25;
    private static final int BAR_Y_OFFSET = 23;
    private static final int BAR_BG_COLOR = ColorUtils.argb(ColorUtils.DARK_GRAY.getRGB(), 1f);
    private static final int BAR_FILL_COLOR = ColorUtils.argb(ColorUtils.GREEN.getRGB(), 1f);

    private EggRenderer() {
    }

    public static void renderStorageSlot(GuiGraphics context, @NotNull EggDummy eggDummy, int posX, int posY) {
        renderProgressBarStorageSlot(context, eggDummy.getHatchPercentage(), posX, posY);
        renderEggModelStorageSlot(context, eggDummy, posX, posY);
    }

    /**
     * Renders a small model of the egg in the bottom right of the storage slot
     */
    private static void renderEggModelStorageSlot(GuiGraphics context, @NotNull EggDummy eggDummy, int posX, int posY) {
        context.pose().pushPose();
        context.pose().translate(posX + EGG_X_OFFSET, posY + EGG_Y_OFFSET, EGG_Z_OFFSET);
        context.pose().scale(EGG_SCALE, EGG_SCALE, 1F);

        Quaternionf rotation = QuaternionUtilsKt.fromEulerXYZDegrees(new Quaternionf(), EGG_ROTATION);

        PokemonGuiUtilsKt.drawProfilePokemon(
                eggDummy.getOriginalRenderablePokemon(),
                context.pose(),
                rotation,
                PoseType.PROFILE,
                new FloatingState(),
                PROFILE_OFFSET,
                PROFILE_SCALE,
                true,  // portraitScale
                false, // useFixedPivot
                1F, 1F, 1F, 1F,  // RGBA
                0F,    // partialTicks (unused here)
                0F     // yaw (unused here)
        );

        context.pose().popPose();
    }

    private static void renderProgressBarStorageSlot(GuiGraphics context, int hatchPercentage, int posX, int posY) {
        int xStart = posX;
        int yStart = posY + BAR_Y_OFFSET;

        // Background
        context.fill(xStart, yStart, xStart + BAR_WIDTH, yStart + BAR_HEIGHT, BAR_BG_COLOR);

        // Progress fill
        int fillWidth = (int) (BAR_WIDTH * Mth.clamp(hatchPercentage, 0, 100));
        if (fillWidth > 0) {
            context.fill(xStart, yStart, xStart + fillWidth, yStart + BAR_HEIGHT, BAR_FILL_COLOR);
        }
    }
}