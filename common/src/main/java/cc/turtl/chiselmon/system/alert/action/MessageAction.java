package cc.turtl.chiselmon.system.alert.action;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.filter.RuntimeFilter;
import cc.turtl.chiselmon.platform.PlatformServices;
import cc.turtl.chiselmon.system.alert.AlertContext;
import cc.turtl.chiselmon.util.format.ColorUtils;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class MessageAction implements AlertAction {

    private static Component buildAlertMessage(AlertContext ctx) {
        Pokemon pokemon = ctx.pokemon();
        RuntimeFilter filter = ctx.messageFilter();

        // Alert prefix
        MutableComponent message = Component.literal("⚠ ")
                .withStyle(style -> style
                        .withColor(ColorUtils.PINK.getRGB())
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/" + ChiselmonConstants.MOD_ID + " alert mute " + ctx.entity().getUUID()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("chiselmon.spawnalert.mute.tooltip"))));
        // Filter name
        message.append(Component.empty()
                .append(filter.displayName())
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/" + ChiselmonConstants.MOD_ID + " config alert"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("chiselmon.spawnalert.filter.tooltip")))));

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
        message.append(Component.literal(" spawned nearby!"));
        // Coords
        BlockPos pos = ctx.entity().getOnPos();
        MutableComponent coordsComponent = Component.literal(" (" + pos.toShortString() + ")")
                .withColor(ColorUtils.AQUA.getRGB());

        // only if xaeros is installed and loaded
        if (PlatformServices.getModChecker().isLoaded("xaerominimap")) {
            char mcColor = ColorUtils.legacy(ctx.messageFilter().rgb()).getChar();
            int colorIndex = Character.digit(mcColor, 16);
            String dimension = ctx.entity().level().dimension().location().toString().replace(":", "$");
            // this commands gets intercepted by xaeros to bring up the waypoint screen
            String waypointCommand = "/xaero_waypoint_add:" + pokemon.getSpecies().getName()
                    + ":!:" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ()
                    + ":" + colorIndex + ":false:0:Internal-dim%" + dimension + "-waypoints";

            coordsComponent.withStyle(style -> style
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            waypointCommand))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.translatable("chiselmon.spawnalert.waypoint.tooltip"))));
        }
        message.append(Component.empty().append(coordsComponent));

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