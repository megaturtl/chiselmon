# CobbleAid - Copilot Instructions

## Overview
Client-side Fabric mod for Minecraft 1.21.1 + Cobblemon. Provides PC highlighting, Pokémon sorting, tooltips, and storage features. **52 Java files, ~3700 lines**. Uses Gradle 9.1.0, Mixins, AutoConfig.

## Build Requirements (CRITICAL)

**Java 21 REQUIRED**:
```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
chmod +x ./gradlew  # Run once if permission denied
./gradlew build     # Takes 2-3 minutes
```

**Known Issue**: `loom_version=1.13-SNAPSHOT` may fail locally → **Resolves to 1.13.6 in CI**. Don't modify.
**Expected warning**: `Cannot remap outer...WallpaperEntry` during remapJar (non-critical).

## CI Workflow (.github/workflows/build.yml)
Runs on: ubuntu-24.04, Java 21 (Microsoft dist)
Steps: checkout → validate wrapper → setup JDK → `chmod +x gradlew` → `./gradlew build` → upload artifacts
Output: `build/libs/cobbleaid-<version>+<mc-version>.jar`

## Project Structure

**Root**: build.gradle, gradle.properties, settings.gradle, gradlew, LICENSE, README.md, TODO
**Source**: src/main/java/cc/turtl/cobbleaid/ & src/main/resources/

```
├── CobbleAid.java - Main entry (ClientModInitializer), singleton
├── WorldDataManager.java - Per-world persistence
├── api/
│   ├── capture/ - BallBonusEstimator, CaptureChanceEstimator
│   ├── comparator/ - PokemonComparators (SIZE_COMPARATOR, IVS_COMPARATOR)
│   ├── format/ - FormatUtil, PokemonFormatUtil
│   ├── predicate/ - PokemonPredicates (IS_SHINY, HAS_HIDDEN_ABILITY, etc.)
│   ├── render/ - TextRenderUtil
│   └── util/ - CalcUtil (IVs/abilities/moves), ColorUtil
├── command/ - Brigadier commands: CobbleAidCommand (/cobbleaid, /ca)
│   Subcommands: info, config, debug (dump), egg
├── config/
│   ├── ModConfig.java - AutoConfig with nested classes
│   ├── CobbleAidLogger.java - SLF4J wrapper
│   └── ModMenuIntegration.java
├── feature/
│   ├── hud/ - PokeRodBaitOverlay
│   └── pc/ - PC features (icon rendering, sorting, tabs, tooltips)
├── integration/ - jade/, neodaycare/
├── mixin/ - SpongePowered mixins (pc/, GuiMixin)
└── util/ - MiscUtil, ObjectDumper (reflection), StringUtil

resources/
├── fabric.mod.json - Metadata, entrypoints, dependencies
├── cobbleaid.mixins.json - Mixin config (JAVA_21 compatibility)
└── assets/cobbleaid/
    ├── icon.png (64x64)
    ├── lang/en_us.json
    └── textures/gui/pc/ - 9x9 icons (ability_patch, bottle_cap, etc.)
```

## Key Files & Configuration

**fabric.mod.json**:
- Entry points: CobbleAid (client), ModMenuIntegration (modmenu), CobbleAidJadePlugin (jade)
- Dependencies: Fabric Loader ≥0.17.3, MC ~1.21.1, Java ≥21, Fabric API, Cobblemon ≥1.7.0
- Optional: Jade ≥15.10.2

**build.gradle**:
- Fabric Loom 1.13-SNAPSHOT (→ 1.13.6 in CI), Kotlin 2.2.21
- Mappings: Official Mojang + Parchment 2024.07.28
- Target: Java 21

**gradle.properties** (see file for current versions):
- Minecraft 1.21.1, Fabric Loader 0.17.3, Fabric API 0.116.7+1.21.1
- Cobblemon 1.7.1, Cloth Config 15.0.140, Mod Menu 11.0.3
- Mod version: 1.0.1+1.21.1

## Code Patterns

**Entry Point** (CobbleAid.java):
```java
public class CobbleAid implements ClientModInitializer {
    private static CobbleAid INSTANCE;
    public static CobbleAid getInstance() { return INSTANCE; }
    public ModConfig getConfig() { return config; }
}
```

**Config Access**:
```java
ModConfig config = CobbleAid.getInstance().getConfig();
if (config.modDisabled) return;
```

**Logging**:
```java
CobbleAidLogger LOGGER = CobbleAid.getLogger();
LOGGER.info("Message with {} args", value);
```

