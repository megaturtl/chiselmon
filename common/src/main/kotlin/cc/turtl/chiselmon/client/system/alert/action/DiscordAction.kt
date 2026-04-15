package cc.turtl.chiselmon.client.system.alert.action

import cc.turtl.chiselmon.BuildDetails
import cc.turtl.chiselmon.core.ChiselmonConstants
import cc.turtl.chiselmon.core.util.normalizeSpeciesName
import cc.turtl.chiselmon.client.system.alert.AlertContext
import cc.turtl.chiselmon.core.api.PokemonEncounter
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*

class DiscordAction {

    /** Timestamps of recently-sent alerts, used for simple in-window rate limiting. */
    private val sentTimestamps = ArrayDeque<Long>()

    fun execute(ctx: AlertContext) {
        if (!ctx.shouldDiscord) return
        val encounter = PokemonEncounter.from(ctx.entity)
        if (!allowAlert()) {
            ChiselmonConstants.LOGGER.warn(
                "Discord alert suppressed: rate limit reached ({} per {}ms)", MAX_ALERTS, WINDOW_MS
            )
            return
        }

        val body = JsonObject().apply {
            add("embeds", JsonArray().apply { add(buildDiscordEmbed(ctx, encounter)) })
        }

        Thread.ofVirtual().start {
            try {
                val conn = URI.create(ctx.discordWebhookUrl).toURL().openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("User-Agent", "Chiselmon/1.0")
                conn.doOutput = true

                val bytes = body.toString().toByteArray(StandardCharsets.UTF_8)
                conn.setFixedLengthStreamingMode(bytes.size)
                conn.outputStream.use { it.write(bytes) }

                val status = conn.responseCode
                if (status in 200..299) {
                    ChiselmonConstants.LOGGER.debug("Discord webhook response: {}", status)
                } else {
                    val errorBody = conn.errorStream?.readAllBytes()?.toString(StandardCharsets.UTF_8).orEmpty()
                    ChiselmonConstants.LOGGER.warn("Discord webhook returned {}: {}", status, errorBody)
                }
                conn.disconnect()
            } catch (e: Exception) {
                ChiselmonConstants.LOGGER.warn("Failed to send Discord notification", e)
            }
        }
    }

    /**
     * Returns true and records the current timestamp if the alert is within
     * the allowed rate, false if it should be suppressed to stop spam.
     */
    @Synchronized
    private fun allowAlert(): Boolean {
        val now = System.currentTimeMillis()
        while (sentTimestamps.isNotEmpty() && now - sentTimestamps.peekFirst() >= WINDOW_MS) {
            sentTimestamps.pollFirst()
        }
        if (sentTimestamps.size >= MAX_ALERTS) return false
        sentTimestamps.addLast(now)
        return true
    }

    private fun buildDiscordEmbed(ctx: AlertContext, encounter: PokemonEncounter): JsonObject {
        val filter = ctx.discordFilter ?: return JsonObject()
        val username = Minecraft.getInstance().user.name
        val pokemonName = ctx.pokemon.species.name
        val urlSlug = normalizeSpeciesName(pokemonName)

        return JsonObject().apply {
            add("author", JsonObject().apply {
                addProperty("name", "\uD83D\uDEA8 Spawn Alert for @$username")
            })
            addProperty("title", "$pokemonName matched filter ${filter.name}!")
            addProperty("color", filter.rgb and 0xFFFFFF)

            // Thumbnail image
            val spriteUrl = "https://play.pokemonshowdown.com/sprites/" +
                    "${if (encounter.isShiny) "ani-shiny" else "ani"}/$urlSlug.gif"
            add("thumbnail", JsonObject().apply { addProperty("url", spriteUrl) })

            // Fields
            add("fields", JsonArray().apply {
                add(
                    embedField(
                        "📍 Location",
                        "${encounter.pokemonX}, ${encounter.pokemonY}, ${encounter.pokemonZ}",
                        true
                    )
                )
                add(embedField("🏞️ Biome", encounter.biome, true))
                add(embedField("🕐 Time", "<t:${Instant.now().epochSecond}:R>", false))
            })

            // Footer
            add("footer", JsonObject().apply {
                addProperty("text", "Sent using ${BuildDetails.MOD_DISPLAY_NAME} by ${BuildDetails.MOD_AUTHOR}")
            })
        }
    }

    private fun embedField(name: String, value: String, inline: Boolean): JsonObject = JsonObject().apply {
        addProperty("name", name)
        addProperty("value", value)
        addProperty("inline", inline)
    }

    companion object {
        // Max 2 alerts every 5 seconds to prevent spam but still allow double spawns to both register
        private const val MAX_ALERTS = 2
        private const val WINDOW_MS = 5_000L
    }
}
