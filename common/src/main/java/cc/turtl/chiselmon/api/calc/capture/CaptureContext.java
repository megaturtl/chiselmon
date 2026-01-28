package cc.turtl.chiselmon.api.calc.capture;

import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.status.PersistentStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

public record CaptureContext(
        PokemonEntity targetEntity,
        Pokemon pokemon,
        List<ClientBattlePokemon> playerActiveBattlePokemon,
        PersistentStatus targetStatus,
        Level level,
        BlockPos pos
) {}