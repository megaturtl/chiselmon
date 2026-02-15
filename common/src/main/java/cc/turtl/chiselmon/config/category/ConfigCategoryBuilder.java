package cc.turtl.chiselmon.config.category;

import dev.isxander.yacl3.api.ConfigCategory;

/**
 * Marker interface for config categories.
 * Each category is responsible for building its own UI representation.
 */
public interface ConfigCategoryBuilder {
    /**
     * Builds the YACL ConfigCategory instance for this category.
     */
    ConfigCategory buildCategory();
}