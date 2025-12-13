package cc.turtl.cobbleaid.service;

import cc.turtl.cobbleaid.config.CobbleAidLogger;

public class LoggingService {
    private final CobbleAidLogger rootLogger;

    public LoggingService(String name) {
        this.rootLogger = new CobbleAidLogger(name);
    }

    public CobbleAidLogger getRootLogger() {
        return rootLogger;
    }

    public CobbleAidLogger getLogger(Class<?> clazz) {
        return rootLogger;
    }

    public CobbleAidLogger logger(Class<?> clazz) {
        return getLogger(clazz);
    }

    public void setDebugMode(boolean enabled) {
        rootLogger.setDebugMode(enabled);
    }
}
