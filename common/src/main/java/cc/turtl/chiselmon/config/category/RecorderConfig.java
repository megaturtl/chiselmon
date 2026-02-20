package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.config.OptionFactory;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RecorderConfig implements ConfigCategoryBuilder {

    @SerialEntry
    public boolean actionBar = true;

    @SerialEntry
    public boolean despawnGlow = false;


    @Override
    public ConfigCategory buildCategory(Screen parent) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("chiselmon.config.category.recorder"))
                .option(OptionFactory.toggleOnOff(
                        "chiselmon.config.recorder.action_bar",
                        () -> actionBar,
                        v -> actionBar = v
                ))
                .option(OptionFactory.toggleOnOff(
                        "chiselmon.config.recorder.despawn_glow",
                        () -> despawnGlow,
                        v -> despawnGlow = v
                ))
                .build();
    }
}