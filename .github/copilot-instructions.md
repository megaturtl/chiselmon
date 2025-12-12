# CobbleAid - Copilot Coding Agent Instructions

## Repository Overview

**CobbleAid** is a client-side Minecraft mod for Cobblemon that provides quality-of-life features including PC highlighting filters, Pokémon sorting, tooltips, and storage management. This is a Fabric mod for Minecraft 1.21.1.

### Project Statistics
- **Size**: ~1.1MB, 133 files total
- **Language**: Java (52 source files) with Kotlin support
- **Framework**: Fabric Mod Loader with Mixin support
- **Build Tool**: Gradle 9.1.0
- **Target Runtime**: Java 21

## Critical Build Information

### Java Version Requirement
**ALWAYS use Java 21.** The project will NOT build with Java 17 or other versions.

```bash
# Set Java 21 before any Gradle command
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
```

### Gradle Wrapper Setup
**CRITICAL**: The `gradlew` script must have execute permissions before running any Gradle commands:

```bash
chmod +x ./gradlew
```

This step is REQUIRED and CI automatically handles it. Without it, you'll get "Permission denied" errors.

### Build Commands

**To build the project:**
```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
chmod +x ./gradlew
./gradlew build
```

**Build timing**: Expect 2-3 minutes for a full build (CI average: ~3 minutes)

**To clean the project:**
```bash
./gradlew clean
```

**Common Gradle tasks:**
- `./gradlew build` - Full build with tests
- `./gradlew jar` - Build JAR without remapping
- `./gradlew remapJar` - Remap the JAR for Minecraft
- `./gradlew tasks` - List all available tasks

### Known Build Issues & Workarounds

**Fabric Loom SNAPSHOT Version Issue:**
The `gradle.properties` specifies `loom_version=1.13-SNAPSHOT`. In local environments, this SNAPSHOT version may not resolve, causing build failures with:
```
Plugin [id: 'fabric-loom', version: '1.13-SNAPSHOT'] was not found
```

**Workaround**: The CI environment successfully resolves this to Fabric Loom 1.13.6. If you encounter this issue locally, the build will still succeed in CI. Do NOT modify the `gradle.properties` unless specifically required for the task.

**Remap Warning (Non-Critical):**
During `remapJar`, you may see:
```
Cannot remap outer because it does not exists in any of the targets [com/cobblemon/mod/common/client/gui/pc/WallpapersScrollingWidget$WallpaperEntry]
```
This is a known non-critical warning and does not affect the build.

## Project Structure

### Root Directory Files
```
.gitattributes         - Git attributes configuration
.gitignore            - Git ignore patterns
.github/              - GitHub workflows and configurations
  workflows/
    build.yml         - CI build workflow
LICENSE               - CC0-1.0 License
README.md             - Project overview and PC highlight feature
TODO                  - Feature wishlist and development notes
build.gradle          - Gradle build configuration
gradle.properties     - Project properties and dependency versions
gradlew, gradlew.bat  - Gradle wrapper scripts
settings.gradle       - Gradle settings
src/                  - Source code
```

### Source Code Organization (52 Java files, ~3700 lines)

