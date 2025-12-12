# CobbleAid Architecture

## Overview

CobbleAid is a client-side Fabric mod for Minecraft 1.21.1 that enhances the Cobblemon experience. The mod is organized into a modular architecture that makes it easy to add new features and maintain existing ones.

## Package Structure

```
cc.turtl.cobbleaid/
├── CobbleAid.java              # Main mod initializer
├── WorldDataManager.java       # Per-world persistent data
├── WorldDataStore.java         # World-specific data container
│
├── core/                       # Core infrastructure
│   ├── lifecycle/
│   │   ├── Feature.java       # Feature interface
│   │   └── FeatureManager.java # Feature lifecycle manager
│   └── registry/
│       └── RegistryHelper.java # Registration utilities
│
├── api/                        # Public reusable utilities
│   ├── capture/               # Capture chance calculation
│   ├── comparator/            # Pokemon comparators
│   ├── format/                # Formatting utilities
│   ├── predicate/             # Pokemon predicates
│   ├── render/                # Rendering utilities
│   └── util/                  # General utilities
│
├── feature/                    # Modular features
│   ├── demo/                  # Example feature
│   │   └── DemoFeature.java
│   ├── hud/                   # HUD elements
│   │   └── PokeRodBaitOverlay.java
│   └── pc/                    # PC-related features
│       ├── PcIconRenderer.java
│       ├── neodaycare/        # Neo Daycare integration
│       ├── sort/              # PC sorting
│       ├── tab/               # PC bookmarks/tabs
│       └── tooltip/           # PC tooltips
│
├── command/                    # Client commands
│   ├── CobbleAidCommand.java  # Main command
│   └── suggestion/            # Command suggestions
│
├── config/                     # Configuration
│   ├── ModConfig.java         # Main configuration
│   ├── CobbleAidLogger.java   # Logging wrapper
│   └── ModMenuIntegration.java
│
├── integration/                # External mod integrations
│   ├── jade/                  # Jade tooltip integration
│   └── neodaycare/            # Neo Daycare integration
│
├── mixin/                      # Mixins (organized by target)
│   ├── GuiMixin.java
│   └── pc/                    # PC-related mixins
│       ├── StorageSlotMixin.java
│       ├── neodaycare/
│       ├── sort/
│       ├── tab/
│       ├── tooltip/
│       └── wallpaper/
│
└── util/                       # Utility classes
    ├── MiscUtil.java
    ├── ObjectDumper.java
    └── StringUtil.java
```

## Core Concepts

### Feature System

The modular feature system is based on the `Feature` interface:

```java
public interface Feature {
    void initialize();      // Called once during mod init
    boolean isEnabled();    // Check if feature is enabled
    String getName();       // Feature name for logging
}
```

Features are managed by the `FeatureManager`, which handles:
- Feature registration
- Lifecycle management
- Error handling and logging

### Initialization Flow

1. **Pre-initialization** (`preInitialize()`)
   - Load configuration
   - Initialize world data manager
   - Initialize core infrastructure (FeatureManager, RegistryHelper)

2. **Initialization** (`initialize()`)
   - Register all features
   - Register commands
   - Initialize all registered features

3. **Post-initialization**
   - Features are ready to use
   - Configuration can be modified at runtime

### Configuration

Configuration is managed through `ModConfig.java` using AutoConfig (Cloth Config). The configuration:
- Persists to disk automatically
- Supports nested configuration objects
- Integrates with Mod Menu for GUI
- Can store per-world data in `worldDataMap`

### World Data

Per-world data is managed through:
- `WorldDataManager`: Manages multiple world stores
- `WorldDataStore`: Stores data for a specific world
- Data persists in the main config file

World identifiers are based on:
- Multiplayer: `MP:<server-ip>`
- Singleplayer: `SP:<world-name>`

## Adding a New Feature

### Step 1: Create Feature Package

Create a new package under `feature/`:

```
feature/
└── myfeature/
    ├── MyFeature.java       # Feature implementation
    ├── MyFeatureConfig.java # Optional: feature-specific config
    └── MyFeatureHelper.java # Optional: helper classes
```

### Step 2: Implement Feature Interface

