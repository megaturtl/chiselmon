package cc.turtl.chiselmon.system.alert.action;

import cc.turtl.chiselmon.system.alert.AlertContext;

@FunctionalInterface
public interface AlertAction {
    void execute(AlertContext ctx);
}