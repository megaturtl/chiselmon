package cc.turtl.chiselmon.feature.spawnalert;

import static cc.turtl.chiselmon.util.ComponentUtil.colored;
import static cc.turtl.chiselmon.util.ComponentUtil.modTranslatable;

import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.ComponentUtil;
import cc.turtl.chiselmon.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class AlertMessage {
    public static void sendChatAlert(LoadedPokemonWrapper alerting, boolean showForm) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        Component message = buildAlertMessage(alerting, showForm);
        client.player.sendSystemMessage(message);
    }

    private static Component buildAlertMessage(LoadedPokemonWrapper alerting, boolean showForm) {
        Pokemon pokemon = alerting.entity.getPokemon();
        String speciesName = pokemon.getSpecies().getName();

        MutableComponent message = colored("⚠ ", ColorUtil.CORAL)
                .withStyle(style -> style
                        .withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/" + ChiselmonConstants.MODID + " alert mute " + alerting.entity.getUUID().toString()))
                        .withHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        ComponentUtil.modTranslatable("spawnalert.mute.tooltip"))));

        message.append(colored(speciesName, ColorUtil.CORAL));

        if (showForm) {
            String formName = pokemon.getForm().getName();

            if (!formName.trim().equalsIgnoreCase("normal")) {
                message.append(colored("-" + formName, ColorUtil.CORAL));
            }
        }

        if (alerting.alertTypes.contains(AlertType.SHINY)) {
            message.append(colored(modTranslatable("spawnalert.message.shiny"), ColorUtil.GOLD));
        }
        if (alerting.alertTypes.contains(AlertType.LEGENDARY)) {
            message.append(colored(modTranslatable("spawnalert.message.legendary"), ColorUtil.MAGENTA));
        }
        if (alerting.alertTypes.contains(AlertType.SIZE)) {
            message.append(
                    colored(" (" + StringUtils.formatDecimal(pokemon.getScaleModifier()) + ")", ColorUtil.TEAL));
        }

        message.append(colored(modTranslatable("spawnalert.message.spawned_nearby"), ColorUtil.CORAL));
        message.append(colored("(" + alerting.entity.getOnPos().toShortString() + ")", ColorUtil.AQUA));

        return message;
    }
}
