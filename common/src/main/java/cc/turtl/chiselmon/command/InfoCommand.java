package cc.turtl.chiselmon.command;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.util.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class InfoCommand implements ChiselmonCommand {

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "Display mod info";
    }

    @Override
    public <S> LiteralArgumentBuilder<S> build() {
        return LiteralArgumentBuilder.<S>literal(getName())
                .executes(this::execute);
    }

    private <S> int execute(CommandContext<S> context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        MessageUtils.sendEmptyLine(player);
        MessageUtils.sendSuccess(player, ChiselmonConstants.MOD_DISPLAY_NAME + " Info");
        MessageUtils.sendLabeled(player, "  Version", ChiselmonConstants.VERSION);
        MessageUtils.sendLabeled(player, "  Author", ChiselmonConstants.AUTHOR);
        return Command.SINGLE_SUCCESS;
    }
}