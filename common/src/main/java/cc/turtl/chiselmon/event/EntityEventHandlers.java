package cc.turtl.chiselmon.event;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public class EntityEventHandlers {
    /**
     * This method runs every time an entity is loaded client side (piped from each platforms event registry)
     */
    public static void handleLoad(Entity entity, ClientLevel clientLevel) {
    }

    public static void handleUnload(Entity entity, ClientLevel clientLevel) {
    }
}