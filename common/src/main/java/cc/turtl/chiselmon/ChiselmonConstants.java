package cc.turtl.chiselmon;


import cc.turtl.chiselmon.platform.PlatformHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

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
}
