package cc.turtl.cobbleaid.service;

public class DefaultCobbleAidServices implements ICobbleAidServices {
    private final ConfigService configService;
    private final LoggerService loggerService;
    private final WorldDataService worldDataService;

    public DefaultCobbleAidServices(
            ConfigService configService,
            LoggerService loggerService,
            WorldDataService worldDataService) {
        this.configService = configService;
        this.loggerService = loggerService;
        this.worldDataService = worldDataService;
    }

    @Override
    public ConfigService config() {
        return configService;
    }

    @Override
    public LoggerService logger() {
        return loggerService;
    }

    @Override
    public WorldDataService worldData() {
        return worldDataService;
    }
}
