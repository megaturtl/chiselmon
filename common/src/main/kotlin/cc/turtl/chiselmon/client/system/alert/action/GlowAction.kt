package cc.turtl.chiselmon.client.system.alert.action

import cc.turtl.chiselmon.client.util.addGlow
import cc.turtl.chiselmon.client.util.highlightNickname
import cc.turtl.chiselmon.client.system.alert.AlertContext

class GlowAction : AlertAction {
    override fun execute(ctx: AlertContext) {
        val filter = ctx.highlightFilter ?: return
        if (!ctx.shouldHighlight) return
        ctx.entity.addGlow(filter.rgb)
        ctx.entity.highlightNickname(filter.rgb)
    }
}
