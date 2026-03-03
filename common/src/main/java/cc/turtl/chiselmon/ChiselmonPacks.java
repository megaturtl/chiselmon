package cc.turtl.chiselmon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class ChiselmonPacks {
    public record BuiltInPack(
            String id,
            String name,
            Set<String> requiredModIds
    ) {
        public static final List<BuiltInPack> ALL = List.of(
                new BuiltInPack("wallpapers_pride", "Chiselmon Pride Wallpapers", Set.of("cobblemon")),
                new BuiltInPack("wallpapers_default", "Chiselmon Default Wallpapers", Set.of("cobblemon"))
        );
    }

    public static Path getOrCreateCustomWallpaperDir() {
        Path userPackDir = ChiselmonConstants.CONFIG_PATH.resolve("Custom_PC_Wallpapers");
        Path wallpaperDir = userPackDir.resolve("assets/cobblemon/textures/gui/pc/wallpaper");

        try {
            Files.createDirectories(wallpaperDir);

            Path mcmetaFile = userPackDir.resolve("pack.mcmeta");
            if (!Files.exists(mcmetaFile)) {
                String mcmetaContent = """
                    {
                      "pack": {
                        "pack_format": 34,
                        "supported_formats": [34, 69],
                        "description": "Added by Chiselmon"
                      }
                    }
                    """;
                Files.writeString(mcmetaFile, mcmetaContent);
            }

            Path instructionsFile = wallpaperDir.resolve("instructions.txt");
            if (!Files.exists(instructionsFile)) {
                String instructionsContent = """
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
                    
                    Note: Native Cobblemon wallpapers are 174x155 pixels. If your wallpapers aren't showing up in-game try to use the same size, or at least the same aspect ratio.
                    """;
                Files.writeString(instructionsFile, instructionsContent);
            }
        } catch (IOException e) {
            ChiselmonConstants.LOGGER.error("Unable to create custom wallpaper files: {}", e.getMessage());
        }
        return wallpaperDir;
    }
}
