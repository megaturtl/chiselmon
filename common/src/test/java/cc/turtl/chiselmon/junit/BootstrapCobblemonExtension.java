package cc.turtl.chiselmon.junit;

import com.cobblemon.mod.common.Cobblemon;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit extension that bootstraps important Cobblemon things before any tests in the class run.
 */
public class BootstrapCobblemonExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(@NonNull ExtensionContext context) {
        Cobblemon.INSTANCE.loadConfig();
    }
}