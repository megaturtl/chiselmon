package cc.turtl.chiselmon.neoforge;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.ChiselmonConfig;
import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.event.ClientTickHandler;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ChiselmonConstants.MOD_ID)
public final class ChiselmonNeoForge {
    public ChiselmonNeoForge() {
        // Run our common setup.
        Chiselmon.initClient();

        // Register events to the common handler
        NeoForge.EVENT_BUS.addListener(this::onClientTick);

        registerConfigScreen();
    }

    private void onClientTick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        ClientTickHandler.handle(mc.level == null);
    }

    private void registerConfigScreen() {
        ModList.get().getModContainerById(ChiselmonConstants.MOD_ID)
                .ifPresent(c -> c.registerExtensionPoint(IConfigScreenFactory.class, this::createConfigScreen));
    }

    private Screen createConfigScreen(ModContainer container, Screen parent) {
        return AutoConfig.getConfigScreen(ChiselmonConfig.class, parent).get();
    }
}
