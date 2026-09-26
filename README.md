# Chiselmon
![Environment](https://img.shields.io/badge/Environment-Client-purple)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/chiselmon?style=flat&logo=modrinth&label=Downloads&color=%2358a95a)](https://modrinth.com/mod/chiselmon)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1475518?style=flat&logo=curseforge&label=Downloads&color=%23d66d36)](https://www.curseforge.com/minecraft/mc-mods/chiselmon)
[![Discord](https://img.shields.io/discord/1476397018204082188?style=flat&logo=discord&label=Discord&color=blue)](https://discord.gg/wFRFqQK9WW)
<br>
![Lurantis with magnifying glass, the Chiselmon logo](common/src/main/resources/assets/chiselmon/icon.png)

Sculpt your perfect Cobblemon experience with deeply customisable QOL tweaks!

## Installation
Chiselmon is a fully client side Cobblemon sidemod for Fabric and NeoForge.

To use Chiselmon, make sure you have Cobblemon 1.7.0+ and [Yet Another Config Lib](https://modrinth.com/mod/yacl) installed. To get the most out of the mod, consider also installing [Jade](https://modrinth.com/mod/jade).

## Customising Your Tweaks
If you haven't used Chiselmon before, I recommend exploring the config options beforehand so you can see what features are available.

![Alert config screen](media/alert_config_screen.png)
You can open the Chiselmon config in 3 ways:
1. Click the Chiselmon icon in your mod menu.
2. Use the `/ch config` command in-game.
3. Press the hotkey. The default is `;`, but this can be changed in the config.

## Features
### PC Overhaul:
![PC screen](common/src/main/resources/assets/chiselmon/screenshots/pc_screen.png)

- **Bookmark up to 5 boxes** and jump to them at any time.
- **10 new wallpapers**. Hold `Ctrl` while choosing to apply a selected wallpaper to every box at once.
- **Quick-sort any box**. Default hotkey is `Middle-Click`.
- **6 unique icons** to easily identify special pokemon (shiny, high IVs, hidden ability, extreme size, marks, rideable).
- **Detailed tooltips** showing IVs, original trainer, form, friendship, ride styles, marks, and egg hatch progress.
- **2 new box sorting modes**. Sort by Pokemon size and total IVs.


### Egg Spy:
![Eggs in PC](media/egg_pc.gif)
![Eggs in PC](media/egg_party.png)
- Use Chiselmon's cutting edge ultrasound technology to preview what's inside pokemon eggs (NeoDaycare only).
- Optionally replaces the HP and XP bar of eggs in your party overlay with their hatch progress so you can easily keep track while hatching.
### Spawn Alerts:
![Discord alert](common/src/main/resources/assets/chiselmon/screenshots/discord_alert.png)
- Alert system for legendaries, shinies, alphas, size variations, or custom whitelisted pokemon that spawn around you.
- Create unlimited custom filters to receive alerts for with Chiselmon's logic building system.
- 4 Fully configurable alert types per filter - chat messages, Discord webhook messages, sounds, colored highlights.
- Mute all active alert sounds with the 'M' keybind (configurable in Minecraft controls and Chiselmon config).
- Create server/world specific exclusion zones to suppress alerts (e.g. at spawn, hubs, or pasture areas).
### Spawn Recorder:
- Records total pokemon spawns and the number of each species in the current session.
- Use the `/ch recorder start, stop, pause, and resume` commands to manage your session.
- Optional action bar display: shows the total spawns and time passed since starting, and the number of currently loaded pokemon + how many are old enough to start despawning.
- Optional despawn glow: highlights loaded pokemon in green, turning red when they're ready to despawn.
### Misc:
#### Move Tooltips
![Move tooltip](common/src/main/resources/assets/chiselmon/screenshots/move_tooltip.png)

#### /checkspawn Improvemennts
![/checkspawn command output](common/src/main/resources/assets/chiselmon/screenshots/checkspawn.png)
`/checkspawn` results now have hoverable tooltips showing egg group and EV yield info to make cake hunting easier.
### Jade Addons:
#### Pokemon Extra Info
![Jade Pokemon](media/jade_pokemon.png)
  - Types
  - Weaknesses
  - Form
  - Pokedex status
  - Egg groups
  - EV yield
  - Catch rate
  - Catch chance % (when holding a Pokeball - considers level, status, etc!)
  - Warnings for self-damaging moves - never lose a shiny to explosion again! </3
#### Poke Snack Extra Info:
![Jade Pokesnack](media/jade_pokesnack.png)
  - Bites remaining
  - Ingredients
  - Seasoning effects

---

# Development Info

## Versioning

The release version is derived from two properties in `gradle.properties`:

```properties
chiselmon_version = 1.4.1
cobblemon_version = 1.8.0
```

These properties would produce `1.4.1+cobblemon-1.8.0`. Local and tagged builds use this clean string, but extra build metadata is added for development jars.

| Context         | Example version                        |
|-----------------|----------------------------------------|
| Local build     | `1.4.1+cobblemon-1.8.0`                |
| Merge to `main` | `1.4.1+cobblemon-1.8.0.commit.a3f92c1` |
| Pull request    | `1.4.1+cobblemon-1.8.0.pr42.a3f92c1`   |
| Tagged release  | `1.4.1+cobblemon-1.8.0`                |

## Build workflows

`build-pull-request` verifies every pull request and uploads testable mod artifacts. `build-commit` verifies maintained branches and keeps the gradle cache warm. `build-tag` builds the official release under the `prod` environment.

## Creating a release

1. Update release properties in `gradle.properties` and merge the release commit to the main branch.
2. Create and push the exact configured tag:
   ```bash
   version=$(./gradlew --quiet printVersion)
   git tag "v$version"
   git push origin "v$version"
   ```
3. `build-tag` verifies the tag, builds both jars, creates or updates the GitHub Release, and attaches the Fabric and NeoForge jars. GitHub generates a default changelog from the previous release, which can be edited afterward if need be.

To build Fabric and NeoForge jars locally, run `./gradlew build`. The outputs will be saved to `{loader directory}/build/libs`.
