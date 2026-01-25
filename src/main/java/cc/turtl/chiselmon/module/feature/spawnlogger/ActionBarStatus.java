package cc.turtl.chiselmon.module.feature.spawnlogger;

import static cc.turtl.chiselmon.util.ComponentUtil.colored;

import cc.turtl.chiselmon.util.ColorUtil;
import cc.turtl.chiselmon.util.ComponentUtil;
import cc.turtl.chiselmon.util.StringFormats;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ActionBarStatus {

    public static void updateActionBar(SpawnLoggerSession session) {

        int totalSpawns = session.getLoggedAmount();

        MutableComponent message = colored(ComponentUtil.modTranslatable("spawnlogger.action_bar.title"),
                ColorUtil.AQUA);
        message.append(colored(String.valueOf(totalSpawns), ColorUtil.CORAL));
        message.append(colored(ComponentUtil.modTranslatable("spawnlogger.action_bar.spawns"), ColorUtil.CORAL));
        message.append(colored(StringFormats.formatDurationMs(session.getElapsedMs()), ColorUtil.GREEN));
        message.append(colored(ComponentUtil.modTranslatable("spawnlogger.action_bar.elapsed"), ColorUtil.GREEN));

        if (session.isPaused()) {
            message.append(colored(ComponentUtil.modTranslatable("spawnlogger.action_bar.paused"), ColorUtil.YELLOW));
        }

        setActionBarStatus(message);
    }

    public static void setActionBarStatus(Component message) {
        Minecraft.getInstance().gui.setOverlayMessage(message, false);
    }
}