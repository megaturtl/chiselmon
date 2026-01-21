package cc.turtl.chiselmon.feature.spawnalert;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import cc.turtl.chiselmon.api.entity.ClientGlowEntity;
import cc.turtl.chiselmon.util.ColorUtil;

public class DespawnTrack {
    private static final int DESPAWN_TICKS = 600;

    public static void despawnHighlight(PokemonEntity pe) {
        if (!(pe instanceof ClientGlowEntity glowable)) {
            return;
        }

        int color = (pe.getTicksLived() >= DESPAWN_TICKS)
                ? ColorUtil.RED
                : ColorUtil.LIME;

        glowable.chiselmon$setClientGlowColor(color);
        glowable.chiselmon$setClientGlowing(true);
    }
}