package cc.turtl.chiselmon.feature.chat;

import cc.turtl.chiselmon.api.event.ChiselmonEvents;
import cc.turtl.chiselmon.api.species.ClientSpecies;
import cc.turtl.chiselmon.api.species.ClientSpeciesRegistry;
import cc.turtl.chiselmon.config.ChiselmonConfig;
import cc.turtl.chiselmon.config.category.GeneralConfig;
import cc.turtl.chiselmon.util.format.ColorUtils;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import cc.turtl.chiselmon.util.format.PokemonFormats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckSpawnInterceptor {

    // Matches strings like "Sandy Shocks: 10.87%," (handles multi-word!!)
    private static final Pattern ENTRY_PATTERN = Pattern.compile(
            "([A-Z][a-zA-Z]+(?:\\s[A-Z][a-zA-Z]+)*):\\s*([\\d.]+%)[,;]?"
    );

    // How many subsequent messages to watch after /checkspawn is sent
    private static final int WATCH_WINDOW = 3;
    private static int messagesRemaining = 0;

    public static void init() {
        ChiselmonEvents.COMMAND_SENT.subscribe(e -> {
            GeneralConfig config = ChiselmonConfig.get().general;
            if (!config.modDisabled && config.checkSpawnDetail && e.commandString().startsWith("checkspawn")) {
                messagesRemaining = WATCH_WINDOW;
            }
        });

        ChiselmonEvents.MESSAGE_RECEIVED.subscribe(e -> {
            GeneralConfig config = ChiselmonConfig.get().general;
            if (!config.modDisabled && config.checkSpawnDetail) {
                Component intercepted = tryIntercept(e.getMessage());
                if (intercepted != null) e.setMessage(intercepted);
            }
        });
    }

    private static Component tryIntercept(Component original) {
        if (messagesRemaining <= 0) return null;

        String raw = original.getString();
        Matcher matcher = ENTRY_PATTERN.matcher(raw);

        // This isn't a checkspawn result - burn a watch slot and bail
        if (!matcher.find()) {
            messagesRemaining--;
            return null;
        }
        matcher.reset();
        messagesRemaining--;

        MutableComponent result = Component.empty();
        int lastEnd = 0;

        while (matcher.find()) {
            // Preserve any literal text between entries (commas, spaces, etc.)
            if (matcher.start() > lastEnd) {
                result.append(Component.literal(raw.substring(lastEnd, matcher.start())));
            }
            result.append(buildEntry(matcher.group(1), matcher.group(2)));
            lastEnd = matcher.end();
        }

        if (lastEnd < raw.length()) {
            result.append(Component.literal(raw.substring(lastEnd)));
        }

        return result;
    }

    private static Component buildEntry(String speciesName, String percentage) {
        ClientSpecies species = ClientSpeciesRegistry.get(speciesName.toLowerCase().replace(" ", "_"));

        MutableComponent hover = Component.empty()
                .append(Component.literal(speciesName))
                .append(Component.literal("\n"))
                .append(ComponentUtils.labelled(
                        Component.translatable("chiselmon.ui.label.ev_yield"),
                        PokemonFormats.evYield(species)))
                .append(Component.literal("\n"))
                .append(ComponentUtils.labelled(
                        Component.translatable("chiselmon.ui.label.egg_groups"),
                        PokemonFormats.eggGroups(species)));

        // Name stays white, only the percentage is colored
        return Component.empty()
                .append(Component.literal(speciesName + ": ")
                        .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))))
                .append(Component.literal(percentage)
                        .withStyle(Style.EMPTY
                                .withColor(percentageColor(percentage))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))));
    }

    // Mirrors Cobblemon's green/yellow/red coloring by percentage threshold
    private static int percentageColor(String percentage) {
        try {
            float value = Float.parseFloat(percentage.replace("%", ""));
            if (value >= 5f) return ColorUtils.GREEN.getRGB();
            if (value >= 1f) return ColorUtils.YELLOW.getRGB();
            return ColorUtils.RED.getRGB();
        } catch (NumberFormatException e) {
            return ColorUtils.WHITE.getRGB();
        }
    }
}