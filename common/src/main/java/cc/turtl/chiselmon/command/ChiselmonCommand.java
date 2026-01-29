package cc.turtl.chiselmon.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public interface ChiselmonCommand {

    /**
     * The command name in lowercase (e.g., "info", "debug")
     */
    String getName();

    /**
     * Short description for help text
     */
    String getDescription();

    /**
     * Build and return your command structure.
     * Use Commands.literal(getName()) and attach logic.
     */
    LiteralArgumentBuilder<CommandSourceStack> build();
}