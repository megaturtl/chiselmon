package cc.turtl.cobbleaid.feature.spawnalert;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import cc.turtl.cobbleaid.api.predicate.PokemonPredicates;
import cc.turtl.cobbleaid.util.ColorUtil;
import cc.turtl.cobbleaid.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import static cc.turtl.cobbleaid.util.ComponentFormatUtil.colored;

public class AlertMessage {
    public static void sendChatAlert(PokemonEntity entity) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        Component message = buildAlertMessage(entity);
        client.player.sendSystemMessage(message);
    }

    private static Component buildAlertMessage(PokemonEntity entity) {
        Pokemon pokemon = entity.getPokemon();
        String speciesName = pokemon.getSpecies().getName();

        MutableComponent message = colored("⚠ ", ColorUtil.CORAL)
                .withStyle(style -> style
                        .withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/cobbleaid alert mute " + entity.getUUID().toString()))
                        .withHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.literal("Click to mute this Pokemon"))));

        message.append(colored(speciesName, ColorUtil.CORAL));

        if (PokemonPredicates.IS_SHINY.test(pokemon)) {
            message.append(colored(" (Shiny)", ColorUtil.GOLD));
        }
        if (PokemonPredicates.IS_LEGENDARY.test(pokemon)) {
            message.append(colored(" (Legendary)", ColorUtil.MAGENTA));
        }
        if (PokemonPredicates.IS_ULTRABEAST.test(pokemon)) {
            message.append(colored(" (Ultra Beast)", ColorUtil.BLUE));
        }
        if (PokemonPredicates.IS_PARADOX.test(pokemon)) {
            message.append(colored(" (Paradox)", ColorUtil.ORANGE));
        }
        if (PokemonPredicates.IS_EXTREME_SIZE.test(pokemon)) {
            message.append(
                    colored(" (" + StringUtils.formatDecimal(pokemon.getScaleModifier()) + ")", ColorUtil.TEAL));
        }

        message.append(colored(" spawned nearby! ", ColorUtil.CORAL));
        message.append(colored("(" + entity.getOnPos().toShortString() + ")", ColorUtil.AQUA));

        return message;
    }
}
