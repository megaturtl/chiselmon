package cc.turtl.chiselmon.config;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.config.category.AlertsConfig;
import cc.turtl.chiselmon.config.category.FilterConfig;
import cc.turtl.chiselmon.config.category.GeneralConfig;
import cc.turtl.chiselmon.config.category.PCConfig;
import cc.turtl.chiselmon.util.MiscUtil;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import static cc.turtl.chiselmon.util.format.ComponentUtils.modTranslatable;

public class ChiselmonConfig {
    private static final ConfigClassHandler<ChiselmonConfig> HANDLER =
            ConfigClassHandler.createBuilder(ChiselmonConfig.class)
                    .id(MiscUtil.modResource("config"))
                    .serializer(config -> GsonConfigSerializerBuilder.create(config)
                            .setPath(ChiselmonConstants.CONFIG_PATH.resolve("config.json"))
                            .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                            .build())
                    .build();

    @SerialEntry
    public final GeneralConfig general = new GeneralConfig();

    @SerialEntry
    public final PCConfig pc = new PCConfig();

    @SerialEntry
    public final AlertsConfig alerts = new AlertsConfig();

    // Not serialized, this is purely a UI builder now
    public final FilterConfig filter = new FilterConfig();

    public static ChiselmonConfig get() {
        return HANDLER.instance();
    }

    public static void load() {
        HANDLER.load();
        HANDLER.save();
    }

    public static void save() {
        HANDLER.save();
    }

    public static Screen createScreen(Screen parent) {
        var builder = YetAnotherConfigLib.createBuilder()
                .title(modTranslatable("config.title"))
                .category(get().general.buildCategory(parent))
                .category(get().pc.buildCategory(parent))
                .category(get().filter.buildCategory(parent))
                .category(get().alerts.buildCategory(parent))
                .save(ChiselmonConfig::save);

        var config = builder.build();
        return config.generateScreen(parent);
    }

    /**
     * Saves config and recreates the screen to reflect changes.
     * Takes a tab index to switch back to that tab after reload.
     * This is a workaround since YACL doesn't support in-place refresh.
     */
    public static void saveAndReloadScreen(Screen parent, int tabIndex) {
        ChiselmonConfig.save();
        YACLScreen newScreen = (YACLScreen) ChiselmonConfig.createScreen(parent);
        Minecraft.getInstance().setScreen(newScreen);
        switchTab(newScreen, tabIndex);
    }

    private static void switchTab(YACLScreen screen, int tabIndex) {
        if (screen.tabNavigationBar != null) {
            // Ensure index is within bounds of the tabs list
            if (tabIndex >= 0 && tabIndex < screen.tabNavigationBar.getTabs().size()) {
                screen.tabNavigationBar.selectTab(tabIndex, false);
            }
        }
    }
}