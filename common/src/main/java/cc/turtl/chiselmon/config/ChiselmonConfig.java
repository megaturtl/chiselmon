package cc.turtl.chiselmon.config;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.config.category.GeneralCategory;
import cc.turtl.chiselmon.platform.PlatformHelper;
import cc.turtl.chiselmon.util.MiscUtil;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.client.gui.screens.Screen;

import java.nio.file.Path;

import static cc.turtl.chiselmon.util.format.ComponentUtils.modTranslatable;

public class ChiselmonConfig {
    private static final Path PATH = PlatformHelper.getPathFinder().getConfigDir().resolve(ChiselmonConstants.MOD_ID + ".json5");
    private static final ConfigClassHandler<ChiselmonConfig> HANDLER = ConfigClassHandler.createBuilder(ChiselmonConfig.class)
            .id(MiscUtil.modResource("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(PATH)
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

    // CONFIG FIELD CATEGORIES
    @SerialEntry
    public GeneralCategory general = new GeneralCategory();

    public static ChiselmonConfig getInstance() {
        return HANDLER.instance();
    }

    public static void load() {
        HANDLER.load();
    }

    public static void save() {
        HANDLER.save();
    }

    public static Screen createScreen(Screen parent) {
        ChiselmonConfig config = HANDLER.instance();

        return YetAnotherConfigLib.createBuilder()
                .title(modTranslatable("config.title"))
                .category(config.general.build())
                .save(ChiselmonConfig::save)
                .build()
                .generateScreen(parent);
    }

}