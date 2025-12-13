package cc.turtl.cobbleaid.service;

import cc.turtl.cobbleaid.config.CobbleAidLogger;

public interface CobbleAidServices {
    ConfigService config();

    LoggingService logging();

    WorldDataService worldData();

    default CobbleAidLogger logger(Class<?> clazz) {
        return logging().logger(clazz);
    }
}
