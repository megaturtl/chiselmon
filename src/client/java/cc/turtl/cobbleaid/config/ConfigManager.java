package cc.turtl.cobbleaid.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.util.ActionResult;

public class ConfigManager {
    private final ConfigHolder<ModConfig> holder;

    public ConfigManager(CustomLogger logger) {
        this.holder = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

        holder.registerSaveListener(
                (manager, data) -> {
                    data.validate_fields();
                    logger.setDebugMode(data.debugMode);
                    logger.debug("Configuration saved.");
                    return ActionResult.SUCCESS;
                });

        ModConfig config = holder.getConfig();
        logger.setDebugMode(config.debugMode);
    }

    public ModConfig getConfig() {
        return holder.getConfig();
    }

    public void setConfig(ModConfig newConfig) {
        this.holder.setConfig(newConfig);
    }

    public void resetToDefault() {
        holder.resetToDefault();
    }

    public void save() {
        holder.save();
    }
}
