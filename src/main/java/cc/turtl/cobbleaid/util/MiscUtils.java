package cc.turtl.cobbleaid.util;

import cc.turtl.cobbleaid.CobbleAid;
import net.minecraft.resources.ResourceLocation;

public class MiscUtils {

    public static ResourceLocation modResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(CobbleAid.MODID, path);
    }
}
