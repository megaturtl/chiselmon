# Chiselmon Codebase Refactoring Report

## Executive Summary

This report analyzes the Chiselmon mod codebase to identify opportunities for code decoupling, cleanup, and improvements that will make it easier to add new features. The codebase is already well-structured with good separation between platform-specific and shared code using the Architectury framework.

**Current State:**
- ~48 Java files, ~3,000 lines of code
- Multi-platform support (Fabric + NeoForge) using Architectury
- Clean API structure with predicates, calculators, and data types
- Good use of Java records and modern Java 21 features
- Some areas could benefit from additional abstraction

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Strengths](#strengths)
3. [Decoupling Opportunities](#decoupling-opportunities)
4. [Code Cleanup Recommendations](#code-cleanup-recommendations)
5. [Feature Addition Patterns](#feature-addition-patterns)
6. [Best Practices Alignment](#best-practices-alignment)
7. [Priority Recommendations](#priority-recommendations)

---

## Architecture Overview

### Current Structure

```
chiselmon/
├── common/                           # Shared code (Architectury pattern)
│   └── src/main/java/cc/turtl/chiselmon/
│       ├── api/                      # Public API layer
│       │   ├── calc/                 # Calculation utilities
│       │   │   └── capture/          # Capture mechanics
│       │   ├── comparator/           # Pokemon comparators
│       │   ├── data/                 # Data models
│       │   │   ├── species/          # Species registry
│       │   │   └── type/             # Type effectiveness
│       │   ├── duck/                 # Mixin duck interfaces
│       │   └── predicate/            # Pokemon predicates
│       ├── command/                  # Command handlers
│       ├── event/                    # Event handlers
│       ├── jade/                     # Jade integration
│       ├── services/                 # Platform abstraction
│       └── util/                     # Utility classes
│           └── format/               # Formatting utilities
├── fabric/                           # Fabric-specific code
└── neoforge/                         # NeoForge-specific code
```

### Key Patterns Already Used

1. **Service Provider Interface (SPI):** `IPathFinder` with `PlatformHelper` for platform-specific file access
2. **Strategy Pattern:** `BallStrategy` for different PokeBall capture calculations
3. **Builder Pattern:** `CaptureParams.Builder` for complex parameter construction
4. **Singleton Pattern:** `TypeEffectivenessCache.INSTANCE`, `PokemonProvider.INSTANCE`
5. **Predicate Collections:** `PokemonPredicates`, `MoveTemplatePredicates`, `PokemonEntityPredicates`
6. **Records:** Extensive use of Java records for immutable data (`ClientSpecies`, `CaptureContext`, etc.)

---

## Strengths

### ✅ Well-Done Aspects

1. **Clean Platform Separation**
   - Excellent use of Architectury for multi-platform support
   - Platform-specific code is minimal and focused
   - SPI pattern for `IPathFinder` is correctly implemented

2. **Immutable Data Structures**
   - Good use of Java records (`ClientSpecies`, `CaptureContext`, `BattleContext`, etc.)
   - Defensive copying in collections (`List.copyOf`, `Map.copyOf`)

3. **Strategy Pattern Implementation**
   - `BallStrategy` interface with many implementations
   - Easily extensible for new ball types
   - Registration system in `BallBonusCalc`

4. **Utility Organization**
   - Clear separation between pure string formatting (`StringFormats`)
   - Minecraft-specific formatting (`ComponentUtils`, `PokemonFormats`)
   - Color utilities isolated (`ColorUtils`)

5. **Predicate Composition**
   - Functional approach with `Predicate<Pokemon>` and `BiPredicate`
   - Composable predicates using `.or()`, `.and()`, `.negate()`

6. **Command System**
   - Clean `ChiselmonCommand` interface
   - Centralized registration in `ChiselmonCommands`
   - Easy to add new commands

---

## Decoupling Opportunities

### 1. Extract Configuration Interface (High Priority)

**Current State:**
```java
// ChiselmonConfig.java - Minimal implementation
@Config(name = ChiselmonConstants.MOD_ID)
public class ChiselmonConfig implements ConfigData {
    public boolean modDisabled = false;
}
```

The language file (`en_us.json`) contains translations for many config options (e.g., `text.autoconfig.chiselmon.option.threshold.extremeSmall`, `text.autoconfig.chiselmon.option.pc.quickSortEnabled`, etc.) but these are not yet implemented in the actual config class.

**Recommendation:**
Create a proper configuration hierarchy using Cloth Config (already a dependency):

```java
// config/ChiselmonConfig.java
@Config(name = ChiselmonConstants.MOD_ID)
public class ChiselmonConfig implements ConfigData {
    
    @ConfigEntry.Gui.Tooltip
    public boolean modDisabled = false;
    
    @ConfigEntry.Gui.Tooltip
    public boolean debugMode = false;
    
    @ConfigEntry.Gui.CollapsibleObject
    public ThresholdConfig threshold = new ThresholdConfig();
    
    @ConfigEntry.Gui.CollapsibleObject
    public PCConfig pc = new PCConfig();
    
    @ConfigEntry.Gui.CollapsibleObject
    public HUDConfig hud = new HUDConfig();
    
    @ConfigEntry.Gui.CollapsibleObject
    public SpawnAlertConfig spawnAlert = new SpawnAlertConfig();
}

// config/ThresholdConfig.java (example nested config)
public class ThresholdConfig {
    @ConfigEntry.Gui.Tooltip
    public float extremeSmall = 0.5f;
    
    @ConfigEntry.Gui.Tooltip
    public float extremeLarge = 1.5f;
    
    @ConfigEntry.Gui.Tooltip
    public int maxIvs = 5;
}

// config/PCConfig.java (example nested config)
public class PCConfig {
    @ConfigEntry.Gui.Tooltip
    public boolean quickSortEnabled = true;
    
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public SortMode quickSortMode = SortMode.IVS;
    
    @ConfigEntry.Gui.Tooltip
    public boolean bookmarksEnabled = true;
    
    // Add nested objects for tooltip and icon settings
}

// config/SortMode.java (example enum)
public enum SortMode {
    IVS,
    SIZE,
    LEVEL,
    POKEDEX
}
```

**Benefits:**
- Easier to add new config options
- Clearer organization
- Type-safe access throughout the codebase

---

### 2. Introduce Feature Modules (Medium Priority)

**Current State:**
The codebase mentions several features (PC enhancements, spawn alerts, spawn logger, HUD) but there's no clear module structure.

**Recommendation:**
Create a feature module pattern:

```java
// feature/Feature.java
public interface Feature {
    String getId();
    void onEnable(Minecraft client);
    void onDisable(Minecraft client);
    void onTick(Minecraft client);
    boolean isEnabled();
}

// feature/FeatureRegistry.java
public class FeatureRegistry {
    private static final List<Feature> features = new ArrayList<>();
    private static final Set<Feature> enabledFeatures = new HashSet<>();
    
    public static void register(Feature feature) {
        features.add(feature);
    }
    
    public static void tickAll(Minecraft client) {
        features.stream()
            .filter(Feature::isEnabled)
            .forEach(f -> f.onTick(client));
    }
    
    public static void enableFeature(Feature feature, Minecraft client) {
        if (enabledFeatures.add(feature)) {
            feature.onEnable(client);
        }
    }
    
    public static void disableFeature(Feature feature, Minecraft client) {
        if (enabledFeatures.remove(feature)) {
            feature.onDisable(client);
        }
    }
}

// feature/impl/SpawnAlertFeature.java
public class SpawnAlertFeature implements Feature {
    private final ChiselmonConfig config;
    
    public SpawnAlertFeature(ChiselmonConfig config) {
        this.config = config;
    }
    
    @Override
    public String getId() { return "spawn_alert"; }
    
    @Override
    public boolean isEnabled() { 
        return config.spawnAlert.enabled; 
    }
    
    @Override
    public void onEnable(Minecraft client) {
        // Initialize spawn tracking
    }
    
    @Override
    public void onDisable(Minecraft client) {
        // Cleanup spawn tracking resources
    }
    
    @Override
    public void onTick(Minecraft client) {
        // Spawn alert logic
    }
}
```

**Benefits:**
- Each feature is self-contained
- Easy to enable/disable features independently
- Simpler testing of individual features
- Clear lifecycle management

---

### 3. Extract Jade Provider Factory (Low Priority)

**Current State:**
```java
// ChiselmonJadePlugin.java
@Override
public void registerClient(IWailaClientRegistration registration) {
    registerPokemonEntity(registration);
    registerPokeSnackBlock(registration);
}
```

**Recommendation:**
Create a more extensible provider registration system:

```java
// jade/JadeProviderFactory.java
public interface JadeProviderFactory {
    void register(IWailaClientRegistration registration);
    
    // Returns config options as (resourceLocation, defaultValue) pairs
    // Using Map.Entry as a simple pair since Java doesn't have a built-in Pair type
    List<Map.Entry<ResourceLocation, Boolean>> getConfigOptions();
}

// jade/providers/AbstractEntityProvider.java
public abstract class AbstractEntityProvider<T extends Entity> 
        implements IEntityComponentProvider, JadeProviderFactory {
    
    protected final List<TooltipSection> sections = new ArrayList<>();
    
    protected void addSection(TooltipSection section) {
        sections.add(section);
    }
    
    // TooltipSection is a simple record to encapsulate tooltip generation logic
    protected record TooltipSection(
        ResourceLocation configKey,
        boolean defaultEnabled,
        BiConsumer<ITooltip, T> renderer
    ) {}
}
```

**Benefits:**
- Easier to add new Jade providers
- Consistent configuration pattern
- Better separation of concerns

---

### 4. Decouple Species Data Loading (Medium Priority)

**Current State:**
```java
// ClientSpeciesRegistry.java
public static void loadAsync(IPathFinder pathFinder) {
    // Hardcoded path to Cobblemon data
    pathFinder.getPath("cobblemon", "data/cobblemon/species").ifPresentOrElse(root -> {
        // Loading logic
    }, () -> ChiselmonConstants.LOGGER.error("Cobblemon species path not found!"));
}
```

**Recommendation:**
Introduce a data loader abstraction:

```java
// data/DataLoader.java
public interface DataLoader<T> {
    CompletableFuture<Map<String, T>> loadAsync(IPathFinder pathFinder);
    String getDataPath();
    String getModId();
}

// data/species/SpeciesDataLoader.java
public class SpeciesDataLoader implements DataLoader<ClientSpecies> {
    private static final Gson GSON = new Gson();
    
    @Override
    public String getModId() { return "cobblemon"; }
    
    @Override
    public String getDataPath() { return "data/cobblemon/species"; }
    
    @Override
    public CompletableFuture<Map<String, ClientSpecies>> loadAsync(IPathFinder pathFinder) {
        // Implementation
    }
}
```

**Benefits:**
- Testable in isolation
- Reusable for other data types
- Cleaner separation of loading vs storage

---

### 5. Extract Type Effectiveness as a Service (Low Priority)

**Current State:**
`TypeEffectivenessCache` uses static methods for global access.

**Recommendation:**
Consider making it injectable:

```java
// services/ITypeEffectivenessService.java
public interface ITypeEffectivenessService {
    float getEffectiveness(ElementalType attacker, Iterable<ElementalType> defenders);
    List<ElementalType> getSuperEffectiveTypes(Iterable<ElementalType> defenders);
}

// services/TypeEffectivenessService.java
public class TypeEffectivenessService implements ITypeEffectivenessService {
    // Current TypeEffectivenessCache implementation
}
```

**Benefits:**
- Testable with mocks
- Could support custom type charts (e.g., for modded types)

---

## Code Cleanup Recommendations

### 1. Remove Unused Mixin Infrastructure

**Current State:**
```json
// chiselmon.mixins.json
{
  "client": [],
  "mixins": []
}
```

The mixin JSON is empty but infrastructure exists.

**Recommendation:**
- Either remove the empty mixin configuration until needed
- Or document what mixins are planned

---

### 2. Consolidate Duck Interface Pattern

**Current State:**
```java
// api/duck/GlowableEntityDuck.java
public interface GlowableEntityDuck {
    void chiselmon$setClientGlowColor(Integer color);
    void chiselmon$setClientGlowing(boolean glowing);
    Integer chiselmon$getClientGlowColor();
}
```

This duck interface exists but there's no corresponding mixin.

**Recommendation:**
- Complete the mixin implementation or remove unused interfaces
- Document the pattern for future contributors

---

### 3. Improve Error Handling

**Current State:**
```java
// ClientSpeciesRegistry.java
private static void parse(Path path, Map<String, ClientSpecies> map) {
    try (Reader reader = Files.newBufferedReader(path)) {
        // ...
    } catch (Exception ignored) {
    }
}
```

Silent exception swallowing can hide issues.

**Recommendation:**
```java
private static void parse(Path path, Map<String, ClientSpecies> map) {
    try (Reader reader = Files.newBufferedReader(path)) {
        // ...
    } catch (Exception e) {
        // Log at debug level to avoid spamming during normal operation
        // Debug mode could be controlled by a static flag or config check
        ChiselmonConstants.LOGGER.debug("Failed to parse species file: {}", path, e);
    }
}
```

Consider adding a `debugMode` field to `ChiselmonConfig` that can be checked for more verbose logging throughout the codebase.

---

### 4. Extract Reflection Usage

**Current State:**
```java
// PokemonFormats.java - marks() method
try {
    Field f = Mark.class.getDeclaredField("name");
    f.setAccessible(true);
    String key = (String) f.get(mark);
    // ...
} catch (Exception e) {
    return UNKNOWN;
}
```

**Recommendation:**
Create a reflection utility:

```java
// util/ReflectionUtils.java
public final class ReflectionUtils {
    
    // Cache with weak references to avoid memory leaks if classes are unloaded
    // Note: For a mod context, classes are rarely unloaded, so a simple cache is fine
    private static final Map<String, Field> fieldCache = new ConcurrentHashMap<>();
    
    private ReflectionUtils() {}
    
    /**
     * Gets a field value with caching for repeated access.
     * Cache key is className + "." + fieldName to ensure uniqueness.
     */
    public static <T> Optional<T> getFieldValue(Object obj, String fieldName, Class<T> type) {
        if (obj == null) return Optional.empty();
        
        String cacheKey = obj.getClass().getName() + "." + fieldName;
        
        try {
            Field field = fieldCache.computeIfAbsent(cacheKey, k -> {
                try {
                    Field f = obj.getClass().getDeclaredField(fieldName);
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException e) {
                    return null;
                }
            });
            
            if (field == null) return Optional.empty();
            
            Object value = field.get(obj);
            return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
        } catch (Exception e) {
            ChiselmonConstants.LOGGER.debug("Reflection failed for {}.{}", 
                obj.getClass().getSimpleName(), fieldName, e);
            return Optional.empty();
        }
    }
}
```

---

### 5. Add Missing Validation

**Current State:**
```java
// CaptureParams.java
public CaptureParams {
    if (maxHp <= 0) throw new IllegalArgumentException("maxHp must be positive");
    if (currentHp < 0) throw new IllegalArgumentException("currentHp cannot be negative");
    if (currentHp > maxHp) throw new IllegalArgumentException("currentHp cannot exceed maxHp");
}
```

Good validation in `CaptureParams`, but missing in other areas.

**Recommendation:**
Apply similar validation patterns to other data classes:

```java
// DefenderTypes.java - already has validation, good pattern
// ClientSpecies.java - could add validation
public ClientSpecies {
    Objects.requireNonNull(name, "Species name cannot be null");
    Objects.requireNonNull(eggGroups, "eggGroups cannot be null");
}
```

---

## Feature Addition Patterns

### Adding a New Command

```java
// 1. Create the command class
public class NewCommand implements ChiselmonCommand {
    @Override
    public String getName() { return "newcmd"; }
    
    @Override
    public String getDescription() { return "Does something new"; }
    
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getName())
                .executes(this::execute);
    }
    
    private int execute(CommandContext<CommandSourceStack> context) {
        // Implementation
        return Command.SINGLE_SUCCESS;
    }
}

// 2. Register in ChiselmonCommands.java
private static final List<ChiselmonCommand> COMMANDS = List.of(
        new InfoCommand(),
        new DebugCommand(),
        new NewCommand() // Add here
);
```

### Adding a New Ball Strategy

```java
// 1. Create the strategy class (in BallStrategies.java or separate file)
class NewBallStrategy implements BallStrategy {
    @Override
    public float calculate(CaptureContext ctx) {
        // Calculate bonus based on context
        // Example: 1.5x bonus if target is in water
        return ctx.targetEntity().isInWater() ? 1.5F : 1.0F;
    }
}

// 2. Register in BallBonusCalc.registerDefaultStrategies()
// Use the appropriate getter from PokeBalls class for the ball you're adding
// For example, for a custom mod ball:
registerStrategy(PokeBalls.INSTANCE.getPokeBall("custom_ball_id"), new NewBallStrategy());
// Or for existing Cobblemon balls, use the existing getters like:
// registerStrategy(PokeBalls.getPremierBall(), new NewBallStrategy());
```

### Adding a New Jade Provider

```java
// 1. Create the provider
public enum NewBlockProvider implements IBlockComponentProvider {
    INSTANCE;
    
    public static final ResourceLocation CONFIG_OPTION = modResource("new_block.option");
    private static final ResourceLocation UID = modResource("new_block");
    
    @Override
    public ResourceLocation getUid() { return UID; }
    
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        // Implementation
    }
}

// 2. Register in ChiselmonJadePlugin.java
private void registerNewBlock(IWailaClientRegistration registration) {
    registration.registerBlockComponent(NewBlockProvider.INSTANCE, NewBlock.class);
    configureOption(registration, NewBlockProvider.CONFIG_OPTION, true);
}
```

### Adding a New Predicate

```java
// In PokemonPredicates.java
public static final Predicate<Pokemon> NEW_CONDITION = p -> {
    // Check condition
    return someCondition(p);
};

// Composable with existing predicates
public static final Predicate<Pokemon> COMPLEX_CHECK = IS_SHINY.and(NEW_CONDITION);
```

---

## Best Practices Alignment

### ✅ Already Following

| Practice | Status | Notes |
|----------|--------|-------|
| Immutable data | ✅ Good | Records used extensively |
| Null safety | ⚠️ Partial | Some null checks, could use more `Optional` |
| Thread safety | ✅ Good | `AtomicBoolean`, `ConcurrentHashMap` in async code |
| Code organization | ✅ Good | Clear package structure |
| Java 21 features | ✅ Good | Records, pattern matching, enhanced switch |

### 🔧 Improvements Suggested

| Practice | Current | Recommended |
|----------|---------|-------------|
| Dependency Injection | Static singletons | Consider constructor injection for testability |
| Configuration | Minimal class | Full hierarchical config |
| Feature toggles | None visible | Add feature toggle system |
| Logging levels | Mixed | Standardize DEBUG vs INFO usage |
| Internationalization | Partial | Complete all user-facing strings |

### Minecraft Modding Best Practices

| Practice | Status | Notes |
|----------|--------|-------|
| Client-side only checks | ✅ Good | Architectury handles this via platform abstractions |
| Platform abstraction | ✅ Excellent | Clean SPI pattern |
| Resource location usage | ✅ Good | Proper `modResource()` helper |
| Event handling | ✅ Good | Platform-appropriate registration |

---

## Priority Recommendations

### High Priority (Do First)

1. **Complete the Configuration System**
   - Add all config options shown in lang file
   - Organize into nested config classes
   - This unblocks many feature implementations

2. **Add Debug Mode Toggle**
   - Currently no way to enable verbose logging
   - Would help with troubleshooting

3. **Document API Package**
   - Add Javadoc to public API classes
   - Create README in api/ folder

### Medium Priority

4. **Implement Feature Module Pattern**
   - Start with one feature (e.g., Spawn Alert)
   - Migrate others incrementally

5. **Add Unit Tests**
   - Currently no tests exist
   - Start with pure logic classes (`CaptureFormulaCalc`, `StringFormats`)

6. **Complete Mixin Infrastructure**
   - Implement GlowableEntityDuck mixin or remove
   - Document mixin patterns for contributors

### Low Priority

7. **Refactor Static Singletons**
   - Only if testability becomes an issue
   - Current approach is acceptable for mod size

8. **Extract Jade Provider Factory**
   - Only if adding many more providers

---

## Conclusion

The Chiselmon codebase is well-structured for a Minecraft mod of its size. The main areas for improvement are:

1. **Configuration completeness** - Align the config class with documented options
2. **Feature modularity** - Add a formal feature system as the mod grows
3. **Documentation** - Add Javadoc and contributor guidelines
4. **Testing** - Add unit tests for pure logic components

The existing patterns (Strategy, SPI, Records, Predicates) are good foundations. The primary focus should be on completing the configuration system and establishing patterns that scale as new features are added.
