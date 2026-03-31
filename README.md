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

## Building

> **Prerequisites:** Java 17, an internet connection to download Forge.

```bash
# Windows
gradlew.bat build

# macOS / Linux
./gradlew build
```

The compiled JAR will be in `build/libs/`.

## Installation

1. Install [Minecraft Forge 1.20.1](https://files.minecraftforge.net/) (tested with 47.2.0).
2. Copy the built JAR into your `.minecraft/mods/` folder.
3. Launch the game with the Forge profile.

## Development Setup

```bash
./gradlew genIntellijRuns   # IntelliJ IDEA
./gradlew genEclipseRuns    # Eclipse
```

## License

MIT