```java
package cc.turtl.cobbleaid.feature.myfeature;

import cc.turtl.cobbleaid.CobbleAid;
import cc.turtl.cobbleaid.core.lifecycle.Feature;

public class MyFeature implements Feature {
    
    @Override
    public void initialize() {
        // Register event listeners, commands, etc.
        // This is called once during mod startup
    }
    
    @Override
    public boolean isEnabled() {
        // Check configuration
        return CobbleAid.getInstance()
            .getConfig()
            .myFeature
            .enabled;
    }
    
    @Override
    public String getName() {
        return "My Feature";
    }
}
```

### Step 3: Add Configuration (if needed)

In `ModConfig.java`, add a nested configuration class:

```java
@ConfigEntry.Gui.CollapsibleObject
public MyFeatureConfig myFeature = new MyFeatureConfig();

public static class MyFeatureConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean enabled = true;
    
    // Add more config options...
}
```

### Step 4: Register Feature

In `CobbleAid.registerFeatures()`:

```java
private void registerFeatures() {
    // ... existing features ...
    featureManager.register(new MyFeature());
}
```

### Step 5: Add Mixins (if needed)

Create mixins in the appropriate package:

```
mixin/
└── myfeature/
    └── MyTargetClassMixin.java
```

Register the mixin in `cobbleaid.mixins.json`:

```json
{
  "client": [
    "myfeature.MyTargetClassMixin"
  ]
}
```

## Best Practices

### Code Organization

1. **Keep features isolated**: Each feature should be self-contained in its package
2. **Use the API package**: Share reusable utilities in `api/`
3. **Prefix mixin methods**: Use `cobbleaid$methodName` for injected methods
4. **Check feature enablement**: Always check `isEnabled()` before executing feature logic

### Configuration

1. **Use nested classes**: Group related config options
2. **Add tooltips**: Use `@ConfigEntry.Gui.Tooltip` for user guidance
3. **Provide defaults**: Set sensible default values
4. **Validate on save**: Use `validate_fields()` for custom validation

### Logging

1. **Use the logger**: Access via `CobbleAid.getLogger()`
2. **Log at appropriate levels**:
   - `debug()`: Detailed diagnostic info
   - `info()`: Important events
   - `warn()`: Potential issues
   - `error()`: Errors and exceptions

### Mixins

1. **Keep mixins minimal**: Only inject what's necessary
2. **Use descriptive names**: Name mixins after their target class
3. **Set remap correctly**: Use `remap = false` for Cobblemon targets
4. **Check mod disabled**: Always check `config.modDisabled` first

## Testing

### Manual Testing Checklist

1. **Build**: `./gradlew build`
2. **Launch**: Test in dev environment
3. **Verify features**: Check each feature works as expected
4. **Test configuration**: Verify config GUI and persistence
5. **Check logs**: Look for errors or warnings

### Debug Commands

- `/cobbleaid info` - Show mod info
- `/cobbleaid config` - Config commands
- `/cobbleaid debug dump <slot>` - Dump Pokemon data

## Migration Guide

### For Contributors

If you have existing work based on the old structure:

1. **Feature code**: Move to appropriate `feature/<name>/` package
2. **Utilities**: Move to `api/` or `util/` as appropriate
3. **Implement Feature interface**: Wrap feature init in Feature class
4. **Update imports**: Fix any package reference changes
5. **Test thoroughly**: Verify feature still works

### For Users

No action required. Configuration files are backward compatible.

## Performance Considerations

1. **Lazy initialization**: Only initialize features when needed
2. **Cache calculations**: Store expensive computations
3. **Check enablement**: Skip disabled feature logic early
4. **Minimize allocations**: Reuse objects where possible

## Security

1. **Client-side only**: No server-side code
2. **No secrets**: Don't store sensitive data in config
3. **Validate input**: Check all user input in commands
4. **Safe mixins**: Be careful with injection points

## Future Enhancements

Potential improvements to the architecture:

1. **Event bus**: Add internal event system for feature communication
2. **Dependency injection**: Consider DI framework for cleaner code
3. **Feature dependencies**: Allow features to depend on each other
4. **Hot reload**: Support feature reload without restart
5. **Unit tests**: Add testing infrastructure

## Resources

- [Fabric Wiki](https://fabricmc.net/wiki/)
- [Cobblemon Wiki](https://wiki.cobblemon.com/)
- [Mixin Documentation](https://github.com/SpongePowered/Mixin/wiki)
- [AutoConfig](https://github.com/shedaniel/AutoConfig)
