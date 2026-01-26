package cc.turtl.chiselmon.service;

import cc.turtl.chiselmon.ChiselmonConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.world.InteractionResult;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConfigService {
    private final Logger logger;
    private final ConfigHolder<ChiselmonConfig> holder;
    private final List<Consumer<ChiselmonConfig>> listeners = new ArrayList<>();

    public ConfigService(Logger logger) {
        this.logger = logger;
        this.holder = AutoConfig.register(ChiselmonConfig.class, GsonConfigSerializer::new);
        this.holder.registerSaveListener(this::onSave);
        notifyListeners(this.holder.getConfig());
    }

    public ChiselmonConfig get() {
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

    public void addListener(Consumer<ChiselmonConfig> listener) {
        listeners.add(listener);
        listener.accept(get());
    }

    private InteractionResult onSave(ConfigHolder<ChiselmonConfig> manager, ChiselmonConfig data) {
        notifyListeners(data);
        logger.debug("Configuration saved successfully.");
        return InteractionResult.SUCCESS;
    }

    private void notifyListeners(ChiselmonConfig data) {
        for (Consumer<ChiselmonConfig> listener : listeners) {
            listener.accept(data);
        }
    }
}
