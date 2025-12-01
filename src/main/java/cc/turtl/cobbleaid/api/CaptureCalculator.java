package cc.turtl.cobbleaid.api;

import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.BurnStatus;
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.FrozenStatus;
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.ParalysisStatus;
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.PoisonBadlyStatus;
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.PoisonStatus;
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.SleepStatus;

import cc.turtl.cobbleaid.api.util.PlayerUtil;

import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.world.entity.player.Player;
import java.lang.Math;

public class CaptureCalculator {
    public static float getCatchChance(Player player, PokemonEntity entity, PokeBall ball) {
        Pokemon pokemon = entity.getPokemon();
        final int pokemonCatchRate = pokemon.getSpecies().getCatchRate();

        final CatchRateModifier catchRateModifier = ball.getCatchRateModifier();
        if (catchRateModifier.isGuaranteed()) {
            return 1.0F;
        }
        final boolean isBallModifierValid = ball.getCatchRateModifier().isValid(player, pokemon);

        float ballModifier = (isBallModifierValid) ? catchRateModifier.value(player, pokemon) : 1.0F;

        float inBattleModifier = (entity.isBattling()) ? 1.0F : 0.5F;

        float levelDifferenceMultiplier = Math.max(0.1F, Math.min(1F, 1F - ((pokemon.getLevel() - PlayerUtil.getHighestLevel()) / 50)));

        float levelModifier = (pokemon.getLevel() < 13)
                ? Math.max((36.0F - (2.0F * pokemon.getLevel())) / 10.0F, 1.0F)
                : 1.0F;

        float statusModifier;
        if (pokemon.getStatus() != null &&
                (pokemon.getStatus().getStatus() instanceof SleepStatus ||
                        pokemon.getStatus().getStatus() instanceof FrozenStatus)) {
            statusModifier = 2.5F;
        } else if (pokemon.getStatus() != null &&
                (pokemon.getStatus().getStatus() instanceof ParalysisStatus ||
                        pokemon.getStatus().getStatus() instanceof BurnStatus ||
                        pokemon.getStatus().getStatus() instanceof PoisonStatus ||
                        pokemon.getStatus().getStatus() instanceof PoisonBadlyStatus)) {
            statusModifier = 1.5F;
        } else {
            statusModifier = 1.0F;
        }

        float modifiedCatchRate = (catchRateModifier
                .behavior(player, pokemon)
                .getMutator()
                .invoke(
                        (3F * pokemon.getMaxHealth() - 2F * pokemon.getCurrentHealth()) * pokemonCatchRate * inBattleModifier, ballModifier))
                / (3F * pokemon.getMaxHealth());

        modifiedCatchRate *= statusModifier * levelModifier * levelDifferenceMultiplier;

        float shakeProbability = (float) ((65536F / Math.pow(255F / modifiedCatchRate, 0.1875F)) / 65536F);

        return (float) Math.min(Math.pow(shakeProbability, 4), 1.0F);
    }


    public static float getMaxCatchChance(Player player, PokemonEntity entity, PokeBall ball) {
        Pokemon pokemon = entity.getPokemon();
        final int pokemonCatchRate = pokemon.getSpecies().getCatchRate();

        final CatchRateModifier catchRateModifier = ball.getCatchRateModifier();
        if (catchRateModifier.isGuaranteed()) {
            return 1.0F;
        }

        float ballModifier = catchRateModifier.value(player, pokemon);

        float inBattleModifier = (entity.isBattling()) ? 1.0F : 0.5F;

        float levelDifferenceMultiplier = Math.max(0.1F, Math.min(1F, 1F - ((pokemon.getLevel() - PlayerUtil.getHighestLevel()) / 50)));

        float levelModifier = (pokemon.getLevel() < 13)
                ? Math.max((36.0F - (2.0F * pokemon.getLevel())) / 10.0F, 1.0F)
                : 1.0F;

        float statusModifier;
        if (pokemon.getStatus() != null &&
                (pokemon.getStatus().getStatus() instanceof SleepStatus ||
                        pokemon.getStatus().getStatus() instanceof FrozenStatus)) {
            statusModifier = 2.5F;
        } else if (pokemon.getStatus() != null &&
                (pokemon.getStatus().getStatus() instanceof ParalysisStatus ||
                        pokemon.getStatus().getStatus() instanceof BurnStatus ||
                        pokemon.getStatus().getStatus() instanceof PoisonStatus ||
                        pokemon.getStatus().getStatus() instanceof PoisonBadlyStatus)) {
            statusModifier = 1.5F;
        } else {
            statusModifier = 1.0F;
        }

        float modifiedCatchRate = (catchRateModifier
                .behavior(player, pokemon)
                .getMutator()
                .invoke(
                        (3F * pokemon.getMaxHealth() - 2F * pokemon.getCurrentHealth()) * pokemonCatchRate * inBattleModifier, ballModifier))
                / (3F * pokemon.getMaxHealth());

        modifiedCatchRate *= statusModifier * levelModifier * levelDifferenceMultiplier;

        float shakeProbability = (float) ((65536F / Math.pow(255F / modifiedCatchRate, 0.1875F)) / 65536F);

        return (float) Math.min(Math.pow(shakeProbability, 4), 1.0F);
    }
}
