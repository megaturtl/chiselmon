package cc.turtl.chiselmon.config;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.platform.PlatformHelper;
import cc.turtl.chiselmon.util.MiscUtil;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.client.gui.screens.Screen;

public class ChiselmonConfigHandler {
    private static final ConfigClassHandler<ChiselmonConfigFields> HANDLER = ConfigClassHandler.createBuilder(ChiselmonConfigFields.class)
            .id(MiscUtil.modResource("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(PlatformHelper.getPathFinder().getConfigDir().resolve(ChiselmonConstants.MOD_ID + ".json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

    public static ConfigClassHandler<ChiselmonConfigFields> getHandler() {
        return HANDLER;
    }

    public static ChiselmonConfigFields getInstance() {
        return HANDLER.instance();
    }

    public static void load() {
        HANDLER.load();
    }

    public static void save() {
        HANDLER.save();
    }
}