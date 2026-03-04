package cc.turtl.chiselmon.junit;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit extension that bootstraps vanilla Minecraft before any tests in the class run.
 * <p>
 * Use via {@link SetupTestDependencies} - do not reference this class directly.
 */
public class BootstrapMinecraftExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(@NonNull ExtensionContext context) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
}