package cc.turtl.chiselmon.platform;

import cc.turtl.chiselmon.api.event.*;
import cc.turtl.chiselmon.api.predicate.PokemonEntityPredicates;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public class PlatformEventHandlers {
    public static void handleEntityLoad(Entity entity, ClientLevel clientLevel) {
        if (entity instanceof PokemonEntity pe) {
            PokemonLoadedEvent event = new PokemonLoadedEvent(pe, PokemonEntityPredicates.IS_WILD.test(pe));
            ChiselmonEvents.POKEMON_LOADED.emit(event);
        }
    }

    public static void handleEntityUnload(Entity entity, ClientLevel clientLevel) {
        if (entity instanceof PokemonEntity pe) {
            PokemonUnloadedEvent event = new PokemonUnloadedEvent(pe, PokemonEntityPredicates.IS_WILD.test(pe));
            ChiselmonEvents.POKEMON_UNLOADED.emit(event);
        }
    }

    public static void handleClientPostTick(Minecraft client) {
        ClientPostTickEvent event = new ClientPostTickEvent(client);
        ChiselmonEvents.CLIENT_POST_TICK.emit(event);
    }

    public static void handleLevelConnect() {
        LevelConnectedEvent event = new LevelConnectedEvent();
        ChiselmonEvents.LEVEL_CONNECTED.emit(event);
    }

    public static void handleLevelDisconnect() {
        LevelDisconnectedEvent event = new LevelDisconnectedEvent();
        ChiselmonEvents.LEVEL_DISCONNECTED.emit(event);
    }
}
