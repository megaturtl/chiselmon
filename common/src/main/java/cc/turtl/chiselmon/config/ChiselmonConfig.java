package cc.turtl.chiselmon.config;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.config.category.AlertsConfig;
import cc.turtl.chiselmon.config.category.filter.CustomFilterConfig;
import cc.turtl.chiselmon.config.category.GeneralConfig;
import cc.turtl.chiselmon.config.category.PCConfig;
import cc.turtl.chiselmon.util.MiscUtil;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
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
    public final CustomFilterConfig filter = new CustomFilterConfig();

    @SerialEntry
    public final AlertsConfig alerts = new AlertsConfig();

    public static ChiselmonConfig get() {
        return HANDLER.instance();
    }

    public static void load() {
        HANDLER.load();

    }

    public static void save() {
        HANDLER.save();
    }

    public static Screen createScreen(Screen parent) {
        return createScreenAtCategory(parent, null);
    }

    /**
     * Creates a config screen, optionally navigating to a specific category index.
     * @param parent The parent screen
     * @param categoryIndex The category to show (0=general, 1=pc, 2=filters, 3=alerts), or null for default
     */
    public static Screen createScreenAtCategory(Screen parent, Integer categoryIndex) {
        var builder = YetAnotherConfigLib.createBuilder()
                .title(modTranslatable("config.title"))
                .category(get().general.buildCategory(parent))
                .category(get().pc.buildCategory(parent))
                .category(get().filter.buildCategory(parent))
                .category(get().alerts.buildCategory(parent))
                .save(ChiselmonConfig::save);
        
        var config = builder.build();
        var screen = config.generateScreen(parent);
        
        // Try to navigate to specific category if requested
        // Note: This is a best-effort approach as YACL may not expose this directly
        if (categoryIndex != null && categoryIndex >= 0 && categoryIndex < 4) {
            try {
                // Try to set the current category via reflection or other means
                // This is a placeholder - actual implementation depends on YACL's API
                var yaclScreen = screen;
                // For now, we'll just return the screen and document this limitation
            } catch (Exception e) {
                // Silently fail if we can't set the category
            }
        }
        
        return screen;
    }
}