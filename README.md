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

---

## macOS Instructions

### Step 1 – Install Java 17 (including Apple Silicon M1/M2/M3)

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

### Step 2 – Build

```bash
./gradlew build
```

> If you see a "permission denied" error, first run: `chmod +x gradlew`

The compiled JAR will be in `build/libs/`.

### Step 3 – Install the Mod

1. Download and run the [Minecraft Forge 1.20.1 installer](https://files.minecraftforge.net/) (47.2.0).
2. Build the mod (see above) or download the JAR from [Releases](https://github.com/johnmog/creeper-mod/releases).
3. Copy the JAR to your mods folder: `~/Library/Application Support/minecraft/mods/`
4. Launch Minecraft with the **Forge 1.20.1** profile.

### Firewall (LAN / Open to LAN)

When Minecraft with Forge first binds a local server port (single-player or Open to LAN), macOS will show a dialog:

> **"Do you want the app 'java' to accept incoming network connections?"**

Click **Allow**. If you accidentally clicked Deny, re-enable it manually:

1. **System Settings** → **Network** → **Firewall** → **Options…**
2. Find `java` (or the Minecraft launcher) in the list.
3. Set it to **Allow incoming connections**.
4. Click **OK** and restart Minecraft.

> **Note:** This mod itself makes no network calls — all its logic (mob spawn replacement, Creeper AI, explosions) runs entirely server-side inside the game engine. The firewall prompt comes from Minecraft/Forge's built-in LAN server, not from the mod.

---

## Windows (PC) Instructions

### Step 1 – Install Java 17

Download and install the Windows **x64** build from [Adoptium Temurin 17](https://adoptium.net/temurin/releases/?version=17). Run the `.msi` installer and follow the prompts — it will set `JAVA_HOME` and update your `PATH` automatically.

Verify the installation in a new Command Prompt:

```bat
java -version
```

It should print `openjdk version "17.x.x"`.

> **Point the Minecraft Launcher at your Java 17 install**:  
> Launcher → Installations → Edit Forge profile → More Options → Java Executable  
> → `C:\Program Files\Eclipse Adoptium\jdk-17.*\bin\java.exe`  
> (or wherever the JDK was installed)

### Step 2 – Build

```bat
gradlew.bat build
```

The compiled JAR will be in `build\libs\`.

### Step 3 – Install the Mod

1. Download and run the [Minecraft Forge 1.20.1 installer](https://files.minecraftforge.net/) (47.2.0).
2. Build the mod (see above) or download the JAR from [Releases](https://github.com/johnmog/creeper-mod/releases).
3. Copy the JAR to your mods folder: `%APPDATA%\.minecraft\mods\`
4. Launch Minecraft with the **Forge 1.20.1** profile.

### Firewall (LAN / Open to LAN)

When Minecraft with Forge first opens a LAN port, Windows Firewall may show a security alert for `java.exe`. Click **Allow access** to permit LAN connections. If you accidentally blocked it, re-enable it manually:

1. Open **Windows Defender Firewall** → **Allow an app or feature through Windows Defender Firewall**.
2. Find `OpenJDK Platform binary` (or `java.exe`) in the list.
3. Check both **Private** and **Public** as needed, then click **OK**.
4. Restart Minecraft.

> **Note:** This mod itself makes no network calls — all its logic (mob spawn replacement, Creeper AI, explosions) runs entirely server-side inside the game engine. The firewall prompt comes from Minecraft/Forge's built-in LAN server, not from the mod.

---

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
