package cc.turtl.chiselmon.feature.spawnalert;

import static cc.turtl.chiselmon.util.ComponentUtil.colored;
import static cc.turtl.chiselmon.util.ComponentUtil.modTranslatable;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.predicate.PokemonPredicates;
import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.ComponentUtil;
import cc.turtl.chiselmon.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class AlertMessage {
    public static void sendChatAlert(PokemonEntity entity, boolean showForm) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        Component message = buildAlertMessage(entity, showForm);
        client.player.sendSystemMessage(message);
    }

    private static Component buildAlertMessage(PokemonEntity entity, boolean showForm) {
        Pokemon pokemon = entity.getPokemon();
        String speciesName = pokemon.getSpecies().getName();

        MutableComponent message = colored("⚠ ", ColorUtil.CORAL)
                .withStyle(style -> style
                        .withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/" + ChiselmonConstants.MODID + " alert mute " + entity.getUUID().toString()))
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
        if (PokemonPredicates.IS_LEGENDARY.test(pokemon)) {
            message.append(colored(modTranslatable("spawnalert.message.legendary"), ColorUtil.MAGENTA));
        }
        if (PokemonPredicates.IS_MYTHICAL.test(pokemon)) {
            message.append(colored(modTranslatable("spawnalert.message.mythical"), ColorUtil.MAGENTA));
        }
        if (PokemonPredicates.IS_ULTRABEAST.test(pokemon)) {
            message.append(colored(modTranslatable("spawnalert.message.ultra_beast"), ColorUtil.BLUE));
        }
        if (PokemonPredicates.IS_PARADOX.test(pokemon)) {
            message.append(colored(modTranslatable("spawnalert.message.paradox"), ColorUtil.ORANGE));
        }
        if (PokemonPredicates.IS_EXTREME_SIZE.test(pokemon)) {
            message.append(
                    colored(" (" + StringUtils.formatDecimal(pokemon.getScaleModifier()) + ")", ColorUtil.TEAL));
        }

        message.append(colored(modTranslatable("spawnalert.message.spawned_nearby"), ColorUtil.CORAL));
        message.append(colored("(" + entity.getOnPos().toShortString() + ")", ColorUtil.AQUA));

        return message;
    }
}
