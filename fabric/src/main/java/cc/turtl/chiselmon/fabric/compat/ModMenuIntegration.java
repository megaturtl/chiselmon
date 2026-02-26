package cc.turtl.chiselmon.fabric.compat;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ChiselmonConfig::createScreen;
    }
}