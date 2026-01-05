package cc.turtl.chiselmon.service;

public class DefaultChiselmonServices implements IChiselmonServices {
    private final ConfigService configService;
    private final LoggerService loggerService;
    private final WorldDataService worldDataService;

    public DefaultChiselmonServices(
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
