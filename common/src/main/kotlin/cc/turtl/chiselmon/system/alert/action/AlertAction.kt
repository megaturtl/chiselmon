package cc.turtl.chiselmon.system.alert.action

import cc.turtl.chiselmon.system.alert.AlertContext

fun interface AlertAction {
    fun execute(ctx: AlertContext)
}
