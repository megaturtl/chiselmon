package cc.turtl.chiselmon.client.system.alert.action

import cc.turtl.chiselmon.BuildDetails
import cc.turtl.chiselmon.core.util.format.createComponent
import cc.turtl.chiselmon.client.system.alert.AlertContext
import cc.turtl.turtlshell.api.core.Platform
import cc.turtl.turtlshell.api.core.format.ColorLib
import cc.turtl.turtlshell.api.core.util.getClosestLegacy
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent

class MessageAction : AlertAction {

    override fun execute(ctx: AlertContext) {
        if (!ctx.shouldMessage) return
        val client = Minecraft.getInstance()
        if (client.player == null || ctx.isMuted) return

        client.player!!.sendSystemMessage(buildAlertMessage(ctx))
    }

    private fun buildAlertMessage(ctx: AlertContext): Component {
        val pokemon = ctx.pokemon
        val filter = ctx.messageFilter ?: return Component.empty()

        val message: MutableComponent = Component.literal("⚠ ")
            .withStyle { style ->
                style.withColor(ColorLib.PINK.rgb)
                    .withClickEvent(
                        ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/${BuildDetails.MOD_ID} alert mute ${ctx.entity.uuid}"
                        )
                    )
                    .withHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.translatable("chiselmon.spawnalert.mute.tooltip")
                        )
                    )
            }

        // Filter name
        message.append(
            Component.empty()
                .append(filter.displayName())
                .withStyle { style ->
                    style.withClickEvent(
                        ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/${BuildDetails.MOD_ID} config alert"
                        )
                    )
                        .withHoverEvent(
                            HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("chiselmon.spawnalert.filter.tooltip")
                            )
                        )
                }
        )

        message.append(createComponent(" • ", ColorLib.DARK_GRAY.rgb))

        // Pokemon name/details
        message.append(Component.literal(pokemon.species.name))
        if (pokemon.form != pokemon.species.standardForm) {
            message.append(Component.literal("-${pokemon.form.name}"))
        }
        if (pokemon.shiny) {
            message.append(createComponent(" ★", ColorLib.GOLD.rgb))
        }
        if (pokemon.scaleModifier != 1.0f) {
            message.append(createComponent(" (${"%.2f".format(pokemon.scaleModifier)})", ColorLib.TEAL.rgb))
        }

        // Alert suffix
        message.append(Component.literal(" spawned nearby!"))

        // Coords
        val pos = ctx.entity.onPos
        val coords: MutableComponent = createComponent(" (${pos.toShortString()})", ColorLib.AQUA.rgb)

        // only if xaeros is installed and loaded -- the command is intercepted by the mod to bring up the waypoint screen
        if (Platform.isModLoaded("xaerominimap")) {
            val mcColor = getClosestLegacy(filter.rgb).char
            val colorIndex = Character.digit(mcColor, 16)
            val dimension = ctx.entity.level().dimension().location().toString().replace(":", "$")
            val waypointCommand = "/xaero_waypoint_add:${pokemon.species.name}:!:" +
                    "${pos.x}:${pos.y}:${pos.z}:$colorIndex:false:0:Internal-dim%$dimension-waypoints"

            coords.withStyle { style ->
                style.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, waypointCommand))
                    .withHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.translatable("chiselmon.spawnalert.waypoint.tooltip")
                        )
                    )
            }
        }
        message.append(Component.empty().append(coords))

        return message
    }
}
