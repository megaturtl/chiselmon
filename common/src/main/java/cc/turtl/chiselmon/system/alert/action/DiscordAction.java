package cc.turtl.chiselmon.system.alert.action;

import cc.turtl.chiselmon.ChiselmonConstants;
import cc.turtl.chiselmon.system.alert.AlertContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class DiscordAction implements AlertAction {
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @Override
    public void execute(AlertContext ctx) {
        if (!ctx.shouldDiscord()) return;

        String pokemonName = ctx.pokemon().getSpecies().getName();
        String filterName = ctx.filter().name();
        boolean isShiny = ctx.pokemon().getShiny();

        BlockPos pos = ctx.entity().blockPosition();
        String biome = ctx.entity().level().getBiome(pos).unwrapKey()
                .map(key -> key.location().getPath())
                .orElse("unknown");

        String spriteUrl = String.format(
                "https://play.pokemonshowdown.com/sprites/%s/%s.gif",
                isShiny ? "ani-shiny" : "ani",
                pokemonName.toLowerCase()
        );

        long epoch = Instant.now().getEpochSecond();

        JsonObject embed = new JsonObject();
        embed.addProperty("title", String.format("🚨 %s matched filter %s!", pokemonName, filterName));
        embed.addProperty("description", String.format(
                "📍 **%d, %d, %d** — %s\n🕐 <t:%d:R>",
                pos.getX(), pos.getY(), pos.getZ(), biome, epoch
        ));
        embed.addProperty("color", ctx.filter().rgb());

        JsonObject image = new JsonObject();
        image.addProperty("url", spriteUrl);
        embed.add("image", image);

        JsonArray embeds = new JsonArray();
        embeds.add(embed);

        JsonObject body = new JsonObject();
        body.add("embeds", embeds);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ctx.discordWebhookUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();

        Thread.ofVirtual().start(() -> {
            try {
                HTTP.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception e) {
                ChiselmonConstants.LOGGER.warn("Failed to send Discord notification", e);
            }
        });
    }
}