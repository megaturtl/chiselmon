package cc.turtl.chiselmon.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;

/**
 * A config option that holds a boolean value.
 */
public class BooleanOption extends BaseConfigOption<Boolean> {

    public BooleanOption(String name, String displayName, @Nullable String comment, boolean defaultValue) {
        super(name, displayName, comment, defaultValue);
    }

    /**
     * Toggles the boolean value
     */
    public void toggle() {
        setValue(!getValue());
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
            setValue(element.getAsBoolean());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseConfigOption.Builder<Boolean, Builder, BooleanOption> {
        public Builder() {
            this.defaultValue = false;
        }

        @Override
        public BooleanOption build() {
            if (displayName == null) displayName = name;
            return new BooleanOption(name, displayName, comment, defaultValue);
        }
    }
}
