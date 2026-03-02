package cc.turtl.chiselmon.util;

import cc.turtl.chiselmon.ChiselmonConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CustomWallpaperUtil {
    private static final String FOLDER_NAME = ChiselmonConstants.MOD_DISPLAY_NAME + " Custom PC Wallpapers";
    private static final String PACK_ID = "file/" + FOLDER_NAME;

    public static void initializePack() {
        Minecraft mc = Minecraft.getInstance();

        Path packRoot = mc.getResourcePackDirectory().resolve(FOLDER_NAME);
        Path wallpaperDir = packRoot.resolve("assets/cobblemon/textures/gui/pc/wallpaper");
        Path mcmetaFile = packRoot.resolve("pack.mcmeta");

        try {
            if (!Files.exists(wallpaperDir)) {
                Files.createDirectories(wallpaperDir);
            }

            if (!Files.exists(mcmetaFile)) {
                String mcmetaContent = """
                        {
                          "pack": {
                            "pack_format": 15,
                            "description": "Custom Wallpapers for Chiselmon"
                          }
                        }
                        """;
                Files.writeString(mcmetaFile, mcmetaContent);
            }

            // Auto enable the pack
            if (mc.options != null) {
                List<String> enabledPacks = new ArrayList<>(mc.options.resourcePacks);
                if (!enabledPacks.contains(PACK_ID)) {
                    enabledPacks.add(PACK_ID);
                    mc.options.resourcePacks = enabledPacks;
                    mc.options.save();
                    mc.reloadResourcePacks();
                }
            }

        } catch (IOException e) {
            ChiselmonConstants.LOGGER.error("Unable to create directory for custom wallpaper: {}", e.getMessage());
        }
    }

    public static void openFolder() {
        Path path = Minecraft.getInstance().getResourcePackDirectory().resolve(FOLDER_NAME)
                .resolve("assets/cobblemon/textures/gui/pc/wallpaper");
        Util.getPlatform().openPath(path);
    }
}