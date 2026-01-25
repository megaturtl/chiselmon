package cc.turtl.chiselmon;

import org.apache.logging.log4j.Logger;

import cc.turtl.chiselmon.service.IChiselmonServices;

public class Chiselmon {
    private static volatile IChiselmonServices services;

    public static void init(Runnable initServices, Runnable registerCommands, Runnable registerFeatures, Runnable registerListeners) {
        initServices.run();
        registerCommands.run();
        registerFeatures.run();
        registerListeners.run();
        getLogger().info("{} {} initialized.", ChiselmonConstants.MODNAME, ChiselmonConstants.VERSION);
    }

    public static void setServices(IChiselmonServices servicesInstance) {
        services = servicesInstance;
    }

    public void reloadConfig() {
        services().config().reload();
    }

    public void saveConfig() {
        services().config().save();
    }

    public static Logger getLogger() {
        return services().logger().get();
    }

    public static IChiselmonServices services() {
        if (services == null) {
            throw new IllegalStateException(
                    ChiselmonConstants.MODNAME + " services are not initialized yet; access them after client initialization.");
        }
        return services;
    }

    public static boolean isDisabled() {
        return services().config().get().modDisabled;
    }
}
