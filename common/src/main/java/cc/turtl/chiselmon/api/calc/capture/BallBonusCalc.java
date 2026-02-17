package cc.turtl.chiselmon.api.calc.capture;

import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.client.battle.ClientBattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.status.PersistentStatus;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates catch rate bonuses for different PokeBall types.
 */
public class BallBonusCalc {

    private static final Map<PokeBall, BallStrategy> STRATEGIES = new HashMap<>();

    static {
        registerDefaultStrategies();
    }

    /**
     * Registers a custom strategy for a specific ball type.
     * Allows adding or overriding ball behaviors.
     */
    public static void registerStrategy(PokeBall ball, BallStrategy strategy) {
        STRATEGIES.put(ball, strategy);
    }

    /**
     * Calculates the ball bonus for the given context.
     */
    public static float calculateBallBonus(
            PokeBall ball,
            PokemonEntity targetEntity,
            LocalPlayer player,
            List<ClientBattlePokemon> playerActiveBattlePokemon,
            PersistentStatus targetStatus) {

        Level level = targetEntity.level();
        CaptureContext context = new CaptureContext(
                targetEntity,
                targetEntity.getPokemon(),
                playerActiveBattlePokemon,
                targetStatus,
                level,
                targetEntity.blockPosition()
        );

        return STRATEGIES.getOrDefault(ball, ctx -> 1.0F).calculate(context);
    }

    private static void registerDefaultStrategies() {
        // Standard balls
        registerStrategy(PokeBalls.getPokeBall(), ctx -> 1.0F);
        registerStrategy(PokeBalls.getGreatBall(), ctx -> 1.5F);
        registerStrategy(PokeBalls.getSportBall(), ctx -> 1.5F);
        registerStrategy(PokeBalls.getUltraBall(), ctx -> 2.0F);
        registerStrategy(PokeBalls.getMasterBall(), ctx -> 999.0F);

        // Ancient balls
        registerStrategy(PokeBalls.getAncientGreatBall(), ctx -> 1.5F);
        registerStrategy(PokeBalls.getAncientUltraBall(), ctx -> 2.0F);
        registerStrategy(PokeBalls.getAncientOriginBall(), ctx -> 999.0F);

        // Effect-only balls (no catch rate modifier)
        registerStrategy(PokeBalls.getFriendBall(), ctx -> 1.0F);
        registerStrategy(PokeBalls.getLuxuryBall(), ctx -> 1.0F);
        registerStrategy(PokeBalls.getPremierBall(), ctx -> 1.0F);
        registerStrategy(PokeBalls.getHealBall(), ctx -> 1.0F);
        registerStrategy(PokeBalls.getCherishBall(), ctx -> 1.0F);

        // Specialty balls - delegate to strategy classes
        registerStrategy(PokeBalls.getParkBall(), new ParkBallStrategy());
        registerStrategy(PokeBalls.getSafariBall(), new SafariBallStrategy());
        registerStrategy(PokeBalls.getFastBall(), new FastBallStrategy());
        registerStrategy(PokeBalls.getLevelBall(), new LevelBallStrategy());
        registerStrategy(PokeBalls.getLureBall(), new LureBallStrategy());
        registerStrategy(PokeBalls.getHeavyBall(), new HeavyBallStrategy());
        registerStrategy(PokeBalls.getLoveBall(), new LoveBallStrategy());
        registerStrategy(PokeBalls.getMoonBall(), new MoonBallStrategy());
        registerStrategy(PokeBalls.getNetBall(), new NetBallStrategy());
        registerStrategy(PokeBalls.getDiveBall(), new DiveBallStrategy());
        registerStrategy(PokeBalls.getNestBall(), new NestBallStrategy());
        registerStrategy(PokeBalls.getRepeatBall(), new RepeatBallStrategy());
        registerStrategy(PokeBalls.getTimerBall(), new TimerBallStrategy());
        registerStrategy(PokeBalls.getDuskBall(), new DuskBallStrategy());
        registerStrategy(PokeBalls.getQuickBall(), new QuickBallStrategy());
        registerStrategy(PokeBalls.getDreamBall(), new DreamBallStrategy());
        registerStrategy(PokeBalls.getBeastBall(), new BeastBallStrategy());
    }
}