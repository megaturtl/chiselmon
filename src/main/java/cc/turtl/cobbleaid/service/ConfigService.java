package cc.turtl.cobbleaid.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.Logger;

import cc.turtl.cobbleaid.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.world.InteractionResult;

public class ConfigService {
    private final Logger logger;
    private final ConfigHolder<ModConfig> holder;
    private final List<Consumer<ModConfig>> listeners = new ArrayList<>();

    public ConfigService(Logger logger) {
        this.logger = logger;
        this.holder = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        this.holder.registerSaveListener(this::onSave);
        notifyListeners(this.holder.getConfig());
    }

    public ModConfig get() {
        return holder.getConfig();
    }

    public void save() {
        holder.save();
    }

    public void reload() {
        holder.load();
        notifyListeners(holder.getConfig());
        logger.info("Configuration reloaded.");
    }

    public void addListener(Consumer<ModConfig> listener) {
        listeners.add(listener);
        listener.accept(get());
    }

    public ConfigHolder<ModConfig> holder() {
        return holder;
    }

    private InteractionResult onSave(ConfigHolder<ModConfig> manager, ModConfig data) {
        data.validate_fields();
        notifyListeners(data);
        logger.debug("Configuration saved successfully.");
        return InteractionResult.SUCCESS;
    }

    private void notifyListeners(ModConfig data) {
        for (Consumer<ModConfig> listener : listeners) {
            listener.accept(data);
        }
    }
}
