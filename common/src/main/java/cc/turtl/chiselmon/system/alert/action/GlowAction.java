package cc.turtl.chiselmon.system.alert.action;

import cc.turtl.chiselmon.api.duck.DuckGlowableEntity;
import cc.turtl.chiselmon.system.alert.AlertContext;
import cc.turtl.chiselmon.util.format.ColorUtils;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.network.chat.Component;

public class GlowAction implements AlertAction {
    private static void addGlow(PokemonEntity pe, int rgb) {
        if (!(pe instanceof DuckGlowableEntity glowable)) {
            return;
        }
        glowable.chiselmon$setClientGlowColor(rgb);
        glowable.chiselmon$setClientGlowing(true);
    }

    private static void removeGlow(PokemonEntity pe) {
        if (!(pe instanceof DuckGlowableEntity glowable)) {
            return;
        }
        glowable.chiselmon$setClientGlowing(false);
    }

    private static void highlightNickname(PokemonEntity pe, int rgb) {
        char mcColor = ColorUtils.legacy(rgb).getChar();
        String speciesName = pe.getPokemon().getSpecies().getName();
        String formattedName = "§" + mcColor + speciesName;
        pe.getPokemon().setNickname(Component.literal(formattedName));
    }

    @Override
    public void execute(AlertContext ctx) {
        if (!ctx.shouldHighlight()) {
            removeGlow(ctx.entity());
        } else {
            int color = ctx.filter().color().getRGB();
            addGlow(ctx.entity(), color);
            highlightNickname(ctx.entity(), color);
        }
    }
}