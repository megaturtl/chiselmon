package cc.turtl.chiselmon;

import cc.turtl.chiselmon.mixin.accessor.KeyMappingAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ChiselmonKeybinds {
    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.chiselmon.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SEMICOLON,
            "key.chiselmon.categories.chiselmon"
    );

    public static final KeyMapping MUTE_ALERTS = new KeyMapping(
            "key.chiselmon.mute_alerts",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "key.chiselmon.categories.chiselmon"
    );

    public static final List<KeyMapping> ALL = List.of(OPEN_CONFIG, MUTE_ALERTS);

    public static void rebind(KeyMapping keybind, InputConstants.Key key) {
        keybind.setKey(key);
        KeyMapping.resetMapping();
        Minecraft.getInstance().options.save();
    }

    public static boolean isDown(KeyMapping keybind) {
        return isDown(((KeyMappingAccessor) keybind).chiselmon$getKey());
    }

    public static boolean isDown(InputConstants.Key key) {
        if (key.equals(InputConstants.UNKNOWN)) return false;
        long window = Minecraft.getInstance().getWindow().getWindow();
        return switch (key.getType()) {
            case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
            case KEYSYM, SCANCODE -> GLFW.glfwGetKey(window, key.getValue()) == GLFW.GLFW_PRESS;
        };
    }
}