```
src/main/java/cc/turtl/cobbleaid/
├── CobbleAid.java              - Main mod entry point (ClientModInitializer)
│                                 Singleton pattern, initializes config & commands
├── WorldDataManager.java       - Per-world data persistence manager
├── WorldDataStore.java         - World data storage interface
│
├── api/                        - Public APIs with functional programming style
│   ├── capture/
│   │   ├── BallBonusEstimator.java     - Pokéball catch rate bonuses
│   │   └── CaptureChanceEstimator.java - Complex capture probability calculator
│   ├── comparator/
│   │   └── PokemonComparators.java     - SIZE_COMPARATOR, IVS_COMPARATOR
│   ├── format/
│   │   ├── FormatUtil.java            - General text formatting
│   │   └── PokemonFormatUtil.java     - Pokémon-specific display formatting
│   ├── predicate/
│   │   ├── PokemonPredicates.java     - IS_SHINY, HAS_HIDDEN_ABILITY, etc.
│   │   └── MovePredicates.java        - Move-related predicates
│   ├── render/
│   │   └── TextRenderUtil.java        - GuiGraphics text utilities
│   └── util/
│       ├── CalcUtil.java              - IV/ability/moveset calculations
│       └── ColorUtil.java             - RGBA color manipulation
│
├── command/                    - Brigadier-based client commands (/cobbleaid)
│   ├── CobbleAidCommand.java         - Main command router & registration
│   ├── ConfigCommand.java            - Config management commands
│   ├── DebugCommand.java             - Debug & dump commands
│   ├── EggCommand.java               - Egg-related commands
│   ├── InfoCommand.java              - Info display
│   └── suggestion/                   - Command auto-completion providers
│
├── config/                     - AutoConfig (Cloth Config) integration
│   ├── ModConfig.java               - Main config with nested classes
│   ├── CobbleAidLogger.java         - Custom SLF4J wrapper
│   └── ModMenuIntegration.java      - Mod Menu config screen entry
│
├── feature/                    - Feature implementations
│   ├── hud/
│   │   └── PokeRodBaitOverlay.java   - HUD overlays
│   └── pc/                           - PC (Pokémon storage) features
│       ├── PcIconRenderer.java       - Icon rendering (shiny, HA, etc.)
│       ├── neodaycare/
│       │   └── PcEggRenderer.java    - Egg preview in PC
│       ├── sort/
│       │   ├── PcSorter.java         - Box sorting logic
│       │   └── PcSortUIHandler.java  - Sort button UI
│       ├── tab/                      - PC bookmark tab system
│       │   ├── PCTab.java           - Tab data structure
│       │   ├── PCTabStore.java      - Tab persistence
│       │   ├── PCTabManager.java    - Tab logic & creation
│       │   ├── PCTabButton.java     - Tab button widget
│       │   └── PCBookmarkButton.java - Bookmark toggle button
│       └── tooltip/
│           └── StorageSlotTooltipState.java - Tooltip state management
│
├── integration/                - Optional mod integrations
│   ├── jade/
│   │   ├── CobbleAidJadePlugin.java   - Jade plugin entrypoint
│   │   ├── PokemonProvider.java       - Pokémon entity tooltip
│   │   └── PokeSnackProvider.java     - Snack block tooltip
│   └── neodaycare/
│       ├── NeoDaycareEgg.java         - Egg wrapper
│       └── NeoDaycareDummyPokemon.java - Dummy Pokémon for eggs
│
├── mixin/                      - Mixin injections (SpongePowered)
│   ├── GuiMixin.java                 - General GUI modifications
│   └── pc/                           - PC-specific mixins
│       ├── StorageSlotMixin.java     - Slot rendering injection
│       ├── neodaycare/               - Egg preview mixins
│       ├── sort/
│       │   └── PCGUIMixin.java       - Sort button integration
│       ├── tab/
│       │   └── PCGUIMixin.java       - Tab system integration
│       ├── tooltip/                  - Tooltip enhancement mixins
│       └── wallpaper/
│           └── WallpaperEntryMixin.java
│
└── util/                       - General utilities
    ├── MiscUtil.java                 - Miscellaneous helpers
    ├── ObjectDumper.java             - Reflection-based object inspector
    └── StringUtil.java               - String manipulation

src/main/resources/
├── fabric.mod.json            - Mod metadata, entrypoints, dependencies
├── cobbleaid.mixins.json      - Mixin targets (JAVA_21 compatibility)
└── assets/cobbleaid/
    ├── icon.png              - 64x64 mod icon
    ├── lang/en_us.json       - All translatable strings
    └── textures/gui/pc/      - 9x9 icon PNGs (ability_patch, bottle_cap,
                                 saddle, shiny_sparkle, size_shroom, etc.)
```

### Key Configuration Files

**build.gradle**: 
- Fabric Loom plugin (1.13-SNAPSHOT → resolves to 1.13.6 in CI)
- Kotlin plugin 2.2.21
- Java/Kotlin target: Java 21
- Mappings: Official Mojang + Parchment

**gradle.properties**:
- Minecraft version: 1.21.1
- Fabric Loader: 0.17.3
- Fabric API: 0.116.7+1.21.1
- Cobblemon: 1.7.1
- Mod version: 1.0.1+1.21.1

**fabric.mod.json**:
- Client-side only mod (environment: "*")
- Entry points: CobbleAid (client), ModMenuIntegration (modmenu), CobbleAidJadePlugin (jade)
- Dependencies: Fabric Loader ≥0.17.3, Minecraft ~1.21.1, Java ≥21, Fabric API, Cobblemon ≥1.7.0
- Optional: Jade ≥15.10.2

