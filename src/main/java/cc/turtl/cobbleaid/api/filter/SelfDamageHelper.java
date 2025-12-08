package cc.turtl.cobbleaid.api.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.moves.Learnset;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;

public class SelfDamageHelper {
    public static boolean isSelfDamagingMove(MoveTemplate move) {
        // List of known self-damaging moves by their Showdown ID
        Set<String> selfDamageMoves = new HashSet<>();
        
        // Recoil damage moves
        selfDamageMoves.add("doubleedge");
        selfDamageMoves.add("bravebird");
        selfDamageMoves.add("takedown");
        selfDamageMoves.add("submission");
        selfDamageMoves.add("volttackle");
        selfDamageMoves.add("flareblitz");
        selfDamageMoves.add("woodhammer");
        selfDamageMoves.add("headsmash");
        selfDamageMoves.add("headcharge");
        selfDamageMoves.add("wildcharge");
        
        // Direct self-damage moves
        selfDamageMoves.add("struggle");
        selfDamageMoves.add("mindblown");
        selfDamageMoves.add("steelbeam");
        selfDamageMoves.add("chloroblast");
        
        // HP cost moves
        selfDamageMoves.add("bellydrum");
        selfDamageMoves.add("curse");
        selfDamageMoves.add("substitute");
        
        // Fainting moves
        selfDamageMoves.add("selfdestruct");
        selfDamageMoves.add("explosion");
        selfDamageMoves.add("finalgambit");
        selfDamageMoves.add("healingwish");
        selfDamageMoves.add("lunardance");
        selfDamageMoves.add("memento");
        selfDamageMoves.add("mistyexplosion");
        
        return selfDamageMoves.contains(move.getName().toLowerCase());
    }

    public static Set<MoveTemplate> getPokemonLearnsetUpToLevel(Pokemon pokemon) {
        FormData form = pokemon.getForm();
        Learnset learnset = form.getMoves();
        return learnset.getLevelUpMovesUpTo(pokemon.getLevel());
    }

    public static List<MoveTemplate> getSelfDamagingMoves(Pokemon pokemon) {
        Set<MoveTemplate> learnset = getPokemonLearnsetUpToLevel(pokemon);
        
        return learnset.stream()
                .filter(SelfDamageHelper::isSelfDamagingMove)
                .collect(Collectors.toList());
    }
}
