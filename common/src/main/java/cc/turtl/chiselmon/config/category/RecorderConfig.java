package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.config.OptionFactory;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RecorderConfig {

    public static final boolean DEFAULT_ACTION_BAR = true;
    public static final boolean DEFAULT_DESPAWN_GLOW = false;

    @SerialEntry
    public boolean actionBar = DEFAULT_ACTION_BAR;
    @SerialEntry
    public boolean despawnGlow = DEFAULT_DESPAWN_GLOW;

    public ConfigCategory buildCategory(Screen parent) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("chiselmon.config.category.recorder"))
                .option(OptionFactory.toggleOnOff(
                        "chiselmon.config.recorder.action_bar",
                        DEFAULT_ACTION_BAR,
                        () -> actionBar,
                        v -> actionBar = v
                ))
                .option(OptionFactory.toggleOnOff(
                        "chiselmon.config.recorder.despawn_glow",
                        DEFAULT_DESPAWN_GLOW,
                        () -> despawnGlow,
                        v -> despawnGlow = v
                ))
                .build();
    }
}