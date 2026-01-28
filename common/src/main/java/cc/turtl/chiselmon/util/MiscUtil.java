package cc.turtl.chiselmon.util;

import cc.turtl.chiselmon.ChiselmonConstants;
import net.minecraft.resources.ResourceLocation;

public class MiscUtil {
    public static ResourceLocation modResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ChiselmonConstants.MOD_ID, path);
    }
}
