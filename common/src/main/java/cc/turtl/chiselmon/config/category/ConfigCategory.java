package cc.turtl.chiselmon.config.category;

import cc.turtl.chiselmon.config.option.ConfigOption;

import java.util.List;

/**
 * Represents a category/section of config options in the GUI.
 */
public interface ConfigCategory {

    /**
     * @return The unique identifier for this category
     */
    String getName();

    /**
     * @return The display name shown in the config GUI
     */
    String getDisplayName();

    /**
     * @return All config options in this category
     */
    List<ConfigOption<?>> getOptions();
}
