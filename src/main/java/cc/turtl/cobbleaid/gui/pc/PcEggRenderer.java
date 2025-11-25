package cc.turtl.cobbleaid.gui.pc;

import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.gui.GuiGraphics;

import cc.turtl.cobbleaid.api.neodaycare.NeoDaycareEggData; // Ensure this import is correct

import com.cobblemon.mod.common.client.gui.PokemonGuiUtilsKt;
import com.cobblemon.mod.common.util.math.QuaternionUtilsKt;
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState;
import com.cobblemon.mod.common.entity.PoseType;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class PcEggRenderer {

    private PcEggRenderer() {
    }

    // A small scale for the corner model
    private static final float MODEL_SCALE_FACTOR = 4.0F;

    public static void renderEggPreview(GuiGraphics context, Pokemon pokemon, int posX, int posY, float partialTicks) {
        if (pokemon == null || !NeoDaycareEggData.isNeoDaycareEgg(pokemon)) {
            return;
        }

        // 1. Get the representation (the Pokémon that will hatch)
        Pokemon representation = NeoDaycareEggData.createNeoDaycareEggData(pokemon).createPokemonRepresentation();

        if (representation == null) {
            return;
        }

        final var matrices = context.pose();

        matrices.pushPose();
        matrices.translate(posX + 18.0, posY + 8.0, 0.0);

        // Apply scaling for the small corner icon
        matrices.scale(MODEL_SCALE_FACTOR, MODEL_SCALE_FACTOR, 1F);

        PokemonGuiUtilsKt.drawProfilePokemon(
                representation.asRenderablePokemon(), // 1. renderablePokemon
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
}