**cobbleaid.mixins.json**:
- Compatibility level: JAVA_21
- All mixins are client-side
- Mixins modify Cobblemon PC GUI, storage slots, and wallpaper components

## CI/CD Pipeline

### GitHub Actions Workflow (.github/workflows/build.yml)

**Trigger**: Every push and pull request

**Steps**:
1. Checkout repository
2. Validate Gradle wrapper
3. Setup JDK 21 (Microsoft distribution)
4. Make Gradle wrapper executable (`chmod +x ./gradlew`)
5. Build (`./gradlew build`)
6. Upload artifacts from `build/libs/`

**Environment**: ubuntu-24.04

**Build artifacts**: Located in `build/libs/` after successful build

### Validation Before Committing

Run these checks before pushing changes:

1. **Build**: `./gradlew build` (must succeed)
2. **Check for compilation errors**: Review build output
3. **Verify JAR creation**: Check `build/libs/cobbleaid-1.0.1+1.21.1.jar` exists

## Development Guidelines

### Code Organization & Patterns

**Entry Point**: `CobbleAid.java` implements `ClientModInitializer`
- `onInitializeClient()` is the starting point
- Registers commands via `CobbleAidCommand.register()`
- Loads configuration using AutoConfig with `ModConfig`
- Initializes `WorldDataManager` for per-world persistent data

**Configuration Pattern**: Uses AutoConfig (Cloth Config) with nested classes
- Main config: `ModConfig.java` with nested static classes for categories
- `@ConfigEntry.Gui.Tooltip` for tooltips
- `@ConfigEntry.Gui.CollapsibleObject` for collapsible sections
- `@ConfigEntry.Gui.Category` for tabs in config GUI
- Access via `CobbleAid.getInstance().getConfig()`

**Logging Pattern**: Custom `CobbleAidLogger` wrapper around SLF4J
- Supports debug mode toggling at runtime
- Always includes `[cobbleaid]` prefix
- Use `CobbleAid.getLogger()` for consistent logging

**Command Structure**: Brigadier-based client commands
- Base command: `/cobbleaid` (alias: `/ca`)
- Subcommands: `info`, `config`, `debug`, `egg`
- All commands in `command/` package
- Use `ClientCommandManager.literal()` and `.then()` for structure

**Mixin Patterns**:
```java
@Mixin(TargetClass.class)
public class TargetClassMixin {
    @Shadow
    public abstract ReturnType targetMethod();  // Access original methods
    
    @Inject(method = "methodName", at = @At("INJECTION_POINT"), remap = false)
    private void cobbleaid$descriptiveName(..., CallbackInfo ci) {
        // Prefix custom methods with cobbleaid$ to avoid conflicts
        // Check config.modDisabled first!
    }
}
```
- Always use `cobbleaid$` prefix for injected methods
- Set `remap = false` for Cobblemon/mod targets
- Check `config.modDisabled` at method start
- Mixins for PC slots use `@At("TAIL")` or specific `INVOKE` targets

**API Design**: Functional programming patterns
- `PokemonPredicates`: Static `Predicate<Pokemon>` constants (e.g., `IS_SHINY`, `HAS_HIDDEN_ABILITY`)
- `PokemonComparators`: Static `Comparator<Pokemon>` constants (e.g., `SIZE_COMPARATOR`, `IVS_COMPARATOR`)
- Use `Comparator.nullsLast()` for safety
- Predicates can be composed: `IS_EXTREME_SIZE = IS_EXTREME_SMALL.or(IS_EXTREME_LARGE)`

**Resource Locations**: Use modern `ResourceLocation.fromNamespaceAndPath()` syntax
```java
ResourceLocation icon = ResourceLocation.fromNamespaceAndPath(
    "cobbleaid", "textures/gui/pc/icon_name.png");
```

**Rendering**: All GUI modifications use `GuiGraphics` (modern Minecraft API)
- Z-index manipulation for layering: `context.pose().translate(0, 0, Z_INDEX)`
- Use `context.blit()` for texture rendering with proper UV coordinates
- Render in mixins that inject into `renderSlot` or `renderWidget`

### Making Code Changes

