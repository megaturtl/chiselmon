package cc.turtl.chiselmon.core.util

import cc.turtl.chiselmon.BuildDetails
import net.minecraft.resources.ResourceLocation

/**
 * Creates a ResourceLocation based on the Chiselmon mod id.
 */
fun modResource(path: String): ResourceLocation {
    return ResourceLocation.fromNamespaceAndPath(BuildDetails.MOD_ID, path)
}