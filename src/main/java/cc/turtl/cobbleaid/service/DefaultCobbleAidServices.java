package cc.turtl.cobbleaid.service;

public class DefaultCobbleAidServices implements CobbleAidServices {
    private final ConfigService configService;
    private final LoggingService loggingService;
    private final WorldDataService worldDataService;

    public DefaultCobbleAidServices(
            ConfigService configService,
            LoggingService loggingService,
            WorldDataService worldDataService) {
        this.configService = configService;
        this.loggingService = loggingService;
        this.worldDataService = worldDataService;
    }

    @Override
    public ConfigService config() {
        return configService;
    }

    @Override
    public LoggingService logging() {
        return loggingService;
    }

    @Override
    public WorldDataService worldData() {
        return worldDataService;
    }
}
