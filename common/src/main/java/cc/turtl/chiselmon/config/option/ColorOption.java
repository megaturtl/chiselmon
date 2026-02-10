package cc.turtl.chiselmon.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;

/**
 * A config option that holds an ARGB color value.
 * Supports hex string format (#RRGGBB or #AARRGGBB) for better human readability.
 */
public class ColorOption extends BaseConfigOption<Integer> {

    public ColorOption(String name, String displayName, @Nullable String comment, int defaultValue) {
        super(name, displayName, comment, defaultValue);
    }

    /**
     * @return The alpha component (0-255)
     */
    public int getAlpha() {
        return (value >> 24) & 0xFF;
    }

    /**
     * @return The red component (0-255)
     */
    public int getRed() {
        return (value >> 16) & 0xFF;
    }

    /**
     * @return The green component (0-255)
     */
    public int getGreen() {
        return (value >> 8) & 0xFF;
    }

    /**
     * @return The blue component (0-255)
     */
    public int getBlue() {
        return value & 0xFF;
    }

    /**
     * Sets the color from individual ARGB components
     */
    public void setARGB(int alpha, int red, int green, int blue) {
        setValue(((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF));
    }

    /**
     * Sets the color from individual RGB components (alpha = 255)
     */
    public void setRGB(int red, int green, int blue) {
        setARGB(255, red, green, blue);
    }

    /**
     * @return The color as a hex string (#AARRGGBB)
     */
    public String getHexString() {
        return String.format("#%08X", value);
    }

    /**
     * @return The color as a hex string without alpha (#RRGGBB)
     */
    public String getHexStringRGB() {
        return String.format("#%06X", value & 0x00FFFFFF);
    }

    /**
     * Sets the color from a hex string
     * @param hex The hex string (#RRGGBB or #AARRGGBB, with or without #)
     */
    public void setFromHexString(String hex) {
        String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            if (cleaned.length() == 6) {
                // RGB format, add full alpha
                setValue(0xFF000000 | Integer.parseInt(cleaned, 16));
            } else if (cleaned.length() == 8) {
                // ARGB format
                setValue((int) Long.parseLong(cleaned, 16));
            }
        } catch (NumberFormatException ignored) {
            // Keep current value if parsing fails
        }
    }

    @Override
    public JsonElement toJson() {
        // Store as hex string for readability
        return new JsonPrimitive(getHexString());
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isString()) {
                setFromHexString(element.getAsString());
            } else if (element.getAsJsonPrimitive().isNumber()) {
                setValue(element.getAsInt());
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseConfigOption.Builder<Integer, Builder, ColorOption> {
        public Builder() {
            this.defaultValue = 0xFFFFFFFF; // White with full alpha
        }

        /**
         * Set default from hex string
         */
        public Builder defaultHex(String hex) {
            String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
            if (cleaned.length() == 6) {
                this.defaultValue = 0xFF000000 | Integer.parseInt(cleaned, 16);
            } else if (cleaned.length() == 8) {
                this.defaultValue = (int) Long.parseLong(cleaned, 16);
            }
            return self();
        }

        /**
         * Set default from RGB values
         */
        public Builder defaultRGB(int red, int green, int blue) {
            this.defaultValue = 0xFF000000 | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
            return self();
        }

        @Override
        public ColorOption build() {
            if (displayName == null) displayName = name;
            return new ColorOption(name, displayName, comment, defaultValue);
        }
    }
}
