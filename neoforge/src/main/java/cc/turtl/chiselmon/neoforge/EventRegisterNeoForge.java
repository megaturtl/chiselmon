package cc.turtl.chiselmon.neoforge;

import cc.turtl.chiselmon.ChiselmonCommands;
import cc.turtl.chiselmon.event.ClientTickHandler;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

// Subscribes common handlers to platform-specific events
// NeoForge events will auto register with the decorators (no need to call anything in init)
@EventBusSubscriber
public class EventRegisterNeoForge {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ChiselmonCommands.registerRoot(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        var client = Minecraft.getInstance();
        ClientTickHandler.handle(client);
    }
}
