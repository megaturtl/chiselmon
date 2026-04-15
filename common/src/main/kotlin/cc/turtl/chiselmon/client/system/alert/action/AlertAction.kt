package cc.turtl.chiselmon.client.system.alert.action

import cc.turtl.chiselmon.client.system.alert.AlertContext

fun interface AlertAction {
    fun execute(ctx: AlertContext)
}
