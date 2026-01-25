package cc.turtl.chiselmon.module.feature.spawnlogger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import cc.turtl.chiselmon.Chiselmon;
import net.minecraft.client.Minecraft;

public class CsvExporter {
    
    private static final String CSV_HEADER = "Species,Form,Level,Shiny,Special,Extreme Size,Scale Modifier,Snack Spawn,X,Y,Z,Dimension,Biome,Timestamp";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    public static Path exportSession(SpawnLoggerSession session) throws IOException {
        Path exportDir = getExportDirectory();
        Files.createDirectories(exportDir);
        
        String filename = "spawn_log_" + DATE_FORMAT.format(new Date()) + ".csv";
        Path filePath = exportDir.resolve(filename);
        
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(CSV_HEADER);
            writer.newLine();
            
            Collection<LoggedPokemon> logs = session.getResults();
            for (LoggedPokemon pokemon : logs) {
                writer.write(formatPokemonAsCsv(pokemon));
                writer.newLine();
            }
        }
        
        Chiselmon.getLogger().info("Exported spawn log to: " + filePath);
        return filePath;
    }
    
    private static String formatPokemonAsCsv(LoggedPokemon pokemon) {
        return String.join(",",
            escapeCsvField(pokemon.species()),
            escapeCsvField(pokemon.form()),
            String.valueOf(pokemon.level()),
            String.valueOf(pokemon.isShiny()),
            String.valueOf(pokemon.isSpecial()),
            String.valueOf(pokemon.isExtremeSize()),
            String.valueOf(pokemon.scaleModifier()),
            String.valueOf(pokemon.snackSpawn()),
            String.valueOf(pokemon.x()),
            String.valueOf(pokemon.y()),
            String.valueOf(pokemon.z()),
            escapeCsvField(pokemon.dimension()),
            escapeCsvField(pokemon.biome()),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(pokemon.timeLoggedMs()))
        );
    }
    
    private static String escapeCsvField(String field) {
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
    
    private static Path getExportDirectory() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("chiselmon").resolve("spawn_logs");
    }
}