package cc.turtl.cobbleaid.feature.spawnlogger;

import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import cc.turtl.cobbleaid.util.ColorUtil;
import cc.turtl.cobbleaid.util.StringUtils;

import static cc.turtl.cobbleaid.util.ComponentFormatUtil.colored;
import static cc.turtl.cobbleaid.util.ComponentFormatUtil.labelledValue;

public class ResultsMessage {

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
        long elapsedMs = session.getElapsedMs();
        int totalSpawns = session.getLoggedAmount();
        double spawnsPerMinute = calculateSpawnsPerMinute(elapsedMs, totalSpawns);

        MutableComponent message = colored("=== Spawn Logger Report ===", ColorUtil.AQUA);
        appendSessionStats(message, elapsedMs, totalSpawns, spawnsPerMinute);
        appendSpecialEncounters(message, logs);

        return message;
    }

    private static double calculateSpawnsPerMinute(long elapsedMs, int totalSpawns) {
        return elapsedMs > 0 ? (totalSpawns * 60.0 * 1000.0) / elapsedMs : 0.0;
    }

    private static void appendSessionStats(MutableComponent message, long durationMs, int totalSpawns,
            double spawnsPerMinute) {
        message.append(labelledValue("\nTime Elapsed: ", StringUtils.formatDurationMs(durationMs)));
        message.append(labelledValue("\nTotal Spawns: ", totalSpawns));
        message.append(labelledValue("\nSpawns/Min: ", StringUtils.formatDecimal(spawnsPerMinute)));
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
        message.append(colored("Lvl. " + pokemon.level() + " " + pokemon.species(), ColorUtil.AQUA));

        if (pokemon.isShiny()) {
            message.append(colored(" (Shiny)", ColorUtil.GOLD));
        }
        if (pokemon.isExtremeSize()) {
            message.append(colored(" (" + StringUtils.formatDecimal(pokemon.scaleModifier())  + ")", ColorUtil.TEAL));
        }
    }
}
