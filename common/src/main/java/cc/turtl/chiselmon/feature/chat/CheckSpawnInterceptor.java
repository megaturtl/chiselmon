package cc.turtl.chiselmon.feature.chat;

import cc.turtl.chiselmon.client.api.ClientSpecies;
import cc.turtl.chiselmon.client.api.ClientSpeciesRegistry;
import cc.turtl.chiselmon.client.config.ChiselmonConfig;
import cc.turtl.chiselmon.client.config.category.GeneralConfig;
import cc.turtl.chiselmon.util.format.ColorUtils;
import cc.turtl.chiselmon.util.format.ComponentUtils;
import cc.turtl.chiselmon.util.format.PokemonFormats;
import cc.turtl.turtlshell.api.client.ClientEvents;
import kotlin.Unit;
import net.minecraft.client.Minecraft;
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
        ClientEvents.INSTANCE.getCOMMAND_SENT().subscribe(e -> {
            GeneralConfig config = ChiselmonConfig.INSTANCE.getGeneral();
            if (!config.getModDisabled() && config.getCheckSpawnDetail() && e.startsWith("checkspawn")) {
                messagesRemaining = WATCH_WINDOW;
            }
            return Unit.INSTANCE;
        });

        ClientEvents.INSTANCE.getMESSAGE_RECEIVED().subscribe(message -> {
            GeneralConfig config = ChiselmonConfig.INSTANCE.getGeneral();
            if (!config.getModDisabled() && config.getCheckSpawnDetail()) {
                Component intercepted = tryIntercept(message);
                if (intercepted != null) {
                    // send the modified message manually, cancel the original
                    Minecraft.getInstance().gui.getChat().addMessage(intercepted);
                    return true;
                }
            }
            return false;
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