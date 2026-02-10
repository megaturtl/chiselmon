package cc.turtl.chiselmon.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;

/**
 * A config option that holds a string value.
 */
public class StringOption extends BaseConfigOption<String> {

    public StringOption(String name, String displayName, @Nullable String comment, String defaultValue) {
        super(name, displayName, comment, defaultValue);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            setValue(element.getAsString());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseConfigOption.Builder<String, Builder, StringOption> {
        public Builder() {
            this.defaultValue = "";
        }

        @Override
        public StringOption build() {
            if (displayName == null) displayName = name;
            return new StringOption(name, displayName, comment, defaultValue);
        }
    }
}
