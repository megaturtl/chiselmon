package cc.turtl.chiselmon.system.group;

import cc.turtl.chiselmon.system.group.match.GroupMatchResult;
import cc.turtl.chiselmon.system.group.match.GroupMatcher;
import com.cobblemon.mod.common.pokemon.Pokemon;

public class PokemonGroupSystem {
    private final PokemonGroupRegistry registry;
    private final GroupMatcher matcher;

    public PokemonGroupSystem() {
        this.registry = new PokemonGroupRegistry();
        this.matcher = new GroupMatcher(registry);
    }

    public PokemonGroupRegistry getRegistry() {
        return registry;
    }

    public GroupMatchResult match(Pokemon pokemon) {
        return matcher.match(pokemon);
    }
}