package cc.turtl.chiselmon.config.custom;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;

public class HoldToConfirmController implements Controller<BiConsumer<YACLScreen, HoldToConfirmButton>> {
    private final HoldToConfirmButton option;
    private final Component text;

    public HoldToConfirmController(HoldToConfirmButton option, Component text) {
        this.option = option;
        this.text = text;
    }

    @Override
    public HoldToConfirmButton option() {
        return option;
    }

    @Override
    public Component formatValue() {
        return text;
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new HoldToConfirmWidget(this, screen, widgetDimension);
    }
}