# Chiselmon Codebase Analysis Report

This report provides a comprehensive analysis of the Chiselmon mod, including potential bugs, code quality improvements, performance optimizations, and feature suggestions.

> **Last Updated**: 2026-01-08
> 
> **Note**: Items marked with ✅ have been fixed in the dev branch.

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Potential Bugs](#potential-bugs)
3. [Code Quality Improvements](#code-quality-improvements)
4. [Performance & Efficiency Improvements](#performance--efficiency-improvements)
5. [Feature Suggestions](#feature-suggestions)
6. [Remaining Code Edits](#remaining-code-edits)

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

### ✅ 1. Race Condition in `SimpleSpeciesRegistry.loadAsync()`
**Status**: FIXED in commit 2e561b3

**Location**: `src/main/java/cc/turtl/chiselmon/api/SimpleSpeciesRegistry.java`

---

### ✅ 2. Memory Leak in `NeoDaycareEgg.DUMMY_CACHE`
**Status**: FIXED in commit 6e7db11 - Added cache clearing on world disconnect

---

### ✅ 3. Potential NPE in `PokemonFormatUtil.marks()`
**Status**: FIXED in commit 6e7db11 - Added debug logging

---

### ✅ 4. Incorrect Component Equality Check
**Status**: FIXED in commit 6e7db11

---

### ✅ 5. Missing Null Check in `PCTabManager.createTabButtons()`
**Status**: FIXED in commit 6e7db11

---

### ✅ 6. Hardcoded Text in UI Components
**Status**: FIXED in commit 6e7db11 - Added translation keys

---

### ✅ 7. Moon Ball Time Range Check Error
**Status**: FIXED in commit 6e7db11

---

### 8. Potential Stale Data in `StorageSlotTooltipState`
**Status**: PENDING

**Location**: `src/main/java/cc/turtl/chiselmon/feature/pc/StorageSlotTooltipState.java`

**Issue**: This static state holder could potentially hold stale references to `StorageSlot` objects across screen changes.

**Fix**: Clear state in screen close/init events. See [Remaining Code Edits](#remaining-code-edits) section below.

---

## Code Quality Improvements

### 1. Extract Constants for Magic Numbers
**Status**: PENDING

**Location**: Various files

**Examples**:
- `BallBonusEstimator.java`: Magic numbers like `8.0f`, `2.5f`, `3.5f` for ball multipliers
- `PokeRodBaitOverlay.java`: Magic numbers like `54`, `14`, `16`, `3`
- `PcEggRenderer.java`: `23`, `25`, `5F`

**Recommendation**: Define named constants with clear descriptions. See [Remaining Code Edits](#remaining-code-edits).

---

### ✅ 2. Reduce Code Duplication in Predicates
**Status**: FIXED in commit 6e7db11 - Added `hasLabel()` helper method

---

### ✅ 3. Use Enum for Alert Sound Profiles
**Status**: FIXED in commit 6e7db11 - Moved sound profiles to `AlertPriority` enum

---

### 4. Improve Resource Management
**Status**: PENDING (Optional improvement)

**Location**: `src/main/java/cc/turtl/chiselmon/api/SimpleSpeciesRegistry.java:33-48`

---

### 5. Add Null Safety Annotations
**Status**: PENDING (Optional improvement)

**Location**: Throughout the codebase

**Recommendation**: Add JetBrains annotations to all public API methods for better IDE support and null safety.

---

### 6. Consolidate Mixin Classes
**Status**: PENDING (Optional refactoring)

**Location**: `src/main/java/cc/turtl/chiselmon/mixin/pc/`

**Issue**: There are duplicate class names (`StorageSlotMixin.java`) in different packages. Also, both `pc.sort.PCGUIMixin` and `pc.tab.PCGUIMixin` exist for the same target class.

**Recommendation**: Use clear naming conventions like `PCGUISortMixin` and `PCGUITabMixin`.

---

### 7. Use Builder Pattern for Complex Configuration
**Status**: PENDING (Optional improvement)

**Location**: `src/main/java/cc/turtl/chiselmon/api/capture/BallBonusEstimator.java:293-310`

**Issue**: The `CaptureContext` class has a long constructor with many parameters.

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

The Chiselmon codebase is well-organized and follows good practices overall. Many issues from the original report have been addressed in commits 2e561b3 and 6e7db11. The remaining areas for improvement are:

1. ~~**Thread safety** in the species registry~~ ✅ Fixed
2. ~~**Memory management** for caches~~ ✅ Fixed
3. ~~**Internationalization** of hardcoded strings~~ ✅ Fixed
4. ~~**Code deduplication** in predicates and configurations~~ ✅ Fixed
5. **Performance optimization** through caching (type effectiveness) - Optional
6. **Stale state** in StorageSlotTooltipState - Pending

---

## Remaining Code Edits

This section contains specific code changes for items that haven't been addressed yet.

### 1. Fix Stale State in `StorageSlotTooltipState`

**File**: `src/main/java/cc/turtl/chiselmon/feature/pc/StorageSlotTooltipState.java`

The static state could hold stale references. Add a method to also clear mouse coordinates and ensure it's called from the mixin when the screen closes.

**Current Code**:
```java
public static void clear() {
    hoveredSlot = null;
}
```

**Replace with**:
```java
public static void clear() {
    hoveredSlot = null;
    tooltipMouseX = 0;
    tooltipMouseY = 0;
}
```

Then ensure `StorageSlotTooltipState.clear()` is called when PCGUI is closed. Add this to one of the PCGUI mixins:

```java
@Inject(method = "removed", at = @At("HEAD"))
private void chiselmon$clearTooltipStateOnClose(CallbackInfo ci) {
    StorageSlotTooltipState.clear();
}
```

---

### 2. Add Type Effectiveness Caching (Performance)

**File**: `src/main/java/cc/turtl/chiselmon/api/util/TypeEffectivenessUtil.java`

Add caching to avoid recalculating super effective types on every Jade tooltip render.

**Add these imports**:
```java
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
```

**Add this field after the TYPE_INDICES map**:
```java
// Cache for super effective type lookups
private static final Map<Set<ElementalType>, List<ElementalType>> SUPER_EFFECTIVE_CACHE = 
    new ConcurrentHashMap<>();
```

**Replace `getSuperEffectiveTypes(Iterable<ElementalType> defenders)` method**:
```java
public static List<ElementalType> getSuperEffectiveTypes(Iterable<ElementalType> defenders) {
    if (defenders == null) {
        return List.of();
    }
    
    // Convert to Set for cache key
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

### 3. Extract Magic Numbers in BallBonusEstimator (Optional)

**File**: `src/main/java/cc/turtl/chiselmon/api/capture/BallBonusEstimator.java`

Add named constants for ball multipliers at the top of the class:

```java
// --- Ball Multiplier Constants ---
private static final float BASE_BONUS = 1.0f;
private static final float MASTER_BONUS = 999.0f;
private static final float NET_BALL_MULTIPLIER = 3.0f;

// Love Ball
private static final float LOVE_BALL_SAME_SPECIES = 8.0f;
private static final float LOVE_BALL_OPPOSITE_GENDER = 2.5f;

// Park Ball
private static final float PARK_BALL_TEMPERATE = 2.5f;

// Moon Ball
private static final float MOON_BALL_FULL_MOON = 4.0f;
private static final float MOON_BALL_GIBBOUS = 2.5f;
private static final float MOON_BALL_HALF = 1.5f;

// Heavy Ball
private static final float HEAVY_BALL_TIER3 = 4.0f;
private static final float HEAVY_BALL_TIER2 = 2.5f;
private static final float HEAVY_BALL_TIER1 = 1.5f;
private static final float HEAVY_WEIGHT_TIER3 = 3000f;
private static final float HEAVY_WEIGHT_TIER2 = 2000f;
private static final float HEAVY_WEIGHT_TIER1 = 1000f;

// Dusk Ball
private static final float DUSK_BALL_DARK = 3.5f;
private static final float DUSK_BALL_DIM = 3.0f;
private static final int DUSK_BRIGHTNESS_THRESHOLD = 7;

// Level Ball
private static final float LEVEL_BALL_4X = 4.0f;
private static final float LEVEL_BALL_3X = 3.0f;
private static final float LEVEL_BALL_2X = 2.0f;

// Repeat Ball
private static final float REPEAT_BALL_CAUGHT = 3.5f;

// Timer Ball
private static final float TIMER_BALL_MAX = 4.0f;
private static final float TIMER_BALL_MULTIPLIER = 1229F / 4096F;

// Quick Ball
private static final float QUICK_BALL_TURN1 = 5.0f;

// Safari Ball
private static final float SAFARI_BALL_PASSIVE = 1.5f;

// Fast Ball
private static final float FAST_BALL_FAST = 4.0f;
private static final int FAST_SPEED_THRESHOLD = 100;

// Lure Ball
private static final float LURE_BALL_FISHED = 4.0f;

// Dive Ball
private static final float DIVE_BALL_UNDERWATER = 3.5f;

// Dream Ball
private static final float DREAM_BALL_ASLEEP = 4.0f;

// Beast Ball
private static final float BEAST_BALL_UB = 5.0f;
```

---

### 4. Rename Duplicate Mixin Classes (Optional Refactoring)

**Current structure**:
```
mixin/pc/StorageSlotMixin.java           
mixin/pc/tooltip/StorageSlotMixin.java   <- Duplicate name!
mixin/pc/sort/PCGUIMixin.java
mixin/pc/tab/PCGUIMixin.java             <- Duplicate name!
```

**Suggested renames**:
- `mixin/pc/StorageSlotMixin.java` → `mixin/pc/StorageSlotIconMixin.java`
- `mixin/pc/tooltip/StorageSlotMixin.java` → `mixin/pc/tooltip/StorageSlotTooltipMixin.java`
- `mixin/pc/sort/PCGUIMixin.java` → `mixin/pc/sort/PCGUISortMixin.java`
- `mixin/pc/tab/PCGUIMixin.java` → `mixin/pc/tab/PCGUITabMixin.java`

Remember to update `chiselmon.mixins.json` accordingly.

---

*Report updated: 2026-01-08*