1. **Adding PC Icons**: 
   - Define icon in `PcIconRenderer.renderIconElements()`
   - Add predicate in `PokemonPredicates`
   - Add config toggle in `ModConfig.PcConfig.PcIconConfig`
   - Place 9x9 PNG texture in `resources/assets/cobbleaid/textures/gui/pc/`

2. **Adding Predicates**:
   - Use functional style in `PokemonPredicates`
   - Access config via `CobbleAid.getInstance().getConfig()`
   - Leverage `CalcUtil` for complex calculations (IVs, abilities, moves)

3. **Adding Commands**:
   - Create class in `command/` extending the pattern from existing commands
   - Return `LiteralArgumentBuilder<FabricClientCommandSource>`
   - Register in `CobbleAidCommand.registerCommand()`
   - Use colored output: `Component.literal("§d=== Header ===")`

4. **Adding Mixins**:
   - Target Cobblemon classes (usually in `com.cobblemon.mod.common.client.gui.pc`)
   - Add to `cobbleaid.mixins.json` under `"client"` array
   - Follow naming: `pc.SomeFeature.TargetClassMixin`
   - Test thoroughly - mixins can break with Cobblemon updates

5. **Configuration**:
   - Add fields to appropriate nested class in `ModConfig`
   - AutoConfig handles GUI generation automatically
   - Hidden data (like `worldDataMap`) uses `@ConfigEntry.Gui.Excluded`

### Testing Changes

**No automated tests exist**. Manual validation required:
1. Build: `./gradlew build` (3 minutes)
2. Check output: `build/libs/cobbleaid-1.0.1+1.21.1.jar`
3. Install in Minecraft 1.21.1 + Fabric + Cobblemon
4. Test affected features in-game
5. Check logs for errors: Enable debug mode via config or `/cobbleaid config`

**Debug Tools**:
- `/cobbleaid debug dump <slot>` - Dumps party Pokémon data
- `/cobbleaid debug dump look` - Dumps targeted Pokémon entity
- Uses `ObjectDumper.logObjectFields()` for deep reflection-based inspection
- Debug output goes to game log (not chat)

### Code Style & Conventions

1. **Package Organization**: Features grouped by domain (`api`, `feature`, `mixin`, `integration`)
2. **Null Safety**: Use `Comparator.nullsLast()`, check nulls before operations
3. **Constants**: Use `private static final` for resource locations, sizes, multipliers
4. **Utility Classes**: Final class with private constructor (no instantiation)
5. **Comments**: Minimal inline comments; prefer descriptive naming
6. **Line Length**: ~3700 total lines across 52 files (average 71 lines/file)
7. **No Kotlin**: Despite Kotlin support, all code is Java

### Common Pitfalls

1. **Do NOT change Java version** - Must be Java 21 (CI uses Java 21.0.2)
2. **Do NOT modify `gradlew` permissions in code** - CI handles this in workflow
3. **Do NOT change Fabric Loom version** - SNAPSHOT resolves to 1.13.6 in CI
4. **CLIENT-SIDE ONLY** - No server-side code; uses client-specific Cobblemon APIs
5. **Mixin fragility** - Changes break with Cobblemon updates; test extensively
6. **Config access** - Always null-check: `ModConfig config = CobbleAid.getInstance().getConfig()`
7. **Remap warnings** - Non-critical remapping warnings about `WallpaperEntry` are expected
8. **Z-index layering** - Use high z-values (99.0) for icons to render on top

### Dependencies

- **Fabric API**: Required for Fabric mod functionality
- **Fabric Language Kotlin**: Kotlin support (project uses Java but includes Kotlin runtime)
- **Cobblemon**: The base mod being enhanced (1.7.1) - provides Pokémon mechanics
- **Cloth Config**: Configuration GUI (via AutoConfig)
- **Mod Menu**: Integration for in-game config access
- **Jade** (optional): Enhanced tooltip integration

### Key Utility Classes

**CalcUtil** - Pokémon calculations:
- `countUniqueAbilities(Species)` - Returns distinct ability count
- `countPerfectIVs(IVs)` - Counts IVs equal to 31
- `getPossibleMoves(Pokemon, preferLatest)` - Calculates probable wild moveset

