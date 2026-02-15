package cc.turtl.chiselmon.neoforge;

import cc.turtl.chiselmon.ChiselmonCommands;
import cc.turtl.chiselmon.platform.PlatformEventHandlers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

// Subscribes common handlers to platform-specific events
// NeoForge events will auto register with the decorators (no need to call anything in init)
@EventBusSubscriber
public class EventRegisterNeoForge {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent e) {
        ChiselmonCommands.register(e.getDispatcher(), e.getBuildContext(), e.getCommandSelection());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post e) {
        var client = Minecraft.getInstance();
        PlatformEventHandlers.handleClientPostTick(client);
    }

    @SubscribeEvent
    public static void onEntityLoad(EntityJoinLevelEvent e) {
        if (e.getLevel() instanceof ClientLevel level) {
            PlatformEventHandlers.handleEntityLoad(e.getEntity(), level);
        }
    }

    @SubscribeEvent
    public static void onEntityUnload(EntityLeaveLevelEvent e) {
        if (e.getLevel() instanceof ClientLevel level) {
            PlatformEventHandlers.handleEntityUnload(e.getEntity(), level);
        }
    }

    @SubscribeEvent
    public static void onConnect(ClientPlayerNetworkEvent.LoggingIn e) {
        PlatformEventHandlers.handleLevelConnect();
    }

    @SubscribeEvent
    public static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut e) {
        PlatformEventHandlers.handleLevelDisconnect();
    }

    @SubscribeEvent
    public static void onGameStopping(GameShuttingDownEvent e) {
        PlatformEventHandlers.handleGameStopping();
    }
}
