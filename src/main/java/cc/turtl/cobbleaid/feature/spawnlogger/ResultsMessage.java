package cc.turtl.cobbleaid.feature.spawnlogger;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.time.DurationFormatUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import cc.turtl.cobbleaid.util.ColorUtil;
import static cc.turtl.cobbleaid.util.ComponentFormatUtil.colored;
import static cc.turtl.cobbleaid.util.ComponentFormatUtil.labelledValue;

public class ResultsMessage {
    private static final String DURATION_FORMAT = "mm:ss";

    public static void sendResultsMessage(SpawnLoggerSession session) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        Component message = buildResultsMessage(session);
        client.player.sendSystemMessage(message);
    }

    private static MutableComponent buildResultsMessage(SpawnLoggerSession session) {
        Collection<LoggedPokemon> logs = session.getResults();
        long totalSeconds = session.getElapsedSeconds();
        int totalSpawns = logs.size();
        double spawnsPerMinute = calculateSpawnsPerMinute(totalSeconds, totalSpawns);

        MutableComponent message = colored("=== Spawn Logger Report ===", ColorUtil.TURQUOISE);
        appendSessionStats(message, totalSeconds, totalSpawns, spawnsPerMinute);
        appendSpecialEncounters(message, logs);

        return message;
    }

    private static double calculateSpawnsPerMinute(long totalSeconds, int totalSpawns) {
        return totalSeconds > 0 ? (totalSpawns * 60.0) / totalSeconds : 0.0;
    }

    private static void appendSessionStats(MutableComponent message, long totalSeconds, int totalSpawns,
            double spawnsPerMinute) {
        String timeString = DurationFormatUtils.formatDuration(totalSeconds * 1000, DURATION_FORMAT);
        message.append(labelledValue("\nTime Elapsed: ", timeString));
        message.append(labelledValue("\nTotal Spawns: ", totalSpawns));
        message.append(labelledValue("\nSpawns/Min: ", String.format("%.2f", spawnsPerMinute)));
    }

    private static void appendSpecialEncounters(MutableComponent message, Collection<LoggedPokemon> logs) {
        List<LoggedPokemon> specialLogs = logs.stream()
                .filter(p -> p.isShiny() || p.isSpecial() || p.isExtremeSize())
                .toList();

        if (specialLogs.isEmpty()) {
            return;
        }

        message.append(colored("\n\nSpecial Encounters:", ColorUtil.GOLD));
        specialLogs.forEach(pokemon -> appendPokemonEntry(message, pokemon));
    }

    private static void appendPokemonEntry(MutableComponent message, LoggedPokemon pokemon) {

        message.append(colored("\n- ", ColorUtil.LIGHT_GRAY));
        message.append(colored("Lvl. " + pokemon.level() + " " + pokemon.species(), ColorUtil.TURQUOISE));

        if (pokemon.isShiny()) {
            message.append(colored(" (Shiny)", ColorUtil.GOLD));
        }
        if (pokemon.isExtremeSize()) {
            message.append(colored(" [" + pokemon.scaleModifier() + "x]", ColorUtil.TURQUOISE));
        }
    }
}
