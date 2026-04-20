package cc.turtl.chiselmon.core.api.predicate

import com.cobblemon.mod.common.api.moves.MoveTemplate
import java.util.function.Predicate

private val SELF_DAMAGE_MOVES = setOf(
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
    "outrage", "petaldance", "thrash"
)

@JvmField
val IS_SELF_DAMAGING: Predicate<MoveTemplate> = Predicate {
    SELF_DAMAGE_MOVES.contains(it.name.lowercase())
}