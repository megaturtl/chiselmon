package cc.turtl.chiselmon.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * A config option that holds an enum value.
 *
 * @param <E> The enum type
 */
public class EnumOption<E extends Enum<E>> extends BaseConfigOption<E> {

    private final Class<E> enumClass;
    private final List<E> allowedValues;

    public EnumOption(String name, String displayName, @Nullable String comment, E defaultValue, Class<E> enumClass) {
        super(name, displayName, comment, defaultValue);
        this.enumClass = enumClass;
        this.allowedValues = Arrays.asList(enumClass.getEnumConstants());
    }

    public Class<E> getEnumClass() {
        return enumClass;
    }

    public List<E> getAllowedValues() {
        return allowedValues;
    }

    /**
     * Cycles to the next enum value
     */
    public void cycleNext() {
        int currentIndex = allowedValues.indexOf(getValue());
        int nextIndex = (currentIndex + 1) % allowedValues.size();
        setValue(allowedValues.get(nextIndex));
    }

    /**
     * Cycles to the previous enum value
     */
    public void cyclePrevious() {
        int currentIndex = allowedValues.indexOf(getValue());
        int prevIndex = (currentIndex - 1 + allowedValues.size()) % allowedValues.size();
        setValue(allowedValues.get(prevIndex));
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value.name());
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            try {
                E enumValue = Enum.valueOf(enumClass, element.getAsString());
                setValue(enumValue);
            } catch (IllegalArgumentException ignored) {
                // Keep default if invalid enum name
            }
        }
    }

    public static <E extends Enum<E>> Builder<E> builder(Class<E> enumClass) {
        return new Builder<>(enumClass);
    }

    public static class Builder<E extends Enum<E>> extends BaseConfigOption.Builder<E, Builder<E>, EnumOption<E>> {
        private final Class<E> enumClass;

        public Builder(Class<E> enumClass) {
            this.enumClass = enumClass;
            this.defaultValue = enumClass.getEnumConstants()[0];
        }

        @Override
        public EnumOption<E> build() {
            if (displayName == null) displayName = name;
            return new EnumOption<>(name, displayName, comment, defaultValue, enumClass);
        }
    }
}
