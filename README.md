# CobbleAid

Client-side helpers for Cobblemon.

## Features

### PC highlight filter

- Configure `pcHighlighting` in the in-game config screen (Mod Menu + AutoConfig).
- Enable any of the toggles to highlight matching Pokémon in the PC.
- Pick any RGB color code for the tint, then adjust the shared alpha slider to control transparency.
- If you also run Cobblemon Size Variation, toggle the "Extreme Sizes" group to tint Pokemon below your tiny threshold (default 0.25) or above your huge threshold (default 1.75).

## Architecture

CobbleAid uses a modular architecture to make it easy to add new features and maintain existing ones. The codebase is organized into clear packages with well-defined responsibilities:

- **core/** - Core infrastructure (Feature system, lifecycle management)
- **api/** - Public reusable utilities and helpers
- **feature/** - Modular features (HUD, PC enhancements, etc.)
- **config/** - Configuration management
- **integration/** - External mod integrations (Jade, etc.)
- **mixin/** - Mixins organized by feature
- **util/** - General utility classes

For detailed architecture documentation, see [ARCHITECTURE.md](ARCHITECTURE.md).

## For Developers

### Adding a New Feature

1. Create a package under `feature/<yourfeature>/`
2. Implement the `Feature` interface
3. Add configuration options in `ModConfig` (optional)
4. Register your feature in `CobbleAid.registerFeatures()`
5. Add any required mixins

See the `feature/demo/` package for a complete example, or read the [ARCHITECTURE.md](ARCHITECTURE.md) guide for detailed instructions.

### Building

Requires Java 21:

```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
./gradlew build
```

Output: `build/libs/cobbleaid-<version>+<mc-version>.jar`

### Project Structure

```
src/main/java/cc/turtl/cobbleaid/
├── core/          - Core infrastructure
├── api/           - Public APIs and utilities
├── feature/       - Modular features
├── command/       - Client commands
├── config/        - Configuration
├── integration/   - External integrations
├── mixin/         - Mixins
└── util/          - Utilities
```

## Commands

- `/cobbleaid info` (or `/ca info`) - Show mod information
- `/cobbleaid config` - Configuration commands
- `/cobbleaid debug` - Debug and diagnostic commands
- `/cobbleaid egg` - Egg-related commands

## Dependencies

- Minecraft 1.21.1
- Fabric Loader ≥0.17.3
- Fabric API
- Cobblemon ≥1.7.0
- Cloth Config (AutoConfig)
- Mod Menu (for config GUI)

Optional:
- Jade ≥15.10.2 (enhanced tooltips)

## License

CC0-1.0
