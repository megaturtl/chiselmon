package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ConfigCommand implements ChiselmonCommand {

    @Override
    public String getName() {
        return "config";
    }

    @Override
    public String getDescription() {
        return "Open the mod config screen";
    }

    @Override
    public <S> LiteralArgumentBuilder<S> build() {
        return LiteralArgumentBuilder.<S>literal(getName())
                .executes(this::execute);
    }

    private <S> int execute(CommandContext<S> context) {
            Screen screen = ChiselmonConfig.createScreen(Minecraft.getInstance().screen);
            Minecraft.getInstance().setScreen(screen);
        return Command.SINGLE_SUCCESS;
    }
}