package cc.turtl.chiselmon.feature.spawnalert;

import java.util.EnumSet;
import java.util.Set;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import net.minecraft.client.Minecraft;

// A pokemon entity that is currently loaded in the player's client
public class LoadedPokemonWrapper {
    public final PokemonEntity entity;
    public final long loadedGameTime;
    public boolean chatMessageSent = false;
    public boolean muted = false;
    public Set<AlertType> alertTypes = EnumSet.noneOf(AlertType.class);

    public LoadedPokemonWrapper(PokemonEntity entity) {
        this.entity = entity;
        this.loadedGameTime = Minecraft.getInstance().level.getGameTime();
    }

    public void updateAlertTypes(SpawnAlertConfig config) {
        this.alertTypes.clear();
        Pokemon pokemon = this.entity.getPokemon();
        if (PokemonPredicates.IS_SHINY.test(pokemon)) {
            this.alertTypes.add(AlertType.SHINY);
        }
        if (PokemonPredicates.IS_EXTREME_SIZE.test(pokemon)) {
            this.alertTypes.add(AlertType.SIZE);
        }
        if (!PokemonPredicates.isInCustomList(config.blacklist).test(pokemon)) {
            if (PokemonPredicates.IS_SPECIAL.test(pokemon)) {
                this.alertTypes.add(AlertType.LEGENDARY);
            }
            if (PokemonPredicates.isInCustomList(config.list.whitelist).test(pokemon)) {
                this.alertTypes.add(AlertType.LIST);
            }
        }
    }

    public AlertType getWinningAlertType(SpawnAlertConfig config) {
        this.updateAlertTypes(config);

        return alertTypes.stream()
                .filter(t -> t.isEnabled(config))
                .max((p1, p2) -> Integer.compare(p1.getWeight(config), p2.getWeight(config)))
                .orElse(AlertType.NONE);
    }
}
