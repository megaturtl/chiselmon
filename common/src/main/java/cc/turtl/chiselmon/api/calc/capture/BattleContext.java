package cc.turtl.chiselmon.api.calc.capture;

import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.pokemon.status.PersistentStatus;

import java.util.List;

public record BattleContext(
        List<ClientBattlePokemon> playerActivePokemon,
        PersistentStatus targetStatus
) {}