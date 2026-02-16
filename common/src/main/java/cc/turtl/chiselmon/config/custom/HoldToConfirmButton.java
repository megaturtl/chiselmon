package cc.turtl.chiselmon.config.custom;

import com.google.common.collect.ImmutableSet;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class HoldToConfirmButton implements Option<BiConsumer<YACLScreen, HoldToConfirmButton>> {
    private final Component name;
    private final OptionDescription description;
    private final BiConsumer<YACLScreen, HoldToConfirmButton> action;
    private final Component buttonText;
    private final Component holdingText;
    private final int holdTimeTicks;
    private final StateManager<BiConsumer<YACLScreen, HoldToConfirmButton>> stateManager;
    private final HoldToConfirmController controller;
    private boolean available = true;

    private HoldToConfirmButton(Component name, OptionDescription description, BiConsumer<YACLScreen, HoldToConfirmButton> action, Component buttonText, Component holdingText, int holdTimeTicks) {
        this.name = name;
        this.description = description;
        this.action = action;
        this.buttonText = buttonText;
        this.holdingText = holdingText;
        this.holdTimeTicks = holdTimeTicks;
        this.stateManager = StateManager.createImmutable(action);
        this.controller = new HoldToConfirmController(this, buttonText);
    }

    public static Builder builder() {
        return new Builder();
    }

    public BiConsumer<YACLScreen, HoldToConfirmButton> action() {
        return action;
    }

    public int holdTimeTicks() {
        return holdTimeTicks;
    }

    public Component buttonText() {
        return buttonText;
    }

    public Component holdingText() {
        return holdingText;
    }

    @Override
    public @NotNull Component name() {
        return name;
    }

    @Override
    public @NotNull OptionDescription description() {
        return description;
    }

    @Override
    public @NotNull Component tooltip() {
        return description.text();
    }

    @Override
    public @NotNull Controller<BiConsumer<YACLScreen, HoldToConfirmButton>> controller() {
        return controller;
    }

    @Override
    public @NotNull StateManager<BiConsumer<YACLScreen, HoldToConfirmButton>> stateManager() {
        return stateManager;
    }

    @Override
    public @NotNull Binding<BiConsumer<YACLScreen, HoldToConfirmButton>> binding() {
        return EmptyBinding.INSTANCE;
    }

    @Override
    public boolean available() {
        return available;
    }

    @Override
    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public @NotNull ImmutableSet<OptionFlag> flags() {
        return ImmutableSet.of();
    }

    @Override
    public boolean changed() {
        return false;
    }

    @Override
    public @NotNull BiConsumer<YACLScreen, HoldToConfirmButton> pendingValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void requestSet(@NotNull BiConsumer<YACLScreen, HoldToConfirmButton> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean applyValue() {
        return false;
    }

    @Override
    public void forgetPendingValue() {
    }

    @Override
    public void requestSetDefault() {
    }

    @Override
    public boolean isPendingValueDefault() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addEventListener(OptionEventListener<BiConsumer<YACLScreen, HoldToConfirmButton>> listener) {
    }

    @Override
    public void addListener(BiConsumer<Option<BiConsumer<YACLScreen, HoldToConfirmButton>>, BiConsumer<YACLScreen, HoldToConfirmButton>> changedListener) {
    }

    public static class Builder {
        private Component name;
        private OptionDescription description = OptionDescription.EMPTY;
        private BiConsumer<YACLScreen, HoldToConfirmButton> action;
        private Component buttonText = Component.literal("Hold to Confirm");
        private Component holdingText = Component.literal("Release to Cancel");
        private int holdTimeTicks = 30;

        public Builder name(Component name) {
            this.name = name;
            return this;
        }

        public Builder description(OptionDescription description) {
            this.description = description;
            return this;
        }

        public Builder action(BiConsumer<YACLScreen, HoldToConfirmButton> action) {
            this.action = action;
            return this;
        }

        public Builder buttonText(Component text) {
            this.buttonText = text;
            return this;
        }

        public Builder holdingText(Component text) {
            this.holdingText = text;
            return this;
        }

        public Builder holdTimeTicks(int ticks) {
            this.holdTimeTicks = ticks;
            return this;
        }

        public HoldToConfirmButton build() {
            if (name == null) throw new IllegalStateException("Name must be set");
            if (action == null) throw new IllegalStateException("Action must be set");
            return new HoldToConfirmButton(name, description, action, buttonText, holdingText, holdTimeTicks);
        }
    }

    private static class EmptyBinding implements Binding<BiConsumer<YACLScreen, HoldToConfirmButton>> {
        private static final EmptyBinding INSTANCE = new EmptyBinding();

        @Override
        public BiConsumer<YACLScreen, HoldToConfirmButton> getValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setValue(BiConsumer<YACLScreen, HoldToConfirmButton> value) {
        }

        @Override
        public BiConsumer<YACLScreen, HoldToConfirmButton> defaultValue() {
            throw new UnsupportedOperationException();
        }
    }
}