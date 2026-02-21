package cc.turtl.chiselmon.config.custom;

import com.mojang.blaze3d.platform.InputConstants;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.network.chat.Component;

public record KeyController(Option<InputConstants.Key> option) implements Controller<InputConstants.Key> {

    @Override
    public Component formatValue() {
        return option.pendingValue().getDisplayName();
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new KeyWidget(this, screen, widgetDimension);
    }
}