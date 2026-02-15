package cc.turtl.chiselmon.config.category;

import dev.isxander.yacl3.api.OptionGroup;

/**
 * Marker interface for config groups (nested config sections).
 */
public interface ConfigGroupBuilder {
    /**
     * Builds the YACL OptionGroup for this group.
     */
    OptionGroup buildGroup();
}