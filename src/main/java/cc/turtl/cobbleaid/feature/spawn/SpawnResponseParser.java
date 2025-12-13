package cc.turtl.cobbleaid.feature.spawn;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SpawnResponseParser {
    private static final Pattern ENTRY_PATTERN = Pattern.compile("\\s*([^:,]+):\\s*([\\d.,]+)%\\s*");

    private SpawnResponseParser() {
    }

    public static List<SpawnEntry> parse(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }

        List<SpawnEntry> entries = new ArrayList<>();
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }

            String[] segments = line.split(",");
            for (String segment : segments) {
                Matcher matcher = ENTRY_PATTERN.matcher(segment);
                if (!matcher.matches()) {
                    continue;
                }

                String name = matcher.group(1).trim();
                String percentText = matcher.group(2).replace(",", "").trim();

                try {
                    float percentage = Float.parseFloat(percentText);
                    entries.add(new SpawnEntry(name, percentage));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return entries;
    }
}
