package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.config.ChiselmonConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;

import java.util.List;

public class ConfigCommand implements ChiselmonCommand {
    private static final List<String> TABS = List.of("general", "pc", "filter", "alert", "recorder");

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
                .executes(this::execute)
                .then(com.mojang.brigadier.builder.RequiredArgumentBuilder
                        .<S, String>argument("tab", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            TABS.forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(this::executeWithTab));
    }

    private <S> int execute(CommandContext<S> context) {
        openScreen(0);
        return Command.SINGLE_SUCCESS;
    }

    private <S> int executeWithTab(CommandContext<S> context) {
        String tab = StringArgumentType.getString(context, "tab");
        int index = TABS.indexOf(tab);
        openScreen(Math.max(index, 0));
        return Command.SINGLE_SUCCESS;
    }

    private void openScreen(int tabIndex) {
        Minecraft mc = Minecraft.getInstance();
        mc.tell(() -> {
            YACLScreen screen = (YACLScreen) ChiselmonConfig.createScreen(mc.screen);
            mc.setScreen(screen);
            if (tabIndex > 0) {
                ChiselmonConfig.switchTab(screen, tabIndex);
            }
        });
    }
}