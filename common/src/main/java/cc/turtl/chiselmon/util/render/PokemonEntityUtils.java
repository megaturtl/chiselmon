package cc.turtl.chiselmon.util.render;

import cc.turtl.chiselmon.api.duck.DuckGlowableEntity;
import cc.turtl.chiselmon.util.format.ColorUtils;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.network.chat.Component;

public class PokemonEntityUtils {
    public static void addGlow(PokemonEntity pe, int rgb) {
        if (!(pe instanceof DuckGlowableEntity glowable)) {
            return;
        }
        glowable.chiselmon$setClientGlowColor(rgb);
        glowable.chiselmon$setClientGlowing(true);
    }

    public static void removeGlow(PokemonEntity pe) {
        if (!(pe instanceof DuckGlowableEntity glowable)) {
            return;
        }
        glowable.chiselmon$setClientGlowing(false);
    }

    public static void highlightNickname(PokemonEntity pe, int rgb) {
        char mcColor = ColorUtils.legacy(rgb).getChar();
        String speciesName = pe.getPokemon().getSpecies().getName();
        String formattedName = "§" + mcColor + speciesName;
        pe.getPokemon().setNickname(Component.literal(formattedName));
    }

    public static void resetNickname(PokemonEntity pe) {
        Pokemon pokemon = pe.getPokemon();
        pokemon.setNickname(pokemon.getSpecies().getTranslatedName());
    }
}
