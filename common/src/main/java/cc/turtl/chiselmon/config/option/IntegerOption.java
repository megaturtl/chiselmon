package cc.turtl.chiselmon.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;

/**
 * A config option that holds an integer value with optional min/max bounds.
 */
public class IntegerOption extends BaseConfigOption<Integer> {

    private final int minValue;
    private final int maxValue;

    public IntegerOption(String name, String displayName, @Nullable String comment, int defaultValue, int minValue, int maxValue) {
        super(name, displayName, comment, defaultValue);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    @Override
    public void setValue(Integer value) {
        // Clamp to bounds
        super.setValue(Math.max(minValue, Math.min(maxValue, value)));
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            setValue(element.getAsInt());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseConfigOption.Builder<Integer, Builder, IntegerOption> {
        private int minValue = Integer.MIN_VALUE;
        private int maxValue = Integer.MAX_VALUE;

        public Builder() {
            this.defaultValue = 0;
        }

        public Builder minValue(int minValue) {
            this.minValue = minValue;
            return self();
        }

        public Builder maxValue(int maxValue) {
            this.maxValue = maxValue;
            return self();
        }

        public Builder range(int min, int max) {
            this.minValue = min;
            this.maxValue = max;
            return self();
        }

        @Override
        public IntegerOption build() {
            if (displayName == null) displayName = name;
            return new IntegerOption(name, displayName, comment, defaultValue, minValue, maxValue);
        }
    }
}
