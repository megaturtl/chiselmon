package cc.turtl.chiselmon;


import cc.turtl.chiselmon.platform.PlatformHelper;
import cc.turtl.chiselmon.util.format.ColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

import static cc.turtl.chiselmon.util.format.ComponentUtils.createComponent;

public class ChiselmonConstants {
    public static final String MOD_ID = "chiselmon";
    public static final String MOD_NAME = "Chiselmon";
    public static final String VERSION = "1.1.0-alpha";
    public static final String AUTHOR = "megaturtl";

    /**
     * The path to the '.minecraft/config/chiselmon' folder
     */
    public static final Path CONFIG_PATH = PlatformHelper.getPathFinder().getConfigDir().resolve(MOD_ID);

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final Component MESSAGE_PREFIX = Component.empty()
            .append(createComponent("[", ColorUtils.DARK_GRAY.getRGB()))
            .append(Component.literal("\uD83D\uDEE0")
                    .withColor(ColorUtils.PINK.getRGB())
                    .withStyle(ChatFormatting.BOLD))
            .append(createComponent("] ", ColorUtils.DARK_GRAY.getRGB()))
            .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    createComponent("Chiselmon", ColorUtils.PINK.getRGB())))
            );
}
