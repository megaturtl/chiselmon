package cc.turtl.cobbleaid.service;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class LoggerService {
    private final String name;

    public LoggerService(String name) {
        this.name = name;
    }

    public Logger get() {
        Logger logger = LogManager.getLogger(name);
        return logger;
    };

    public void setDebugMode(boolean enabled) {
        Level level = enabled ? Level.DEBUG : Level.INFO;
        Configurator.setLevel(this.name, level);
    }
}
