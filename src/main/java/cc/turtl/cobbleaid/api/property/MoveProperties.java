package cc.turtl.cobbleaid.api.property;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.moves.Learnset;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;

public class MoveProperties {

    private static final Set<String> SELF_DAMAGE_MOVES = Set.of(
            // Recoil damage moves
            "doubleedge", "bravebird", "takedown", "submission",
            "volttackle", "flareblitz", "woodhammer", "headsmash",
            "headcharge", "wildcharge", "jumpkick", "highjumpkick",
            "lightofruin",
            // Direct self-damage moves
            "struggle", "mindblown", "steelbeam", "chloroblast",
            // HP cost moves
            "curse",
            // Fainting moves
            "selfdestruct", "explosion", "finalgambit",
            "healingwish", "lunardance", "memento", "mistyexplosion");

    public static final CustomProperty<MoveTemplate> IS_SELF_DAMAGING = move -> SELF_DAMAGE_MOVES
            .contains(move.getName().toLowerCase());

    public static Set<MoveTemplate> getPokemonLearnsetUpToLevel(Pokemon pokemon) {
        FormData form = pokemon.getForm();
        Learnset learnset = form.getMoves();
        return learnset.getLevelUpMovesUpTo(pokemon.getLevel());
    }

    public static List<MoveTemplate> filterMoves(Pokemon pokemon, CustomProperty<MoveTemplate> filter) {
        Set<MoveTemplate> learnset = getPokemonLearnsetUpToLevel(pokemon);

        return learnset.stream()
                .filter(filter::matches)
                .collect(Collectors.toList());
    }

    public static List<MoveTemplate> getSelfDamagingMoves(Pokemon pokemon) {
        return filterMoves(pokemon, IS_SELF_DAMAGING);
    }
}
