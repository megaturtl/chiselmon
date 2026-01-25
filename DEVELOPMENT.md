# Development Setup Instructions

## IDE Setup for Multi-Loader Development

After cloning this repository, follow these steps to set up your IDE:

### 1. Generate Minecraft Sources

Run the following command to decompile Minecraft sources for your IDE:

```bash
./gradlew genSources
```

This will generate sources for all three modules (common, fabric, neoforge). This process takes a few minutes.

### 2. Import/Refresh Gradle Project

#### IntelliJ IDEA
- File → Open → Select the project root directory
- When prompted, import as a Gradle project
- Wait for Gradle sync to complete
- If imports are still not resolved, run: `./gradlew idea` and then File → Invalidate Caches / Restart

#### Eclipse
- Run `./gradlew eclipse` first
- File → Import → Existing Projects into Workspace
- Select the project root directory

#### VS Code
- Open the project folder
- Install the "Extension Pack for Java" if not already installed
- The Gradle extension should automatically detect the project

### 3. Verify Setup

After setup, all imports in the `common`, `fabric`, and `neoforge` modules should resolve correctly. The IDE should recognize:
- Minecraft classes
- Cobblemon classes
- Fabric API classes (in fabric module)
- NeoForge API classes (in neoforge module)

## Building

To build all modules:
```bash
./gradlew build
```

To build a specific module:
```bash
./gradlew :fabric:build
./gradlew :neoforge:build
```

## Troubleshooting

### "Cannot resolve symbol" errors in IDE

1. Run `./gradlew clean genSources`
2. Refresh/reimport Gradle project
3. Invalidate caches and restart IDE

### Build succeeds but IDE shows errors

This is a common issue with Loom-based projects. The build is authoritative - if it succeeds, the code is correct. Try:
1. `./gradlew --stop` (stop Gradle daemon)
2. `./gradlew clean`
3. `./gradlew genSources`
4. Reimport project in IDE

### Platform-specific dependencies not resolving in common module

This is expected! The common module uses `modCompileOnly` for platform-specific dependencies. They will only fully resolve when viewed from the platform module (fabric or neoforge).
