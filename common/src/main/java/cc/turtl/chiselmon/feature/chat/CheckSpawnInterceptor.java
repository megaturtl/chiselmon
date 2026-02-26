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

    // Handles names like Mr. Mime, Wo-Chien, Flutter Mane
    private static final Pattern ENTRY_PATTERN = Pattern.compile(
            "([A-Z][\\p{L}0-9\\s.\\-']+):\\s*([\\d.]+%)[,;]?"
    );

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

        if (!matcher.find()) {
            messagesRemaining--;
            return null;
        }
        matcher.reset();
        messagesRemaining--;

        MutableComponent result = Component.empty();
        int lastEnd = 0;

        while (matcher.find()) {
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
        // Clean "Mr. Mime" or "Flutter Mane" into "mrmime" or "fluttermane"
        String key = speciesName.toLowerCase().replaceAll("[^a-z0-9]", "");
        ClientSpecies species = ClientSpeciesRegistry.get(key);

        MutableComponent hover = Component.empty()
                .append(Component.literal(speciesName + ": "))
                .append(Component.literal(percentage).withColor(percentageColor(percentage))
                        .append(Component.literal("\n"))
                        .append(ComponentUtils.labelled(
                                Component.translatable("chiselmon.ui.label.ev_yield"),
                                PokemonFormats.evYield(species)))
                        .append(Component.literal("\n"))
                        .append(ComponentUtils.labelled(
                                Component.translatable("chiselmon.ui.label.egg_groups"),
                                PokemonFormats.eggGroups(species))));

        return Component.empty()
                .append(Component.literal(speciesName + ": ")
                        .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))))
                .append(Component.literal(percentage)
                        .withStyle(Style.EMPTY
                                .withColor(percentageColor(percentage))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))));
    }

    private static int percentageColor(String percentage) {
        try {
            float value = Float.parseFloat(percentage.replace("%", ""));
            if (value >= 5f) return ColorUtils.GREEN.getRGB();
            if (value >= 0.5f) return ColorUtils.YELLOW.getRGB();
            return ColorUtils.RED.getRGB();
        } catch (NumberFormatException e) {
            return ColorUtils.WHITE.getRGB();
        }
    }
}