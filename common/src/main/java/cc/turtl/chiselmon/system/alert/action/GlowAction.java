package cc.turtl.chiselmon.system.alert.action;

import cc.turtl.chiselmon.system.alert.AlertContext;
import static cc.turtl.chiselmon.client.util.PokemonEntityExtensionsKt.*;

public class GlowAction implements AlertAction {
    @Override
    public void execute(AlertContext ctx) {
        if (ctx.shouldHighlight()) {
            int color = ctx.highlightFilter().rgb();
            addGlow(ctx.entity(), color);
            highlightNickname(ctx.entity(), color);
        }
    }
}