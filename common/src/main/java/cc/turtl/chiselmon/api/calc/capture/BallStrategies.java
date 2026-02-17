package cc.turtl.chiselmon.api.calc.capture;

import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.api.species.ClientSpeciesRegistry;
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import com.cobblemon.mod.common.api.tags.CobblemonBiomeTags;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.SleepStatus;

class ParkBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        return ctx.level().getBiome(ctx.pos()).is(CobblemonBiomeTags.IS_TEMPERATE) ? 2.5F : 1.0F;
    }
}

class SafariBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        return !ctx.targetEntity().isBattling() ? 1.5F : 1.0F;
    }
}

class FastBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        return ClientSpeciesRegistry.get(ctx.pokemon().getSpecies().getName()).baseStats().get("speed") >= 100 ? 4.0F : 1.0F;
    }
}

class LevelBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        if (ctx.playerActiveBattlePokemon() == null || ctx.playerActiveBattlePokemon().isEmpty()) {
            return 1.0F;
        }

        int maxPlayerLevel = ctx.playerActiveBattlePokemon().stream()
                .mapToInt(ClientBattlePokemon::getLevel)
                .max()
                .orElse(1);

        int targetLevel = ctx.pokemon().getLevel();

        if (maxPlayerLevel > targetLevel * 4) return 4.0F;
        if (maxPlayerLevel > targetLevel * 2) return 3.0F;
        if (maxPlayerLevel > targetLevel) return 2.0F;
        return 1.0F;
    }
}

class LureBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        return ctx.pokemon().getAspects().contains("fished") ? 4.0F : 1.0F;
    }
}

class HeavyBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        float weight = ctx.pokemon().getForm().getWeight();
        if (weight >= 3000F) return 4.0F;
        if (weight >= 2000F) return 2.5F;
        if (weight >= 1000F) return 1.5F;
        return 1.0F;
    }
}

class LoveBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        if (ctx.pokemon().getGender() == Gender.GENDERLESS) {
            return 1.0F;
        }

        boolean foundSameSpeciesOppositeGender = false;
        boolean foundOppositeGender = false;

        for (ClientBattlePokemon p : ctx.playerActiveBattlePokemon()) {
            if (p == null || p.getGender() == Gender.GENDERLESS) continue;

            if (p.getGender() != ctx.pokemon().getGender()) {
                foundOppositeGender = true;
                if (p.getSpecies().equals(ctx.pokemon().getSpecies())) {
                    foundSameSpeciesOppositeGender = true;
                    break;
                }
            }
        }

        if (foundSameSpeciesOppositeGender) return 8.0F;
        if (foundOppositeGender) return 2.5F;
        return 1.0F;
    }
}

class MoonBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        long time = ctx.level().getDayTime() % 24000;
        if (time < 12000) return 1.0F;

        return switch (ctx.level().getMoonPhase()) {
            case 0 -> 4.0F;
            case 1, 7 -> 2.5F;
            case 2, 6 -> 1.5F;
            default -> 1.0F;
        };
    }
}

class NetBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        for (ElementalType type : ctx.pokemon().getTypes()) {
            if (type == ElementalTypes.BUG || type == ElementalTypes.WATER) {
                return 3.0F; // Match found, return the bonus
            }
        }
        return 1.0F;
    }
}

class DiveBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        return ctx.targetEntity().isUnderWater() ? 3.5F : 1.0F;
    }
}

class NestBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        int level = ctx.pokemon().getLevel();
        return level < 30 ? (41.0F - level) / 10.0F : 1.0F;
    }
}

class RepeatBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        var playerDexData = CobblemonClient.INSTANCE.getClientPokedexData();
        var speciesId = ctx.pokemon().getSpecies().getResourceIdentifier();
        var knowledge = playerDexData.getKnowledgeForSpecies(speciesId);
        return knowledge == PokedexEntryProgress.CAUGHT ? 3.5F : 1.0F;
    }
}

class TimerBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        var battleId = ctx.targetEntity().getBattleId();
        if (battleId == null) return 1.0F;

        var battle = BattleRegistry.getBattle(battleId);
        if (battle == null) return 1.0F;

        int turn = battle.getTurn();
        float multiplier = 1.0F + (turn * (1229.0F / 4096.0F));
        return Math.min(4.0F, multiplier);
    }
}

class DuskBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        int brightness = ctx.level().getMaxLocalRawBrightness(ctx.pos());
        if (brightness == 0) return 3.5F;
        if (brightness <= 7) return 3.0F;
        return 1.0F;
    }
}

class QuickBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        var battleId = ctx.targetEntity().getBattleId();
        if (battleId == null) return 1.0F;

        var battle = BattleRegistry.getBattle(battleId);
        if (battle == null) return 1.0F;

        int turn = battle.getTurn();
        return turn == 1 ? 5.0F : 1.0F;
    }
}

class DreamBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        return ctx.targetStatus() instanceof SleepStatus ? 4.0F : 1.0F;
    }
}

// Cobblemon don't implement the 0.1x penalty yet
class BeastBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        return PokemonPredicates.IS_ULTRABEAST.test(ctx.pokemon()) ? 5.0F : 1.0F;
    }
}