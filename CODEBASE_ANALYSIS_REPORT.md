# Chiselmon Codebase Analysis Report

This report provides a comprehensive analysis of the Chiselmon mod, including potential bugs, code quality improvements, performance optimizations, and feature suggestions.

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Potential Bugs](#potential-bugs)
3. [Code Quality Improvements](#code-quality-improvements)
4. [Performance & Efficiency Improvements](#performance--efficiency-improvements)
5. [Feature Suggestions](#feature-suggestions)

---

## Architecture Overview

Chiselmon is a well-structured client-side Fabric mod for Cobblemon with the following key components:

- **Main Entry Point**: `Chiselmon.java` - Initializes services, commands, and features
- **Service Layer**: `IChiselmonServices` interface with config, logging, and world data services
- **Feature System**: `AbstractFeature` base class for modular feature implementation
- **Mixin System**: Extensive use of mixins for PC GUI modifications, tooltips, and HUD rendering
- **API Layer**: Species registry, capture calculations, predicates, and comparators
- **Jade Integration**: Custom tooltip providers for Pokemon entities and PokeSnack blocks

---

## Potential Bugs

### 1. Race Condition in `SimpleSpeciesRegistry.loadAsync()`
**Location**: `src/main/java/cc/turtl/chiselmon/api/SimpleSpeciesRegistry.java:22-59`

**Issue**: The `loaded` and `isLoading` flags are not properly synchronized, which can lead to race conditions in a multi-threaded environment.

```java
public static void loadAsync() {
    if (loaded || isLoading)
        return;
    isLoading = true;  // Not atomic with the check above
    // ...
}
```

**Fix**: Use `AtomicBoolean` or synchronize the check-and-set operation:

```java
private static final AtomicBoolean isLoading = new AtomicBoolean(false);
private static volatile boolean loaded = false;

public static void loadAsync() {
    if (!isLoading.compareAndSet(false, true))
        return;
    if (loaded) {
        isLoading.set(false);
        return;
    }
    // ...
}
```

---

### 2. Memory Leak in `NeoDaycareEgg.DUMMY_CACHE`
**Location**: `src/main/java/cc/turtl/chiselmon/compat/neodaycare/NeoDaycareEgg.java:34`

**Issue**: The `DUMMY_CACHE` is a static `HashMap` that caches dummy Pokemon by UUID, but entries are only removed explicitly via `removeCached()`. If Pokemon are removed from the PC without calling this method, the cache will grow unbounded.

**Fix**: Consider using a `WeakHashMap` or implementing a cache eviction strategy. Also ensure the cache is cleared when connecting to a new world:

```java
// Add to WorldDataService or handle in disconnect event
NeoDaycareEgg.clearCache();
```

---

### 3. Potential NPE in `PokemonFormatUtil.marks()`
**Location**: `src/main/java/cc/turtl/chiselmon/api/util/PokemonFormatUtil.java:245-258`

**Issue**: The reflection-based access to `Mark.name` field could fail silently and return `UNKNOWN` without proper error logging.

```java
try {
    Field nameField = Mark.class.getDeclaredField("name");
    nameField.setAccessible(true);
    // ...
} catch (Exception e) {
    return UNKNOWN;  // Silent failure
}
```

**Fix**: Add logging for debugging purposes:

```java
} catch (Exception e) {
    Chiselmon.getLogger().debug("Failed to access Mark name field: {}", e.getMessage());
    return UNKNOWN;
}
```

---

### 4. Incorrect Component Equality Check
**Location**: `src/main/java/cc/turtl/chiselmon/util/ComponentFormatUtil.java:48`

**Issue**: Reference equality (`!=`) is used to check if a component is empty, which may not work correctly for all Component implementations.

```java
if (mappedComponent != Component.empty()) {
```

**Fix**: Use a content-based check:

```java
if (mappedComponent != null && !mappedComponent.getString().isEmpty()) {
```

---

### 5. Missing Null Check in `PCTabManager.createTabButtons()`
**Location**: `src/main/java/cc/turtl/chiselmon/feature/pc/tab/PCTabManager.java:40`

**Issue**: `clientBoxes.get(targetBoxNumber).getName()` could throw an `IndexOutOfBoundsException` if `targetBoxNumber` equals `clientBoxes.size()`.

```java
if (clientBoxes.size() < targetBoxNumber) {  // Should be <=
    continue;
}
```

**Fix**: Change the condition:

```java
if (targetBoxNumber >= clientBoxes.size()) {
    continue;
}
```

---

### 6. Hardcoded Text in UI Components
**Location**: Multiple files including `PCBookmarkButton.java:28-29`

**Issue**: UI text is hardcoded rather than using translation keys, violating the TODO item to "make all text translatable."

```java
private static final Tooltip TOOLTIP_ON = Tooltip.create(Component.literal("Remove Bookmark"));
private static final Tooltip TOOLTIP_OFF = Tooltip.create(Component.literal("Add Bookmark"));
```

**Fix**: Use translation keys:

```java
private static final Tooltip TOOLTIP_ON = Tooltip.create(Component.translatable("chiselmon.tooltip.bookmark.remove"));
private static final Tooltip TOOLTIP_OFF = Tooltip.create(Component.translatable("chiselmon.tooltip.bookmark.add"));
```

---

### 7. Moon Ball Time Range Check Error
**Location**: `src/main/java/cc/turtl/chiselmon/api/capture/BallBonusEstimator.java:151`

**Issue**: The time check `time > 24000` is always false since `time = ctx.level.getDayTime() % 24000` ensures `time` is in range `[0, 23999]`.

```java
if (time < 12000 || time > 24000)  // Should be time >= 24000 or removed entirely
    return BASE_BONUS;
```

**Fix**:

```java
if (time < 12000)  // Only check for daytime
    return BASE_BONUS;
```

---

### 8. Potential Stale Data in `StorageSlotTooltipState`
**Location**: `src/main/java/cc/turtl/chiselmon/feature/pc/StorageSlotTooltipState.java`

**Issue**: This static state holder could potentially hold stale references to `StorageSlot` objects across screen changes.

**Fix**: Clear state in screen close/init events, or make it an instance field on the screen.

---

## Code Quality Improvements

### 1. Extract Constants for Magic Numbers
**Location**: Various files

**Examples**:
- `BallBonusEstimator.java`: Magic numbers like `8.0f`, `2.5f`, `3.5f` for ball multipliers
- `PokeRodBaitOverlay.java`: Magic numbers like `54`, `14`, `16`, `3`
- `PcEggRenderer.java`: `23`, `25`, `5F`

**Recommendation**: Define named constants with clear descriptions:

```java
private static final float LOVE_BALL_SAME_SPECIES_MULTIPLIER = 8.0f;
private static final float LOVE_BALL_OPPOSITE_GENDER_MULTIPLIER = 2.5f;
```

---

### 2. Reduce Code Duplication in Predicates
**Location**: `src/main/java/cc/turtl/chiselmon/api/predicate/PokemonPredicates.java:22-45`

**Issue**: The label-checking predicates (`IS_LEGENDARY`, `IS_MYTHICAL`, `IS_ULTRABEAST`, `IS_PARADOX`) share nearly identical logic.

**Fix**: Create a helper method:

```java
private static Predicate<Pokemon> hasLabel(String label) {
    return pokemon -> {
        SimpleSpecies species = SimpleSpeciesRegistry.getByName(pokemon.getSpecies().getName());
        return species != null && species.labels.contains(label);
    };
}

public static final Predicate<Pokemon> IS_LEGENDARY = hasLabel("legendary");
public static final Predicate<Pokemon> IS_MYTHICAL = hasLabel("mythical");
// etc.
```

---

### 3. Use Enum for Alert Sound Profiles
**Location**: `src/main/java/cc/turtl/chiselmon/feature/spawnalert/AlertManager.java:120-128`

**Issue**: Sound profiles are defined as a Map but could be more cleanly integrated into the `AlertPriority` enum itself.

**Fix**: Add sound profile as a field in `AlertPriority`:

```java
public enum AlertPriority {
    NONE(0, null),
    CUSTOM(1, new AlertSoundProfile(SoundEvents.NOTE_BLOCK_PLING.value(), 1.18f, 1.0f)),
    // etc.
    
    public final int weight;
    public final AlertSoundProfile soundProfile;
}
```

---

### 4. Improve Resource Management
**Location**: `src/main/java/cc/turtl/chiselmon/api/SimpleSpeciesRegistry.java:33-48`

**Issue**: The try-with-resources could be structured more cleanly and error handling could be more specific.

**Fix**:

```java
try (Stream<Path> walk = Files.walk(speciesRoot)) {
    walk.filter(p -> p.toString().endsWith(".json"))
        .forEach(this::loadSpeciesFile);
} catch (IOException e) {
    Chiselmon.getLogger().error("Failed to walk Cobblemon species directory", e);
}

private void loadSpeciesFile(Path path) {
    try (Reader reader = Files.newBufferedReader(path)) {
        // ...
    } catch (JsonSyntaxException e) {
        Chiselmon.getLogger().warn("Invalid JSON in {}: {}", path.getFileName(), e.getMessage());
    } catch (IOException e) {
        Chiselmon.getLogger().error("Failed to read {}", path.getFileName(), e);
    }
}
```

---

### 5. Add Null Safety Annotations
**Location**: Throughout the codebase

**Issue**: Inconsistent use of `@NotNull` and `@Nullable` annotations.

**Recommendation**: Add JetBrains annotations to all public API methods for better IDE support and null safety.

---

### 6. Consolidate Mixin Classes
**Location**: `src/main/java/cc/turtl/chiselmon/mixin/pc/`

**Issue**: There are duplicate class names (`StorageSlotMixin.java`) in different packages, which can be confusing. Also, both `pc.sort.PCGUIMixin` and `pc.tab.PCGUIMixin` exist for the same target class.

**Recommendation**: Consolidate mixins for the same target class or use clear naming conventions like `PCGUISortMixin` and `PCGUITabMixin`.

---

### 7. Use Builder Pattern for Complex Configuration
**Location**: `src/main/java/cc/turtl/chiselmon/api/capture/BallBonusEstimator.java:293-310`

**Issue**: The `CaptureContext` class has a long constructor with many parameters.

**Fix**: Consider using a builder pattern for better readability.

---

## Performance & Efficiency Improvements

### 1. Cache Type Effectiveness Calculations
**Location**: `src/main/java/cc/turtl/chiselmon/api/util/TypeEffectivenessUtil.java`

**Issue**: `getSuperEffectiveTypes()` iterates through all 18 types every time it's called.

**Fix**: Cache results for type combinations since they don't change:

```java
private static final Map<Set<ElementalType>, List<ElementalType>> SUPER_EFFECTIVE_CACHE = 
    new ConcurrentHashMap<>();

public static List<ElementalType> getSuperEffectiveTypes(Iterable<ElementalType> defenders) {
    Set<ElementalType> key;
    if (defenders instanceof Set) {
        key = (Set<ElementalType>) defenders;
    } else if (defenders instanceof Collection) {
        key = new HashSet<>((Collection<? extends ElementalType>) defenders);
    } else {
        key = new HashSet<>();
        defenders.forEach(key::add);
    }
    
    return SUPER_EFFECTIVE_CACHE.computeIfAbsent(key, k -> 
        ElementalTypes.all().stream()
            .filter(type -> isSuperEffective(type, k))
            .toList()
    );
}
```

---

### 2. Optimize Species Registry Lookup
**Location**: `src/main/java/cc/turtl/chiselmon/api/SimpleSpeciesRegistry.java`

**Issue**: Species names are lowercased on every lookup.

**Fix**: Pre-normalize keys during loading:

```java
// Already done correctly, but ensure consistency
FULL_DATA.put(species.name.toLowerCase(Locale.ROOT), species);
```

---

### 3. Reduce Object Allocation in Render Methods
**Location**: Various render methods

**Issue**: `String.format()` and string concatenation in render methods create garbage every frame.

**Fix**: Pre-compute formatted strings where possible, or use mutable string builders that are reused.

---

### 4. Lazy Initialization for Expensive Computations
**Location**: `src/main/java/cc/turtl/chiselmon/api/util/PokemonCalcUtil.java:52-70`

**Issue**: `getPossibleMoves()` creates new `ArrayList` and `LinkedHashSet` instances every call.

**Recommendation**: Cache results per Pokemon/level combination if called frequently.

---

### 5. Optimize Predicate Composition
**Location**: `src/main/java/cc/turtl/chiselmon/api/predicate/PokemonPredicates.java:46`

**Issue**: The `IS_SPECIAL` predicate chains multiple predicates with `or()`, which creates wrapper objects.

**Fix**: Combine into a single predicate:

```java
public static final Predicate<Pokemon> IS_SPECIAL = pokemon -> {
    SimpleSpecies species = SimpleSpeciesRegistry.getByName(pokemon.getSpecies().getName());
    if (species == null) return false;
    return species.labels.contains("legendary") 
        || species.labels.contains("mythical")
        || species.labels.contains("ultra_beast")
        || species.labels.contains("paradox");
};
```

---

### 6. Use Primitive Collections Where Possible
**Location**: `src/main/java/cc/turtl/chiselmon/api/util/TypeEffectivenessUtil.java`

**Issue**: The `TYPE_INDICES` map uses `Integer` objects as values.

**Fix**: If using a library like Eclipse Collections or Trove, consider primitive maps for better performance.

---

## Feature Suggestions

### 1. PC Box Fullness Indicator (from TODO)
**Description**: Display a percentage showing how full the entire PC is.

**Implementation**:
```java
public static float calculatePcFullness(ClientPC pc) {
    int totalSlots = 0;
    int filledSlots = 0;
    for (ClientBox box : pc.getBoxes()) {
        List<Pokemon> slots = box.getSlots();
        totalSlots += slots.size();
        for (Pokemon slot : slots) {
            if (slot != null) filledSlots++;
        }
    }
    return totalSlots > 0 ? (float) filledSlots / totalSlots : 0f;
}
```

---

### 2. Quick Box Search/Filter
**Description**: Allow users to type in a search query to highlight Pokemon matching criteria (e.g., species name, type, ability).

**Benefits**: Greatly improves PC navigation for large collections.

---

### 3. Bulk Actions for PC
**Description**: Multi-select Pokemon for bulk operations like moving to another box.

**Note**: Mentioned in TODO as "improve the multiselect feature from morecobblemontweaks."

---

### 4. Spawn Statistics Overlay
**Description**: Display running statistics of spawns seen during the current session (leveraging the existing `SpawnLoggerFeature`).

**Example**: "Shinies: 2 | Legendaries: 1 | Total: 156"

---

### 5. Custom Icon Conditions
**Description**: Allow users to configure their own icon display rules (e.g., show an icon for Pokemon with specific marks or abilities).

---

### 6. Catch Rate Accuracy Indicator
**Description**: Show confidence level or variance in catch rate calculations, especially for edge cases where the estimate might be less accurate.

---

### 7. Pokemon Comparison View
**Description**: Side-by-side comparison of two Pokemon's stats, IVs, and abilities.

---

### 8. Sound Customization for Alerts
**Description**: Allow users to choose custom sounds for spawn alerts or even use resource pack sounds.

**Implementation**: Add sound resource location config option in `SpawnAlertConfig`.

---

### 9. Export/Import Bookmarks
**Description**: Allow users to backup and share their PC bookmarks configuration.

---

### 10. Jade Tooltip for Party Pokemon
**Description**: Extend Jade tooltips to show detailed info when hovering over party Pokemon in the GUI.

---

## Summary

The Chiselmon codebase is well-organized and follows good practices overall. The main areas for improvement are:

1. **Thread safety** in the species registry
2. **Memory management** for caches
3. **Internationalization** of hardcoded strings
4. **Code deduplication** in predicates and configurations
5. **Performance optimization** through caching and reduced allocations

The mod has a solid foundation for additional features, and the modular architecture makes it easy to add new functionality.

---

*Report generated: 2026-01-07*
