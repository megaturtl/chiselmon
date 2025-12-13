# Dependency Management Refactor Plan

Goal: standardise how the config, logger, and world data store are registered, instantiated, and accessed so that every subsystem consumes them through a single, predictable lifecycle instead of ad-hoc static calls.

## Current state (pain points)
- **Config**: Loaded in `CobbleAid.onInitializeClient`, stored as a field, and exposed via `CobbleAid.getInstance().getConfig()`. Callers often cache the returned object (e.g., mixins) so reloads do not propagate. Validation is only invoked on save. The config also holds `worldDataMap`, mixing runtime data with user settings.
- **Logger**: A static `CobbleAidLogger` is created before the config is loaded. Its level is toggled after config load, but consumers grab the static logger directly, so there is no link between config changes and logger behaviour other than the manual toggle.
- **World data store**: `WorldDataManager` is constructed with the config-owned `worldDataMap` and exposed via `CobbleAid.getWorldData()`. This couples world persistence to config serialization, hides lifecycle (world changes, logout), and makes it hard to swap storage or add observers.
- **Access pattern**: Most code reaches into `CobbleAid` statically for config/logger/world data, rather than receiving dependencies explicitly. There is no clear lifecycle boundary or service registry.

## Target architecture
Create a small, explicit service container that owns the core dependencies and exposes them via narrow interfaces. Keep construction in one place and avoid leaking raw config objects.

- **App context/service registry**: A `CobbleAidContext` (or `Services`) built during `onInitializeClient` that holds:
  - `ConfigService` (loads/validates, exposes read-only view, change listeners, save/reload helpers)
  - `LoggingService` (creates loggers; reacts to config debug flag changes)
  - `WorldDataService` (per-world store lookup/persistence; decoupled from config storage)
  - Optional helpers (e.g., `LifecycleEvents`, `Scheduler`) if needed later.
- **Access**: Provide a single static accessor `CobbleAid.services()` returning the context; other classes request specific services (`services().config()`, `services().worldData()`, `services().logger(Class<?>)`). Avoid caching config snapshots; prefer lightweight accessors or listeners.
- **Lifecycle**: Bootstrap order in `onInitializeClient`: construct context → load config → wire logger level → create world data service → register commands/listeners with dependencies passed in (not read statically).

## Refactor plan (phased, minimal-risk)
1. **Introduce scaffolding**
   - Add `CobbleAidServices` (interface) and `DefaultCobbleAidServices` (implementation) holding `ConfigService`, `LoggingService`, and `WorldDataService`.
   - Add `ConfigService` wrapper around AutoConfig (load, save, reload, validation hooks, change listener for `debugMode`).
   - Add `LoggingService` that provides class-based loggers and subscribes to config debug flag to set levels.
   - Add `WorldDataService` that internally owns the world map and computes identifiers (currently via `WorldDataManager`). Keep serialization format compatible initially.
2. **Bootstrap through services**
   - In `CobbleAid.onInitializeClient`, build the service container once and store it; remove direct fields (`configHolder`, `config`, `worldManager`, static logger) in favour of services.
   - Register commands/listeners by passing required services explicitly (constructor params or method args).
3. **Migrate consumers incrementally**
   - Replace `CobbleAid.getInstance().getConfig()` usage with `CobbleAid.services().config().get()`; avoid caching in fields where reload is expected.
   - Replace `CobbleAid.getLogger()` usages with `CobbleAid.services().logging().getLogger(Class<?>)`.
   - Replace `CobbleAid.getWorldData()` with `CobbleAid.services().worldData().current()` (or similar).
   - Update mixins with small helper accessors to call services at use time (keeps lazy and reload-safe).
4. **Decouple world data from config storage**
   - Move `worldDataMap` out of `ModConfig` into `WorldDataService` backing storage (e.g., dedicated JSON in the config folder). Provide migration: on first load, read legacy map from config and persist into the new store, then clear the config field.
   - Consider per-world file naming scheme based on the world identifier.
5. **Validation and safety**
   - Add lightweight unit coverage for service wiring (if feasible) or runtime assertions in bootstrap to guard against null services.
   - Document lifecycle expectations (when services are available, reload path) in README or a developer doc.

## Expected outcomes
- Single source of truth for core dependencies with predictable lifecycle.
- Reload-safe config consumption (no stale cached objects).
- Logger level automatically tracks config changes.
- World data persistence no longer pollutes user-facing config and can evolve independently.
- Clear entrypoint for future dependencies without expanding `CobbleAid` static surface.
