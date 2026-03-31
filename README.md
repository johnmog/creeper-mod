# Creeper Mod

A Minecraft Forge mod for **1.20.1** that makes Creepers the dominant hostile mob.

## Features

1. **Creeper Dominance** – Every hostile mob that would naturally spawn is replaced by a Creeper.
2. **Thunderstorm Charging** – Creepers that spawn during a thunderstorm are automatically charged (powered).
3. **Smart Targeting AI** – Creepers no longer blindly chase players. Within a 200-block radius they hunt targets in this priority order:
   1. Beds (nearest first)
   2. Chests / Trapped Chests (nearest first)
   3. The nearest player

   They navigate toward the highest-priority target. When they can no longer get closer (obstacle in the way), they detonate — even if the target is behind a wall, underground, or inside a building.

## Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | **17** (LTS) |
| Minecraft Java Edition | 1.20.1 |
| Minecraft Forge | 47.2.0 |

### Installing Java 17 on macOS (including Apple Silicon M1/M2/M3)

Use [Homebrew](https://brew.sh) to install a **native arm64** JDK — this gives the best performance on Apple Silicon without Rosetta:

```bash
brew install openjdk@17
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
java -version   # should print openjdk version "17.x.x"
```

Alternatively download the macOS **aarch64** (arm64) build directly from [Adoptium Temurin 17](https://adoptium.net/temurin/releases/?version=17).

> **Point the Minecraft Launcher at your Java 17 install**:  
> Launcher → Installations → Edit Forge profile → More Options → Java Executable  
> → `/opt/homebrew/opt/openjdk@17/bin/java`  
> (or wherever your JDK is installed)

## Building

```bash
# macOS / Linux
./gradlew build

# Windows
gradlew.bat build
```

The compiled JAR will be in `build/libs/`.  
On macOS, if you see a "permission denied" error, first run: `chmod +x gradlew`

## Installation (Local Play)

1. Download and run the [Minecraft Forge 1.20.1 installer](https://files.minecraftforge.net/) (47.2.0).
2. Build the mod (see above) or download the JAR from [Releases](https://github.com/johnmog/creeper-mod/releases).
3. Copy the JAR to your mods folder:
   - **macOS**: `~/Library/Application Support/minecraft/mods/`
   - **Windows**: `%APPDATA%\.minecraft\mods\`
4. Launch Minecraft with the **Forge 1.20.1** profile.

## CurseForge

This mod is structured to meet all CurseForge submission requirements:
- `mods.toml` includes `logoFile`, `displayURL`, `authors`, `license`, and `issueTrackerURL`.
- The mod logo (`creepermod.png`) is bundled inside the JAR.
- Uses semantic versioning (`1.0.0`).

## Development Setup

```bash
./gradlew genIntellijRuns   # IntelliJ IDEA
./gradlew genEclipseRuns    # Eclipse
```

## License

MIT
