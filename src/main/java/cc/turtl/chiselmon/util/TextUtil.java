package cc.turtl.chiselmon.util;

import org.jetbrains.annotations.NotNull;

import cc.turtl.chiselmon.Chiselmon;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class TextUtil {
    public static final MutableComponent bold(@NotNull MutableComponent component) {
        component.setStyle(component.getStyle().withBold(true));
        return component;
    }

    public static final MutableComponent underline(@NotNull MutableComponent component) {
        component.setStyle(component.getStyle().withUnderlined(true));
        return component;
    }

    public static final MutableComponent italic(@NotNull MutableComponent component) {
        component.setStyle(component.getStyle().withItalic(true));
        return component;
    }

    public static final MutableComponent strikethrough(@NotNull MutableComponent component) {
        component.setStyle(component.getStyle().withStrikethrough(true));
        return component;
    }

    public static final MutableComponent translate(@NotNull String string) {
        return Component.translatable(string);
    }

    public static final ResourceLocation modResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(Chiselmon.MODID, path);
    }
}
