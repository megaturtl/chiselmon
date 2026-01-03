package cc.turtl.cobbleaid.feature.spawnlogger;

import static cc.turtl.cobbleaid.util.ComponentFormatUtil.colored;

import cc.turtl.cobbleaid.util.ColorUtil;
import cc.turtl.cobbleaid.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ActionBarStatus {

    public static void updateActionBar(SpawnLoggerSession session) {

        int totalSpawns = session.getLoggedAmount();

        MutableComponent message = colored("Spawn Logger: ", ColorUtil.AQUA);
        message.append(colored(totalSpawns + " Spawns, ", ColorUtil.CORAL));
        message.append(colored(StringUtils.formatDurationMs(session.getElapsedMs()) + " elapsed", ColorUtil.GREEN));

        setActionBarStatus(message);
    }

    public static void setActionBarStatus(Component message) {
        Minecraft.getInstance().gui.setOverlayMessage(message, false);
    }
}