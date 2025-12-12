# Migration Guide for Contributors

This guide helps contributors migrate their work to the new modular architecture introduced in the refactoring update.

## Overview of Changes

The refactoring introduces a modular feature system while preserving all existing functionality. The main changes are:

1. **New core infrastructure** (`core/` package)
   - Feature interface and FeatureManager for lifecycle management
   - RegistryHelper for consistent component registration

2. **Enhanced documentation**
   - ARCHITECTURE.md with detailed technical documentation
   - Updated README with developer guidance
   - Example demo feature showing best practices

3. **No breaking changes**
   - All existing features continue to work
   - Configuration is backward compatible
   - Package structure remains largely the same

## Package Structure

### What Changed

```
Before:                          After:
cc.turtl.cobbleaid/             cc.turtl.cobbleaid/
├── CobbleAid.java              ├── CobbleAid.java (enhanced)
├── api/                        ├── core/ (NEW)
├── command/                    │   ├── lifecycle/
├── config/                     │   │   ├── Feature.java
├── feature/                    │   │   └── FeatureManager.java
├── integration/                │   └── registry/
├── mixin/                      │       └── RegistryHelper.java
└── util/                       ├── api/
                                ├── command/
                                ├── config/
                                ├── feature/
                                │   └── demo/ (NEW)
                                ├── integration/
                                ├── mixin/
                                └── util/
```

### What Stayed the Same

- All existing packages remain in their locations
- `api/`, `feature/`, `command/`, `config/`, `integration/`, `mixin/`, `util/` are unchanged
- Individual feature implementations (PC features, HUD, etc.) are unmodified

## Migrating Your Feature

If you're working on a new feature or modifying an existing one:

### Option 1: Use the New Feature System (Recommended)

For new features or major updates:

1. **Create a Feature implementation**:

```java
package cc.turtl.cobbleaid.feature.myfeature;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;

public class MyFeature implements Feature {
    @Override
    public void initialize() {
        // Initialization code here
    }
    
    @Override
    public boolean isEnabled() {
        return CobbleAid.getInstance().getConfig().myFeature.enabled;
    }
}
```

2. **Register your feature** in `CobbleAid.registerFeatures()`:

```java
featureManager.register(new MyFeature());
```

3. **Add configuration** (if needed) in `ModConfig.java`

### Option 2: Continue As-Is

For minor changes to existing features:

- **No migration required**
- Continue working with existing code
- Features will continue to work as before
- Consider migrating to the new system for major updates

## Updated CobbleAid API

### New Methods

```java
// Get the feature manager
CobbleAid.getInstance().getFeatureManager()

// Get the registry helper
CobbleAid.getInstance().getRegistryHelper()
```

### Unchanged Methods

All existing public methods remain available:
- `getInstance()`
- `getLogger()`
- `getConfig()`
- `getConfigHolder()`
- `getWorldData()`
- `reloadConfig()`
- `saveConfig()`

## Common Migration Scenarios

### Scenario 1: Adding a New Feature

**Before**: Manually register components in `CobbleAid.initialize()`

**After**: Implement `Feature` interface and register with `FeatureManager`

```java
// 1. Create feature class
public class MyFeature implements Feature {
    @Override
    public void initialize() {
        // Register event listeners, commands, etc.
    }
    
    @Override
    public boolean isEnabled() {
        return CobbleAid.getInstance().getConfig().myFeature.enabled;
    }
}

// 2. Register in CobbleAid.registerFeatures()
featureManager.register(new MyFeature());
```

### Scenario 2: Organizing Feature Code

**Recommendation**: Group related code in feature-specific packages

```
feature/myfeature/
├── MyFeature.java         # Feature implementation
├── MyFeatureRenderer.java # Rendering logic
├── MyFeatureHelper.java   # Helper utilities
└── MyFeatureConfig.java   # Config classes (if separate from ModConfig)
```

### Scenario 3: Using RegistryHelper

**Before**: Manual registration with error handling

```java
try {
    dispatcher.register(myCommand);
} catch (Exception e) {
    LOGGER.error("Failed to register command", e);
}
```

**After**: Use RegistryHelper

```java
RegistryHelper helper = CobbleAid.getInstance().getRegistryHelper();
helper.registerCommand(dispatcher, myCommand, "my-command");
```

## Configuration Migration

### No Changes Required

Configuration files are fully backward compatible. Users do not need to:
- Modify existing config files
- Reset configuration
- Reconfigure options

### Adding New Config Options

Continue using the existing pattern in `ModConfig.java`:

```java
@ConfigEntry.Gui.CollapsibleObject
public MyFeatureConfig myFeature = new MyFeatureConfig();

public static class MyFeatureConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean enabled = true;
}
```

## Mixin Migration

### No Changes Required

Mixins continue to work as before:
- Same injection points
- Same target classes
- Same mixin configuration

### Best Practices (Unchanged)

- Prefix mixin methods with `cobbleaid$`
- Check `config.modDisabled` early
- Use `remap = false` for Cobblemon targets
- Organize mixins by feature in subdirectories

## Testing Your Migration

### Checklist

- [ ] Code compiles without errors
- [ ] Feature initializes correctly
- [ ] Feature respects enabled/disabled configuration
- [ ] No runtime errors in logs
- [ ] Existing functionality still works
- [ ] Configuration persists correctly

### Test Commands

```bash
# Build the mod
./gradlew build

# Check for compilation errors
./gradlew compileJava

# Run in dev environment (if configured)
./gradlew runClient
```

## Example: Migrating PC Sorting

This is a hypothetical example showing how an existing feature could be wrapped:

### Before (Existing)

```java
// Code scattered in CobbleAid.java, mixins, and feature/pc/sort/
// No centralized initialization
```

### After (Using Feature System)

```java
package cc.turtl.cobbleaid.feature.pc.sorting;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;

public class PcSortingFeature implements Feature {
    
    @Override
    public void initialize() {
        // Initialize sorting UI, register event handlers
        // This code previously in CobbleAid.initialize()
    }
    
    @Override
    public boolean isEnabled() {
        return CobbleAid.getInstance().getConfig().pc.quickSortEnabled;
    }
    
    @Override
    public String getName() {
        return "PC Sorting";
    }
}
```

**Note**: This example is for illustration. The existing PC sorting code does not need to be migrated immediately.

## Getting Help

### Resources

1. **ARCHITECTURE.md** - Detailed technical documentation
2. **feature/demo/DemoFeature.java** - Example implementation
3. **README.md** - Updated developer guide

### Questions?

- Check the ARCHITECTURE.md for detailed explanations
- Review the DemoFeature example
- Look at how existing features are structured
- Ask questions in the project discussion/issues

## Summary

### What You Need to Do

For **new features**:
- ✅ Use the Feature system
- ✅ Follow the new patterns

For **existing features**:
- ❌ No immediate migration required
- ✅ Can optionally adopt new patterns
- ✅ Continue with current approach

### What You Don't Need to Do

- ❌ Don't rewrite existing working code
- ❌ Don't modify configuration files
- ❌ Don't change mixin organization (unless you want to)
- ❌ Don't update imports for unchanged code

The refactoring is designed to be **non-breaking** and **opt-in** for existing code while providing better patterns for new development.
