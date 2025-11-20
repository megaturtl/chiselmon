package cc.turtl.cobbleaid.config;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomLogger {
    private final Logger logger;
    private final String prefix;
    private final String name;

    public CustomLogger(String name) {
        this.name = name;
        this.logger = LoggerFactory.getLogger(name);
        this.prefix = "[" + name + "] ";
    }

    public void setDebugMode(boolean enabled) {
        Level level = enabled ? Level.DEBUG : Level.INFO;
        Configurator.setLevel(this.name, level);
        this.logger.info("{}Debug mode {}", prefix, enabled ? "ENABLED" : "DISABLED");
    }

    public void info(String message, Object... args) {
        logger.info(prefix + message, args);
    }

    public void debug(String message, Object... args) {
        logger.debug(prefix + message, args);
    }

    public void warn(String message, Object... args) {
        logger.warn(prefix + message, args);
    }

    public void error(String message, Object... args) {
        logger.error(prefix + message, args);
    }
}
