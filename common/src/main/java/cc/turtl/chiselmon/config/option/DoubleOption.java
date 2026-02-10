package cc.turtl.chiselmon.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;

/**
 * A config option that holds a double value with optional min/max bounds.
 */
public class DoubleOption extends BaseConfigOption<Double> {

    private final double minValue;
    private final double maxValue;

    public DoubleOption(String name, String displayName, @Nullable String comment, double defaultValue, double minValue, double maxValue) {
        super(name, displayName, comment, defaultValue);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    @Override
    public void setValue(Double value) {
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
            setValue(element.getAsDouble());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseConfigOption.Builder<Double, Builder, DoubleOption> {
        private double minValue = Double.MIN_VALUE;
        private double maxValue = Double.MAX_VALUE;

        public Builder() {
            this.defaultValue = 0.0;
        }

        public Builder minValue(double minValue) {
            this.minValue = minValue;
            return self();
        }

        public Builder maxValue(double maxValue) {
            this.maxValue = maxValue;
            return self();
        }

        public Builder range(double min, double max) {
            this.minValue = min;
            this.maxValue = max;
            return self();
        }

        @Override
        public DoubleOption build() {
            if (displayName == null) displayName = name;
            return new DoubleOption(name, displayName, comment, defaultValue, minValue, maxValue);
        }
    }
}
