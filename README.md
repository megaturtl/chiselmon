# Chiselmon

Client-side quality-of-life improvements for Cobblemon. Lightweight, configurable, and focused on PC management, fishing, and Jade additions.

## Install
1. Install Cobblemon, Cloth Config, and Jade (optional).
2. Drop the jar file into your mods folder and launch the game.

## Key features
All features are configurable or can be disabled via the config.

- PC
  - Sort boxes by Pokemon size or IV total (uses hypertrained IVs).
  - Quick-sort a box with middle-click (sorting mode configurable).
  - Small icons on each pc slot for quick recognition:
    - Gold cap = high max IVs
    - Green mushroom = extreme size
    - Ability patch = hidden ability
    - Saddle = rideable
    - Sparkle = shiny
  - Tooltips on hover, use shift for more detailed info.
  - 6 new wallpapers (some with dark variants). Hold Ctrl to apply a selected wallpaper to every box.
  - Bookmark up to 5 boxes.
  - Extra egg info before hatching.
  - Alert system for legendaries, shinies, size variations, or custom whitelisted pokemon that spawn around you. Press 'M' (configurable in controls) to mute all active alerts.
  - Spawnlogger system to track spawns around you and export to CSV.
    - /chisel log start
    - /chisel log stop
    - /chisel log pause
    - /chisel log resume

- Fishing
  - Show current bait type and quantity while holding a pokerod.

- Jade UI enhancements
  - Extra hover info: typing, egg groups, EV yield, catch rate, catch chance (when holding a Pokeball), and warnings for self-damaging moves.
  - Poke Snack details: bites remaining, ingredients, and spawn effects.

## Configuration
Open the mod config from the 'Mods' tab in the escape menu to enable/disable features and adjust thresholds (e.g., IV count, size limits).

## Modular Architecture Refactor Plan (big-refactor)
This plan describes **what** to change and **why** to meet the modular architecture goals while keeping the current feature set intact.

### Core Architecture & Module Boundaries
- **Create a core module** (`chiselmon-core`) that owns:
  - Shared services (config, logging, world data).
  - A small **event bus** abstraction and module registry.
  - Loader-agnostic **client lifecycle hooks** (init, tick, command registration, keybind registration).
- **Feature modules** become standalone packages:
  - `chiselmon-spawn-alert`, `chiselmon-spawn-logger`, `chiselmon-check-spawn-tracker`,
    `chiselmon-egg-preview`, `chiselmon-pc`, `chiselmon-hud`, `chiselmon-jade`.
  - Each module depends only on `chiselmon-core`.
- **Why:** enforce independence and allow selective packaging or distribution of features.

### Loader-Agnostic Bridges
- **Introduce bridge modules** per loader (e.g., `chiselmon-fabric`, `chiselmon-neoforge`).
- Bridges implement **core abstractions**:
  - lifecycle event adapters (client init, tick, world load).
  - command registration adapters.
  - config GUI integration adapters.
  - keybind & input adapters.
- **Why:** keep loader-specific APIs out of core and feature modules, enabling multi-loader builds.

### Per-Module Config & Commands
- Each feature module owns a config class and registers it with core:
  - `ModuleConfig` interface in core + module-specific data classes.
  - Core aggregates configs into a **single config root** and exposes module sections in the GUI.
- Each module registers its own commands via a **module command registrar** in core.
- **Why:** remove centralized command/config registration and keep each module self-contained.

### Inter-Module Communication
- Core provides an **event-driven API** and **module discovery**:
  - `ModuleRegistry` tracks loaded modules and exposes typed APIs.
  - Modules publish/subscribe to core events (e.g., spawn events, egg events).
- **Optional enhancement pattern**:
  - Example: SpawnLogger listens for SpawnAlert events when available, but also emits its own spawn events.
- **Why:** modules can enhance each other without hard dependencies.

### Migration Strategy (Feature by Feature)
- **SpawnAlert**: move listeners + alert logic into `chiselmon-spawn-alert`; publish spawn events to core bus.
- **SpawnLogger**: move CSV export + timers into `chiselmon-spawn-logger`; subscribe to core spawn events.
- **CheckSpawnTracker**: move HUD overlay into `chiselmon-check-spawn-tracker`; use core config + HUD adapter.
- **EggPreview**: move mixin logic & UI hints into `chiselmon-egg-preview`; expose egg data to core.
- **PC**: move PC overlays, sorting, and wallpapers into `chiselmon-pc`; publish PC state events.
- **HUD (PokeRod)**: move bait overlay into `chiselmon-hud` using core HUD adapter.
- **Jade**: keep `chiselmon-jade` as optional integration; only uses core data APIs.
- **Why:** controlled migration reduces risk, and module boundaries are clear.

### Project Structure & Build Changes
- Switch to **multi-module Gradle**:
  - `settings.gradle` includes core + feature modules + loader bridges.
  - Core + features use pure Java with no loader dependencies.
  - Bridges apply the loader-specific Gradle plugins and dependencies.
- **Suggested package layout**:
  - `cc.turtl.chiselmon.core.*`
  - `cc.turtl.chiselmon.module.<feature>.*`
  - `cc.turtl.chiselmon.bridge.<loader>.*`
- **Why:** clean separation of concerns and supports separate publication artifacts.

### Execution & Registration Flow
1. Loader bridge starts up and instantiates core.
2. Core loads and registers available modules (via ServiceLoader or explicit registry).
3. Each module registers config + commands + event listeners through core APIs.
4. Modules query `ModuleRegistry` for optional enhancements.
