package cc.turtl.chiselmon.system.alert.action;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.system.alert.AlertContext;
import cc.turtl.chiselmon.util.format.ColorUtils;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import static cc.turtl.chiselmon.util.format.ComponentUtils.createComponent;

public class MessageAction implements AlertAction {

    private static Component buildAlertMessage(AlertContext ctx) {
        Pokemon pokemon = ctx.pokemon();
        String speciesName = pokemon.getSpecies().getName();

        // Alert Emoji + Mute Click Event
        MutableComponent message = createComponent("⚠ ", ColorUtils.CORAL.getRGB())
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/" + ChiselmonConstants.MOD_ID + " alert mute " + ctx.entity().getUUID()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("chiselmon.spawnalert.mute.tooltip"))));
        // Species Name
        message.append(createComponent(speciesName, ColorUtils.CORAL.getRGB()));
        // Optional Form
        if (ctx.config().showFormInMessage) {
            String formName = pokemon.getForm().getName();
            if (!formName.trim().equalsIgnoreCase("normal")) {
                message.append(createComponent("-" + formName, ColorUtils.CORAL.getRGB()));
            }
        }
        // The filter match this alert fired for
        message.append(Component.literal(" ["));
        message.append(ctx.filter().displayName());
        message.append(Component.literal("] "));
        // Suffix
        message.append(Component.translatable("chiselmon.spawnalert.message.spawned_nearby").withStyle(s -> s.withColor(ColorUtils.CORAL.getRGB())));
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