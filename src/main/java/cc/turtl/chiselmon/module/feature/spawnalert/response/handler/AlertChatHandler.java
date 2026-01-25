package cc.turtl.chiselmon.module.feature.spawnalert.response.handler;

import static cc.turtl.chiselmon.util.ComponentUtil.colored;
import static cc.turtl.chiselmon.util.ComponentUtil.modTranslatable;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.module.feature.spawnalert.AlertLevel;
import cc.turtl.chiselmon.module.feature.spawnalert.SpawnAlertConfig;
import cc.turtl.chiselmon.module.feature.spawnalert.response.AlertResponse;
import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.ComponentUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class AlertChatHandler {
    public static void handle(AlertResponse response, SpawnAlertConfig config) {
        if (response.chatLevel() == null || response.chatLevel() == AlertLevel.NONE || !response.chatLevel().shouldChat(config)) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        Component message = buildAlertMessage(response.pe(), config.showFormInMessage);
        client.player.sendSystemMessage(message);
    }

    private static Component buildAlertMessage(PokemonEntity pe, boolean showForm) {
        Pokemon pokemon = pe.getPokemon();
        String speciesName = pokemon.getSpecies().getName();

        MutableComponent message = colored("⚠ ", ColorUtil.CORAL)
                .withStyle(style -> style
                        .withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/" + ChiselmonConstants.MODID + " alert mute " + pe.getUUID().toString()))
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

        if (PokemonPredicates.IS_SHINY.test(pokemon)) {
            message.append(colored(modTranslatable("spawnalert.message.shiny"), ColorUtil.GOLD));
        }
        if (PokemonPredicates.IS_SPECIAL.test(pokemon)) {
            message.append(colored(modTranslatable("spawnalert.message.legendary"), ColorUtil.MAGENTA));
        }
        if (PokemonPredicates.IS_EXTREME_SIZE.test(pokemon)) {
            message.append(
                    colored(" (" + String.format("%.2f", pokemon.getScaleModifier()) + ")", ColorUtil.TEAL));
        }

        message.append(colored(modTranslatable("spawnalert.message.spawned_nearby"), ColorUtil.CORAL));
        message.append(colored("(" + pe.getOnPos().toShortString() + ")", ColorUtil.AQUA));

        return message;
    }
}