**PokemonPredicates** - Functional predicates:
- `IS_SHINY`, `HAS_HIDDEN_ABILITY`, `HAS_HIGH_IVS`
- `IS_EXTREME_SMALL`, `IS_EXTREME_LARGE`, `IS_EXTREME_SIZE`
- `IS_RIDEABLE`, `HAS_SELF_DAMAGING_MOVE`
- All predicates access config thresholds dynamically

**PokemonComparators** - Sorting:
- `SIZE_COMPARATOR` - Uses `Pokemon.getScaleModifier()`
- `IVS_COMPARATOR` - Uses `IVs.getEffectiveBattleTotal()`
- Both use `nullsLast()` for safety

**ObjectDumper** - Deep reflection inspector:
- `logObjectFields(logger, object)` - Recursively dumps fields
- MAX_RECURSION_DEPTH = 2
- Filters out Java core classes
- Used by debug commands for diagnostics

## Quick Reference

### Important File Locations
- **Main class**: `src/main/java/cc/turtl/cobbleaid/CobbleAid.java`
- **Config**: `src/main/java/cc/turtl/cobbleaid/config/ModConfig.java`
- **Predicates**: `src/main/java/cc/turtl/cobbleaid/api/predicate/PokemonPredicates.java`
- **Comparators**: `src/main/java/cc/turtl/cobbleaid/api/comparator/PokemonComparators.java`
- **Icon rendering**: `src/main/java/cc/turtl/cobbleaid/feature/pc/PcIconRenderer.java`
- **PC sorting**: `src/main/java/cc/turtl/cobbleaid/feature/pc/sort/PcSorter.java`
- **Mixins**: `src/main/java/cc/turtl/cobbleaid/mixin/` (organized by feature)
- **Mixin config**: `src/main/resources/cobbleaid.mixins.json`
- **Resources**: `src/main/resources/assets/cobbleaid/`
- **Translations**: `src/main/resources/assets/cobbleaid/lang/en_us.json`
- **Textures**: `src/main/resources/assets/cobbleaid/textures/gui/pc/` (9x9 PNGs)

### Key Constants
```java
// CobbleAid.java
public static final String MODID = "cobbleaid";
public static final String VERSION = "1.0.1";

// PcIconRenderer.java
private static final int ICON_SIZE = 9;           // Source texture
private static final int ICON_RENDER_SIZE = 5;    // Rendered size
private static final double Z_INDEX_MAX = 99.0;   // Layering

// CaptureChanceEstimator.java
private static final float MAX_POKEMON_LEVEL = 100.0F;
private static final float STATUS_SLEEP_FROZEN = 2.5F;
private static final float STATUS_OTHER = 1.5F;

// CalcUtil.java
IVs.MAX_VALUE = 31  // Perfect IV value
```

### Common Code Patterns

**Accessing Config**:
```java
ModConfig config = CobbleAid.getInstance().getConfig();
if (config.modDisabled) return;
```

**Logging**:
```java
CobbleAidLogger LOGGER = CobbleAid.getLogger();
LOGGER.info("Message with {} args", value);
LOGGER.debug("Debug info");
```

**Resource Location**:
```java
ResourceLocation icon = ResourceLocation.fromNamespaceAndPath(
    "cobbleaid", "textures/gui/pc/icon_name.png");
```

**Predicate Usage**:
```java
if (PokemonPredicates.IS_SHINY.test(pokemon)) {
    // Handle shiny
}
```

**Sorting**:
```java
List<Pokemon> sorted = pokemonList.stream()
    .sorted(PokemonComparators.SIZE_COMPARATOR.reversed())
    .toList();
```

### Common Commands
```bash
# Setup (always first!)
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
chmod +x ./gradlew

# Build
./gradlew build

# Clean
./gradlew clean

# List tasks
./gradlew tasks
```

### Build Artifacts
- Output: `build/libs/cobbleaid-1.0.1+1.21.1.jar`
- Source JAR: Currently disabled in build config

## Important Notes

- **Trust these instructions**: If a command is documented here as working, use it. Only search for additional information if these instructions are incomplete or incorrect.
- **CI is the source of truth**: If something works in CI but not locally, document the difference but maintain CI compatibility.
- **No testing framework**: This project doesn't have automated tests. Changes must be manually validated.
- **Minecraft mod development**: Requires understanding of Fabric, Mixins, and Minecraft/Cobblemon internals.
