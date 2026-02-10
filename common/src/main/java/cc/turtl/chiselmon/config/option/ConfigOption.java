package cc.turtl.chiselmon.config.option;

import com.google.gson.JsonElement;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Base interface for all config options.
 * Inspired by malilib's config system for extensibility.
 *
 * @param <T> The type of value this config option holds
 */
public interface ConfigOption<T> {

    /**
     * @return The unique identifier/name of this config option
     */
    String getName();

    /**
     * @return The display name shown in the config GUI
     */
    String getDisplayName();

    /**
     * @return The tooltip/description for this config option
     */
    @Nullable
    String getComment();

    /**
     * @return The current value of this config option
     */
    T getValue();

    /**
     * @return The default value of this config option
     */
    T getDefaultValue();

    /**
     * Sets the value of this config option
     * @param value The new value
     */
    void setValue(T value);

    /**
     * Resets the config option to its default value
     */
    void resetToDefault();

    /**
     * @return true if the current value differs from the default
     */
    boolean isModified();

    /**
     * Serialize this config option to JSON
     * @return The JSON representation
     */
    JsonElement toJson();

    /**
     * Deserialize this config option from JSON
     * @param element The JSON element
     */
    void fromJson(JsonElement element);

    /**
     * Sets a callback that will be called when the value changes
     * @param callback The callback, or null to remove
     */
    void setChangeCallback(@Nullable Consumer<T> callback);
}
