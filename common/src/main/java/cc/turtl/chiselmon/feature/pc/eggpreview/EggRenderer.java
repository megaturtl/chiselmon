package cc.turtl.chiselmon.feature.pc.eggpreview;

import com.cobblemon.mod.common.client.gui.PokemonGuiUtilsKt;
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState;
import com.cobblemon.mod.common.entity.PoseType;
import com.cobblemon.mod.common.util.math.QuaternionUtilsKt;

import cc.turtl.chiselmon.util.format.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import org.joml.Vector3f;

public final class EggRenderer {

    private EggRenderer() {}

    private static final float EGG_SCALE = 5F;

    private static final int BAR_HEIGHT = 2;
    private static final int BAR_WIDTH = 25;
    private static final int BAR_Y_OFFSET = 23;

    private static final int BAR_BG_COLOR = ColorUtils.argb(ColorUtils.DARK_GRAY, 1f);
    private static final int BAR_FILL_COLOR = ColorUtils.argb(ColorUtils.GREEN, 1f);

    public static void renderStorageSlot(GuiGraphics context, @NotNull EggDummy eggDummy, int posX, int posY) {

        renderProgressBar(context, eggDummy.getHatchCompletion(), posX, posY);
        renderEggModel(context, eggDummy, posX, posY);
    }

    private static void renderEggModel(GuiGraphics context, @NotNull EggDummy eggDummy, int posX, int posY) {
        context.pose().pushPose();
        context.pose().translate(posX + 21F, posY + 8F, 150F);
        context.pose().scale(EGG_SCALE, EGG_SCALE, 1F);

        PokemonGuiUtilsKt.drawProfilePokemon(
                eggDummy.getOriginalEgg().asRenderablePokemon(),
                context.pose(),
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

        context.pose().popPose();
    }

    private static void renderProgressBar(GuiGraphics context, float progress, int posX, int posY) {
        int xStart = posX;
        int yStart = posY + BAR_Y_OFFSET;

        // Background
        context.fill(xStart, yStart, xStart + BAR_WIDTH, yStart + BAR_HEIGHT, BAR_BG_COLOR);

        // Progress Fill
        int fillWidth = (int) (BAR_WIDTH * Mth.clamp(progress, 0.0F, 1.0F));
        if (fillWidth > 0) {
            context.fill(xStart, yStart, xStart + fillWidth, yStart + BAR_HEIGHT, BAR_FILL_COLOR);
        }
    }
}