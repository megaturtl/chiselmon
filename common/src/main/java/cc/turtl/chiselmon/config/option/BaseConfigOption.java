package cc.turtl.chiselmon.config.option;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Abstract base implementation of ConfigOption providing common functionality.
 *
 * @param <T> The type of value this config option holds
 */
public abstract class BaseConfigOption<T> implements ConfigOption<T> {

    protected final String name;
    protected final String displayName;
    @Nullable
    protected final String comment;
    protected final T defaultValue;
    protected T value;
    @Nullable
    protected Consumer<T> changeCallback;

    protected BaseConfigOption(String name, String displayName, @Nullable String comment, T defaultValue) {
        this.name = name;
        this.displayName = displayName;
        this.comment = comment;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    @Nullable
    public String getComment() {
        return comment;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setValue(T value) {
        T oldValue = this.value;
        this.value = value;
        if (!Objects.equals(oldValue, value) && changeCallback != null) {
            changeCallback.accept(value);
        }
    }

    @Override
    public void resetToDefault() {
        setValue(defaultValue);
    }

    @Override
    public boolean isModified() {
        return !Objects.equals(value, defaultValue);
    }

    @Override
    public void setChangeCallback(@Nullable Consumer<T> callback) {
        this.changeCallback = callback;
    }

    /**
     * Builder pattern for cleaner config option construction.
     */
    public static abstract class Builder<T, B extends Builder<T, B, O>, O extends BaseConfigOption<T>> {
        protected String name;
        protected String displayName;
        protected String comment;
        protected T defaultValue;

        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }

        public B name(String name) {
            this.name = name;
            return self();
        }

        public B displayName(String displayName) {
            this.displayName = displayName;
            return self();
        }

        public B comment(String comment) {
            this.comment = comment;
            return self();
        }

        public B defaultValue(T defaultValue) {
            this.defaultValue = defaultValue;
            return self();
        }

        public abstract O build();
    }
}
