package cc.turtl.chiselmon.feature.spawnalert.response.handler;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.api.duck.GlowableEntityDuck;
import cc.turtl.chiselmon.feature.spawnalert.AlertLevel;
import cc.turtl.chiselmon.feature.spawnalert.SpawnAlertConfig;
import cc.turtl.chiselmon.feature.spawnalert.response.AlertResponse;
import cc.turtl.chiselmon.util.ColorUtil;
import net.minecraft.network.chat.Component;

public class AlertGlowHandler {
    private static final int DESPAWN_TICKS = 600; // min ticks to become despawnable

    public static void handle(AlertResponse response, SpawnAlertConfig config) {
        PokemonEntity pe = response.pe();
        AlertLevel level = response.glowLevel();

        int color;

        // despawn track color takes priority when enabled (unless leg or shiny)
        if (config.despawnTrackEnabled && level != AlertLevel.LEGENDARY && level != AlertLevel.SHINY) {
            color = (pe.getTicksLived() >= DESPAWN_TICKS) ? ColorUtil.RED : ColorUtil.LIME;
        } else if (level != AlertLevel.NONE && level.shouldGlow(config)) {
            color = level.getColor(config);
        } else {
            // If shouldn't glow, remove
            removeEffects(pe);
            return;
        }

        addGlow(pe, color);
        highlightNickname(pe, color);
    }

    public static void removeEffects(PokemonEntity pe) {
        if (!(pe instanceof GlowableEntityDuck glowable)) {
            return;
        }
        glowable.chiselmon$setClientGlowing(false);
        pe.getPokemon().setNickname(null);
    }

    public static void addGlow(PokemonEntity pe, int color) {
        if (!(pe instanceof GlowableEntityDuck glowable)) {
            return;
        }
        glowable.chiselmon$setClientGlowColor(color);
        glowable.chiselmon$setClientGlowing(true);
    }

    public static void highlightNickname(PokemonEntity pe, int color) {
        char mcColor = ColorUtil.getClosestMcColor(color);
        String speciesName = pe.getPokemon().getSpecies().getName();
        String formattedName = "§" + mcColor + speciesName;
        pe.getPokemon().setNickname(Component.literal(formattedName));
    }
}
