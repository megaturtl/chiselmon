package cc.turtl.chiselmon.neoforge;

import cc.turtl.chiselmon.Chiselmon;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.ChiselmonConstants;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(ChiselmonConstants.MOD_ID)
public final class ChiselmonNeoForge {
    public ChiselmonNeoForge() {
        Chiselmon.initClient();
        registerConfigScreen();
    }

    // Links the Cloth Config screen to the NeoForge mod menu
    private void registerConfigScreen() {
        ModList.get().getModContainerById(ChiselmonConstants.MOD_ID)
                .ifPresent(c -> c.registerExtensionPoint(IConfigScreenFactory.class, this::createConfigScreen));
    }
    private Screen createConfigScreen(ModContainer container, Screen parent) {
        return AutoConfig.getConfigScreen(ChiselmonConfig.class, parent).get();
    }
}
