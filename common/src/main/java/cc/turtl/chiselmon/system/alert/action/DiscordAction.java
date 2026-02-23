package cc.turtl.chiselmon.system.alert.action;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.api.filter.RuntimeFilter;
import cc.turtl.chiselmon.system.alert.AlertContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class DiscordAction implements AlertAction {

    @Override
    public void execute(AlertContext ctx) {
        if (!ctx.shouldDiscord()) return;

        JsonArray embeds = new JsonArray();
        embeds.add(buildDiscordEmbed(ctx));

        JsonObject body = new JsonObject();
        body.add("embeds", embeds);

        Thread.ofVirtual().start(() -> {
            try {
                URL url = URI.create(ctx.discordWebhookUrl()).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("User-Agent", "Chiselmon/1.0");
                conn.setDoOutput(true);

                byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                conn.setFixedLengthStreamingMode(bytes.length);
                try (var os = conn.getOutputStream()) {
                    os.write(bytes);
                }

                int status = conn.getResponseCode();
                if (status >= 200 && status < 300) {
                    ChiselmonConstants.LOGGER.debug("Discord webhook response: {}", status);
                } else {
                    String errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                    ChiselmonConstants.LOGGER.warn("Discord webhook returned {}: {}", status, errorBody);
                }
                conn.disconnect();
            } catch (Exception e) {
                ChiselmonConstants.LOGGER.warn("Failed to send Discord notification", e);
            }
        });
    }

    private JsonObject buildDiscordEmbed(AlertContext ctx) {
        JsonObject embed = new JsonObject();

        RuntimeFilter filter = ctx.discordFilter();
        String username = Minecraft.getInstance().getUser().getName();
        String pokemonName = ctx.pokemon().getSpecies().getName();
        String filterName = filter.name();

        // Author
        JsonObject author = new JsonObject();
        author.addProperty("name", "🚨 Spawn Alert for @" + username);
        embed.add("author", author);

        // Title
        embed.addProperty("title", String.format("%s matched filter %s!", pokemonName, filterName));
        embed.addProperty("color", filter.rgb() & 0xFFFFFF);

        // Thumbnail image
        String spriteUrl = String.format(
                "https://play.pokemonshowdown.com/sprites/%s/%s.gif",
                ctx.encounter().isShiny() ? "ani-shiny" : "ani",
                pokemonName.toLowerCase()
        );

        JsonObject thumbnail = new JsonObject();
        thumbnail.addProperty("url", spriteUrl);
        embed.add("thumbnail", thumbnail);

        // Fields
        JsonArray fields = new JsonArray();

        JsonObject locationField = new JsonObject();
        locationField.addProperty("name", "📍 Location");
        locationField.addProperty("value", String.format("%d, %d, %d", ctx.encounter().pokemonX(), ctx.encounter().pokemonY(), ctx.encounter().pokemonZ()));
        locationField.addProperty("inline", true);
        fields.add(locationField);

        JsonObject biomeField = new JsonObject();
        biomeField.addProperty("name", "🏞️ Biome");
        biomeField.addProperty("value", ctx.encounter().biome());
        biomeField.addProperty("inline", true);
        fields.add(biomeField);

        JsonObject timeField = new JsonObject();
        timeField.addProperty("name", "🕐 Time");
        timeField.addProperty("value", String.format("<t:%d:R>", Instant.now().getEpochSecond()));
        timeField.addProperty("inline", false);
        fields.add(timeField);

        embed.add("fields", fields);

        // Footer
        JsonObject footer = new JsonObject();
        footer.addProperty("text", "Sent using " + ChiselmonConstants.MOD_NAME + " by " + ChiselmonConstants.AUTHOR);
        embed.add("footer", footer);

        return embed;
    }
}