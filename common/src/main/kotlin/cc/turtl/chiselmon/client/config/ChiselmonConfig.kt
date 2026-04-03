package cc.turtl.chiselmon.client.config

import cc.turtl.chiselmon.client.ChiselmonKeybindsKt
import cc.turtl.turtlshell.api.client.ClientEvents
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.gui.YACLScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

object ChiselmonConfig {

    val general get() = ChiselmonConfigHandler.general
    val pc get() = ChiselmonConfigHandler.pc
    val alert get() = ChiselmonConfigHandler.alert
    val recorder get() = ChiselmonConfigHandler.recorder
    val filter get() = ChiselmonConfigHandler.filter

    fun init() {
        ChiselmonConfigHandler.load()
        ClientEvents.TICK_POST.subscribe {
            val client = Minecraft.getInstance()
            while (ChiselmonKeybindsKt.OPEN_CONFIG.consumeClick()) {
                val currentScreen = client.screen
                client.execute { client.setScreen(createScreen(currentScreen)) }
            }
        }
    }

    fun save() = ChiselmonConfigHandler.save()

    fun createScreen(parent: Screen?): Screen =
        YetAnotherConfigLib.createBuilder()
            .title(Component.translatable("chiselmon.config.title"))
            .category(general.buildCategory())
            .category(pc.buildCategory())
            .category(filter.buildCategory(parent))
            .category(alert.buildCategory())
            .category(recorder.buildCategory())
            .save(::save)
            .build()
            .generateScreen(parent)

    /**
     * Saves config and recreates the screen to reflect changes.
     * Takes a tab index to switch back to that tab after reload.
     * This is a workaround since YACL doesn't support in-place refresh.
     */
    fun saveAndReloadScreen(parent: Screen?, tabIndex: Int) {
        save()
        openAtTab(parent, tabIndex)
    }

    fun openAtTab(parent: Screen?, tabIndex: Int) {
        val newScreen = createScreen(parent) as YACLScreen
        Minecraft.getInstance().setScreen(newScreen)
        newScreen.tabNavigationBar?.let { bar ->
            if (tabIndex in 0 until bar.tabs.size) bar.selectTab(tabIndex, false)
        }
    }
}