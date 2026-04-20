package cc.turtl.chiselmon.client

import cc.turtl.chiselmon.core.ChiselmonConstants
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object ChiselmonPacks {

    data class BuiltInPack(
        val id: String,
        val name: String,
        val requiredModIds: Set<String>,
    ) {
        companion object {
            val ALL = listOf(
                BuiltInPack("wallpapers_pride", "Chiselmon Pride Wallpapers", setOf("cobblemon")),
                BuiltInPack("wallpapers_default", "Chiselmon Default Wallpapers", setOf("cobblemon")),
            )
        }
    }

    fun getOrCreateCustomWallpaperDir(): Path {
        val userPackDir = ChiselmonConstants.CONFIG_PATH.resolve("Custom_PC_Wallpapers")
        val wallpaperDir = userPackDir.resolve("assets/cobblemon/textures/gui/pc/wallpaper")

        try {
            Files.createDirectories(wallpaperDir)
            userPackDir.resolve("pack.mcmeta").writeIfAbsent(MCMETA_CONTENT)
            wallpaperDir.resolve("instructions.txt").writeIfAbsent(INSTRUCTIONS_CONTENT)
        } catch (e: IOException) {
            ChiselmonConstants.LOGGER.error("Unable to create custom wallpaper files: {}", e.message)
        }

        return wallpaperDir
    }

    private fun Path.writeIfAbsent(content: String) {
        if (!Files.exists(this)) Files.writeString(this, content)
    }

    private val MCMETA_CONTENT = """
        {
          "pack": {
            "pack_format": 34,
            "supported_formats": [34, 69],
            "description": "Added by Chiselmon"
          }
        }
    """.trimIndent()

    private val INSTRUCTIONS_CONTENT = """
        --- Chiselmon Custom Wallpapers ---
        
        To add your own wallpapers:
        1. Drop .png images into this folder.
        2. Make sure the file names do not have any capital letters or spaces.
        3. Make sure the 'Custom_PC_Wallpapers' resource pack is enabled in your game settings.
        4. To load your new wallpapers you can either:
            - Fully restart your game.
            - Reload textures using F3+T and then relog to your current world/server.
            - Enable and disable the 'Custom_PC_Wallpapers' resource pack and then relog to your current world/server.
        5. They should now show up in your PC options!
        
        Tip: Wallpapers show up in cobblemon in alphabetical order. You can name yours "aa_my_wallpaper.png" to make it appear first!
        
        Note: Native Cobblemon wallpapers are 174x155 pixels. If your wallpapers aren't showing up in-game try to use the same size, or at least the same aspect ratio.
    """.trimIndent()
}