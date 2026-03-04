package cc.turtl.chiselmon.junit;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bootstraps vanilla Minecraft and sets up the Mockito extension before any tests in the
 * annotated class run. Required for any test class that directly or indirectly touches
 * Minecraft or Cobblemon types whose static initializers depend on MC being initialized
 * (e.g. {@code Pokemon}, {@code PokeBall}, {@code ItemStack}).
 *
 * <pre>{@code
 * @SetupTestDependencies
 * class PokemonCalcsTest {
 *     @Mock private Pokemon mockPokemon;
 *     ...
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(BootstrapMinecraftExtension.class)
// @ExtendWith(BootstrapCobblemonExtension.class)
@ExtendWith(MockitoExtension.class)
public @interface SetupTestDependencies {
}