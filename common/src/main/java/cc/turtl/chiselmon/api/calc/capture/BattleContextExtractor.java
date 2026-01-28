package cc.turtl.chiselmon.api.calc.capture;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.cobblemon.mod.common.client.battle.ClientBattleActor;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.status.PersistentStatus;
import net.minecraft.client.player.LocalPlayer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Extracts battle-related context from client-side battle state.
 */
public class BattleContextExtractor {

    public BattleContext extract(LocalPlayer player, PokemonEntity targetEntity) {
        Optional<ClientBattle> battleOpt = Optional.ofNullable(CobblemonClient.INSTANCE.getBattle());

        List<ClientBattlePokemon> playerActivePokemon = extractPlayerActivePokemon(battleOpt, player);
        PersistentStatus targetStatus = extractTargetStatus(battleOpt);

        return new BattleContext(playerActivePokemon, targetStatus);
    }

    private List<ClientBattlePokemon> extractPlayerActivePokemon(
            Optional<ClientBattle> battleOpt,
            LocalPlayer player) {
        return battleOpt
                .map(battle -> battle.getParticipatingActor(player.getUUID()))
                .map(ClientBattleActor::getActivePokemon)
                .orElse(Collections.emptyList())
                .stream()
                .map(ActiveClientBattlePokemon::getBattlePokemon)
                .collect(Collectors.toList());
    }

    private PersistentStatus extractTargetStatus(Optional<ClientBattle> battleOpt) {
        return battleOpt
                .map(ClientBattle::getWildActor)
                .map(ClientBattleActor::getActivePokemon)
                .filter(list -> !list.isEmpty())
                .map(list -> list.getFirst().getBattlePokemon().getStatus())
                .orElse(null);
    }
}