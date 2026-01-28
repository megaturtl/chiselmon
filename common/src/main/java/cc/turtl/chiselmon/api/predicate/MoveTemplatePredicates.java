package cc.turtl.chiselmon.api.predicate;

import com.cobblemon.mod.common.api.moves.MoveTemplate;

import java.util.Set;
import java.util.function.Predicate;

public class MoveTemplatePredicates {

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
            "healingwish", "lunardance", "memento", "mistyexplosion",
            // Confusion inducing moves
            "outrage", "petaldance", "thrash");

    public static final Predicate<MoveTemplate> IS_SELF_DAMAGING = move -> SELF_DAMAGE_MOVES
            .contains(move.getName().toLowerCase());
}