**Mixins** (always prefix with `cobbleaid$`):
```java
@Mixin(TargetClass.class)
public class TargetClassMixin {
    @Shadow public abstract Type method();
    
    @Inject(method = "renderSlot", at = @At("TAIL"), remap = false)
    private void cobbleaid$renderCustom(GuiGraphics ctx, ..., CallbackInfo ci) {
        if (config.modDisabled) return;
        // Implementation
    }
}
```

**Predicates** (functional style):
```java
// PokemonPredicates.java
public static final Predicate<Pokemon> IS_SHINY = Pokemon::getShiny;
public static final Predicate<Pokemon> HAS_HIGH_IVS = pokemon -> {
    ModConfig config = CobbleAid.getInstance().getConfig();
    return CalcUtil.countPerfectIVs(pokemon.getIvs()) >= config.threshold.maxIvs;
};
```

**Comparators**:
```java
// PokemonComparators.java
public static final Comparator<Pokemon> SIZE_COMPARATOR = 
    Comparator.nullsLast(Comparator.comparingDouble(Pokemon::getScaleModifier));
```

**Resource Locations**:
```java
ResourceLocation icon = ResourceLocation.fromNamespaceAndPath(
    "cobbleaid", "textures/gui/pc/icon_name.png");
```

## Development Workflow

**Adding PC Icon**:
1. Add 9x9 PNG to `resources/assets/cobbleaid/textures/gui/pc/`
2. Define constant in `PcIconRenderer` (`ICON_SIZE=9`, `ICON_RENDER_SIZE=5`)
3. Add predicate to `PokemonPredicates`
4. Add config toggle to `ModConfig.PcConfig.PcIconConfig`
5. Render in `PcIconRenderer.renderIconElements()` with Z-index 99.0

**Adding Command**:
1. Create class in `command/` returning `LiteralArgumentBuilder<FabricClientCommandSource>`
2. Register in `CobbleAidCommand.registerCommand()` with `.then(YourCommand.register())`
3. Use colored output: `Component.literal("§d=== Header ===")`

**Adding Mixin**:
1. Create in `mixin/` (e.g., `mixin/pc/FeatureMixin.java`)
2. Add to `cobbleaid.mixins.json` under `"client"` array
3. Use `@Inject`, `@Shadow`, prefix methods with `cobbleaid$`
4. Set `remap = false` for Cobblemon targets

**Configuration**:
- Modify `ModConfig.java` nested classes
- AutoConfig generates GUI automatically
- Use `@ConfigEntry.Gui.Tooltip`, `.Category`, `.CollapsibleObject`

## Testing

**NO automated tests**. Manual validation required:
1. Build: `./gradlew build`
2. Check: `build/libs/cobbleaid-*.jar` exists
3. Install in MC 1.21.1 + Fabric + Cobblemon
4. Test in-game

**Debug Commands**:
- `/cobbleaid debug dump <slot>` - Dumps party Pokémon
- `/cobbleaid debug dump look` - Dumps targeted entity
- Output to logs via `ObjectDumper.logObjectFields()` (MAX_DEPTH=2)

## Key Utilities

**CalcUtil**:
- `countUniqueAbilities(Species)` - Distinct abilities
- `countPerfectIVs(IVs)` - IVs == 31
- `getPossibleMoves(Pokemon, preferLatest)` - Wild moveset

**ObjectDumper**:
- `logObjectFields(logger, obj)` - Recursive reflection (depth 2)
- Filters Java core classes, used by debug commands

## Constants
```java
CobbleAid.MODID = "cobbleaid"
CobbleAid.VERSION = "1.0.1"
PcIconRenderer.ICON_SIZE = 9, ICON_RENDER_SIZE = 5, Z_INDEX_MAX = 99.0
IVs.MAX_VALUE = 31
```

## Dependencies
- Fabric API, Fabric Language Kotlin (runtime only, code is Java)
- Cobblemon 1.7.1 (base mod), Cloth Config (AutoConfig), Mod Menu
- Jade (optional tooltip enhancement)

## Critical Notes
- **CLIENT-SIDE ONLY** - No server code
- **Java 21 required** - CI uses 21.0.2 (Microsoft)
- **Trust these instructions** - Search only if incomplete/incorrect
- **CI is authoritative** - Local SNAPSHOT issue OK if CI passes
- **Mixin fragility** - Test thoroughly, breaks with Cobblemon updates
- **Null safety** - Use `Comparator.nullsLast()`, check config nulls
- **Z-index** - Use 99.0 for overlays to render on top
