package cc.turtl.chiselmon.system.alert.action;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.filter.RuntimeFilter;
import cc.turtl.chiselmon.system.alert.AlertContext;
import cc.turtl.chiselmon.util.format.ColorUtils;
import cc.turtl.chiselmon.util.format.PokemonFormats;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

import static cc.turtl.chiselmon.util.format.ComponentUtils.createComponent;

public class MessageAction implements AlertAction {

    private static Component buildAlertMessage(AlertContext ctx) {
        Pokemon pokemon = ctx.pokemon();
        RuntimeFilter filter = ctx.messageFilter();

        // Alert prefix
        MutableComponent message = Component.empty()
                .withColor(ColorUtils.PINK.getRGB());
        message.append(Component.literal("⚠ ")
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/" + ChiselmonConstants.MOD_ID + " alert mute " + ctx.entity().getUUID()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("chiselmon.spawnalert.mute.tooltip")))));
        // Filter name
        message.append(filter.displayName());
        message.append(Component.literal(" • ")
                .withColor(ColorUtils.DARK_GRAY.getRGB()));
        // Pokemon name/details
        message.append(Component.literal(pokemon.getSpecies().getName()));
        if (pokemon.getForm() != pokemon.getSpecies().getStandardForm()) {
            message.append(Component.literal("-" + pokemon.getForm().getName()));
        }
        if (pokemon.getShiny()) {
            message.append(Component.literal(" ★")
                    .withColor(ColorUtils.GOLD.getRGB()));
        }
        if (pokemon.getScaleModifier() != 1.0f) {
            message.append(Component.literal(" (" + String.format("%.2f", pokemon.getScaleModifier()) + ")")
                    .withColor(ColorUtils.TEAL.getRGB()));
        }
        // Alert suffix
        message.append(Component.literal("spawned nearby!"));
        // Coords
        message.append(createComponent(" (" + ctx.entity().getOnPos().toShortString() + ")", ColorUtils.AQUA.getRGB()));

        return message;
    }

    @Override
    public void execute(AlertContext ctx) {
        if (!ctx.shouldMessage()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || ctx.isMuted()) {
            return;
        }

        Component message = buildAlertMessage(ctx);
        client.player.sendSystemMessage(message);
    }
}