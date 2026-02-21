package cc.turtl.chiselmon.api.event;

import net.minecraft.client.Minecraft;

public record ClientPostTickEvent(Minecraft mc) {
}
