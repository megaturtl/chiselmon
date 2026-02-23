package cc.turtl.chiselmon.system.alert.action;

import cc.turtl.chiselmon.system.alert.AlertContext;
import cc.turtl.chiselmon.util.render.PokemonEntityUtils;

public class GlowAction implements AlertAction {
    @Override
    public void execute(AlertContext ctx) {
        if (ctx.shouldHighlight()) {
            int color = ctx.highlightFilter().rgb();
            PokemonEntityUtils.addGlow(ctx.entity(), color);
            PokemonEntityUtils.highlightNickname(ctx.entity(), color);
        }
    }
}