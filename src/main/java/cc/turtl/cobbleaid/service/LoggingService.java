package cc.turtl.cobbleaid.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cc.turtl.cobbleaid.config.CobbleAidLogger;

public class LoggingService {
    private final CobbleAidLogger rootLogger;
    private final Map<Class<?>, CobbleAidLogger> loggerMap = new ConcurrentHashMap<>();
    private volatile boolean debugEnabled = false;

    public LoggingService(String name) {
        this.rootLogger = new CobbleAidLogger(name);
    }

    public CobbleAidLogger getRootLogger() {
        return rootLogger;
    }

    public CobbleAidLogger getLogger(Class<?> clazz) {
        return loggerMap.computeIfAbsent(clazz, c -> {
            CobbleAidLogger logger = new CobbleAidLogger(c.getName());
            logger.setDebugMode(debugEnabled);
            return logger;
        });
    }

    public void setDebugMode(boolean enabled) {
        this.debugEnabled = enabled;
        rootLogger.setDebugMode(enabled);
        loggerMap.values().forEach(logger -> logger.setDebugMode(enabled));
    }
}
