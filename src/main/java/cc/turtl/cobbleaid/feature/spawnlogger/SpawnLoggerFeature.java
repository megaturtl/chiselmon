package cc.turtl.cobbleaid.feature.spawnlogger;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import cc.turtl.cobbleaid.api.predicate.PokemonEntityPredicates;
import cc.turtl.cobbleaid.feature.AbstractFeature;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public final class SpawnLoggerFeature extends AbstractFeature {
    private static final SpawnLoggerFeature INSTANCE = new SpawnLoggerFeature();

    private SpawnLoggerSession currentSession;

    private SpawnLoggerFeature() {
        super("SpawnLogger");
    }

    public static SpawnLoggerFeature getInstance() {
        return INSTANCE;
    }

    @Override
    protected boolean isFeatureEnabled() {
        return getConfig().spawnLogger.enabled;
    }

    @Override
    protected void init() {
        ClientEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onEntityLoad(net.minecraft.world.entity.Entity entity, net.minecraft.world.level.Level level) {
        if (!canRun() || currentSession == null || currentSession.isPaused()) {
            return;
        }

        if (entity instanceof PokemonEntity pokemonEntity && shouldTrack(pokemonEntity)) {
            currentSession.log(entity.getUUID(), pokemonEntity.getPokemon());
        }
    }

    private void onClientTick(Minecraft client) {
        if (currentSession != null && currentSession.isExpired()) {
            finishSession();
        }
    }

    private boolean shouldTrack(PokemonEntity pokemonEntity) {
        return !PokemonEntityPredicates.IS_OWNED.test(pokemonEntity)
                && !PokemonEntityPredicates.IS_PLUSHIE.test(pokemonEntity);
    }

    public void startSession(int minutes) {
        if (currentSession == null) {
            currentSession = new SpawnLoggerSession(minutes);
        }
    }

    public boolean toggleSessionPause() {
        if (currentSession != null) {
            currentSession.togglePause();
            return !currentSession.isPaused();
        }
        return false;
    }

    public void finishSession() {
        if (currentSession == null) {
            return;
        }

        ResultsMessage.sendResultsMessage(currentSession);
        currentSession = null;
    }

    public SpawnLoggerSession getSession() {
        return currentSession;
    }
}