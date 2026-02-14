package cc.turtl.chiselmon;


import cc.turtl.chiselmon.api.OLDChiselmonConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChiselmonConstants {
    public static final String MOD_ID = "chiselmon";
    public static final String MOD_NAME = "Chiselmon";
    public static final String VERSION = "1.1.0-alpha";
    public static final String AUTHOR = "megaturtl";


    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final ConfigHolder<OLDChiselmonConfig> CONFIG_HOLDER = AutoConfig.getConfigHolder(OLDChiselmonConfig.class);
    public static final OLDChiselmonConfig CONFIG = CONFIG_HOLDER.getConfig();
}
