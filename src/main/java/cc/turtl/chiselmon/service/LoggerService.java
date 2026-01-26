package cc.turtl.chiselmon.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class LoggerService {
    private final String name;

    public LoggerService(String name) {
        this.name = name;
    }

    public Logger get() {
        return LogManager.getLogger(name);
    }

    public void setDebugMode(boolean enabled) {
        Level level = enabled ? Level.DEBUG : Level.INFO;
        Configurator.setLevel(this.name, level);
    }
}
