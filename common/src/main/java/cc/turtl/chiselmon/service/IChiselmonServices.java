package cc.turtl.chiselmon.service;

public interface IChiselmonServices {
    ConfigService config();

    LoggerService logger();

    WorldDataService worldData();
}
