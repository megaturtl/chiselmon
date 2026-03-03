package cc.turtl.chiselmon.neoforge;

import cc.turtl.chiselmon.ChiselmonCommands;
import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.ChiselmonKeybinds;
import cc.turtl.chiselmon.ChiselmonPacks;
import cc.turtl.chiselmon.platform.PlatformEventHandlers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import static cc.turtl.chiselmon.util.MiscUtil.modResource;

// Subscribes common handlers to platform-specific events
// NeoForge events will auto register with the decorators (no need to call anything in init)
@EventBusSubscriber
public class EventRegisterNeoForge {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent e) {
        ChiselmonCommands.register(e.getDispatcher());
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        // Add Chiselmon builtins
        for (ChiselmonPacks.BuiltInPack pack : ChiselmonPacks.BuiltInPack.ALL) {
            if (pack.requiredModIds().stream().allMatch(ModList.get()::isLoaded)) {
                event.addPackFinders(
                        modResource("resourcepacks/" + pack.id()),
                        PackType.CLIENT_RESOURCES,
                        Component.literal(pack.name()),
                        PackSource.BUILT_IN,
                        false,
                        Pack.Position.TOP
                );
            }
        }

        // Create the Custom Wallpapers pack structure
        ChiselmonPacks.getOrCreateCustomWallpaperDir();
        // Add config folder as a pack source
        event.addRepositorySource(new FolderRepositorySource(
                ChiselmonConstants.CONFIG_PATH,
                PackType.CLIENT_RESOURCES,
                PackSource.BUILT_IN,
                new DirectoryValidator(path -> true)
        ));
    }

    @SubscribeEvent
    static void registerKeybinds(RegisterKeyMappingsEvent event) {
        ChiselmonKeybinds.ALL.forEach(event::register);
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

    @SubscribeEvent
    public static void onClientChat(ClientChatEvent e) {
        if (e.getOriginalMessage().startsWith("/")) {
            PlatformEventHandlers.handleCommandSent(e.getOriginalMessage().substring(1));
        }
    }

    @SubscribeEvent
    public static void onSystemMessage(ClientChatReceivedEvent.System e) {
        if (e.isOverlay()) return;
        Component result = PlatformEventHandlers.handleGameMessageReceived(e.getMessage());
        if (result != null) e.setMessage(result);
    }
}
