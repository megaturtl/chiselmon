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
        return YetAnotherConfigLib.createBuilder()
                .title(modTranslatable("config.title"))
                .category(get().general.buildCategory())
                .category(get().pc.buildCategory())
                .category(get().filter.buildCategory())
                .category(get().alerts.buildCategory())
                .save(ChiselmonConfig::save)
                .build()
                .generateScreen(parent);
    }
}