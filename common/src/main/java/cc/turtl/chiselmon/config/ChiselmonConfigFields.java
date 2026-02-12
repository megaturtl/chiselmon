package cc.turtl.chiselmon.config;

import cc.turtl.chiselmon.leveldata.PersistentLevelData;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.AutoGen;
import dev.isxander.yacl3.config.v2.api.autogen.Boolean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChiselmonConfigFields {
    @AutoGen(category = "General")
    @Boolean
    @SerialEntry(comment = "Disable the entire mod")
    public boolean modDisabled = false;

    @SerialEntry(comment = "Threshold configuration for size and IVs")
    public ThresholdConfig threshold = new ThresholdConfig();

    // Storing basic info per level/server in the config for now (I am aware of the jankiness :sob:)
    // Not serialized since it doesn't have @SerialEntry
    public Map<String, PersistentLevelData> levelDataMap = new ConcurrentHashMap<>();

    public static class ThresholdConfig {
        @SerialEntry(comment = "Extreme small threshold")
        public float extremeSmall = 0.4F;

        @SerialEntry(comment = "Extreme large threshold")
        public float extremeLarge = 1.6F;

        @SerialEntry(comment = "Maximum IVs")
        public int maxIvs = 5;
    }
}
