package cc.turtl.chiselmon.feature.spawnlogger;

import static cc.turtl.chiselmon.util.ComponentUtil.colored;
import static cc.turtl.chiselmon.module.feature.SpawnLoggerModule.EXPORT_COMMAND_PATH;

import java.util.Collection;
import java.util.List;

import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.ComponentUtil;
import cc.turtl.chiselmon.util.StringFormats;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class ResultsMessage {

    public static void sendResultsMessage(SpawnLoggerSession session) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        Component message = buildResultsMessage(session);
        client.player.sendSystemMessage(message);
    }

    private static double calculateSpawnsPerMinute(long elapsedMs, int totalSpawns) {
        return elapsedMs > 0 ? (totalSpawns * 60.0 * 1000.0) / elapsedMs : 0.0;
    }

    private static MutableComponent buildResultsMessage(SpawnLoggerSession session) {
        Collection<LoggedPokemon> logs = session.getResults();
        long elapsedMs = session.getElapsedMs();
        int totalSpawns = session.getLoggedAmount();
        double spawnsPerMinute = calculateSpawnsPerMinute(elapsedMs, totalSpawns);

        // Header
        MutableComponent message = colored(ComponentUtil.modTranslatable("spawnlogger.report.header"), ColorUtil.AQUA);

        // Stats
        appendSessionStats(message, elapsedMs, totalSpawns, spawnsPerMinute);
        appendSpecialEncounters(message, logs);
        appendExportButton(message);

        return message;
    }

    private static void appendSessionStats(MutableComponent message, long durationMs, int totalSpawns,
            double spawnsPerMinute) {
        message.append(colored(
                ComponentUtil.modTranslatable("spawnlogger.report.elapsed", StringFormats.formatDurationMs(durationMs)),
                ColorUtil.WHITE));
        message.append(colored(ComponentUtil.modTranslatable("spawnlogger.report.total_spawns", totalSpawns),
                ColorUtil.WHITE));
        message.append(colored(ComponentUtil.modTranslatable("spawnlogger.report.spawns_per_min",
                String.format("%.2f", spawnsPerMinute)), ColorUtil.WHITE));
    }

    private static void appendSpecialEncounters(MutableComponent message, Collection<LoggedPokemon> logs) {
        List<LoggedPokemon> specialLogs = logs.stream()
                .filter(p -> p.isShiny() || p.isSpecial() || p.isExtremeSize())
                .toList();

        if (specialLogs.isEmpty())
            return;

        message.append(colored(ComponentUtil.modTranslatable("spawnlogger.report.special_header"), ColorUtil.GOLD));
        specialLogs.forEach(pokemon -> appendPokemonEntry(message, pokemon));
    }

    private static void appendPokemonEntry(MutableComponent message, LoggedPokemon pokemon) {
        // Basic entry: "- Lvl. X Species"
        message.append(colored(
                ComponentUtil.modTranslatable("spawnlogger.report.pokemon_entry", pokemon.level(), pokemon.species()),
                ColorUtil.AQUA));

        if (pokemon.isShiny()) {
            message.append(colored(ComponentUtil.modTranslatable("spawnlogger.report.shiny_label"), ColorUtil.GOLD));
        }

        if (pokemon.isExtremeSize()) {
            // Since size is just a number in parens, we can keep it dynamic or add a lang
            // key if you want "Size: X"
            message.append(colored(" (" + String.format("%.2f", (pokemon.scaleModifier())) + ")", ColorUtil.TEAL));
        }
    }

    private static void appendExportButton(MutableComponent message) {
        MutableComponent exportButton = colored(ComponentUtil.modTranslatable("spawnlogger.report.export_button"),
                ColorUtil.GREEN)
                .setStyle(Style.EMPTY
                        .withColor(ColorUtil.GREEN)
                        .withBold(true)
                        .withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, EXPORT_COMMAND_PATH))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                colored(ComponentUtil.modTranslatable("spawnlogger.report.export_hover"),
                                        ColorUtil.YELLOW))));

        message.append(exportButton);
    }
}
