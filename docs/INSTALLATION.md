# Install Billy's Mods

This guide explains how to download the ready-to-use mod file. You do not need to use GitHub to code or build anything.

## Before you start

Install these for the Minecraft game version **Java Edition 1.21.10**:

- **Fabric Loader 0.19.5**
- **Fabric API 0.138.4+1.21.10**

The game, Fabric Loader, Fabric API, and this mod must all be for compatible versions.

## Download the mod from GitHub

1. Open the [Build mod workflow page](https://github.com/hughes2604/billys-mods/actions/workflows/build.yml).
2. Select the newest run with a green check mark and the title **Build mod**.
3. On that run's page, scroll to **Artifacts**.
4. Download the artifact named `billys-mods-...`. GitHub downloads it as a ZIP.
5. Open the ZIP. Inside is the mod's `.jar` file. Extract that JAR.

GitHub keeps each build download for 30 days. If the artifact has expired, open a newer successful run.

## Put the JAR in the mods folder

1. Open your Minecraft folder. In the official Minecraft Launcher, you can use **Installations**, select your Fabric installation, then use **Open Folder**. The mods folder is normally inside the Minecraft game folder.
2. Open the folder named `mods`. If it is missing, create a folder with that exact name.
3. Copy the mod's `.jar` file into `mods`. Do not copy the downloaded ZIP.
4. Launch Minecraft using the Fabric installation for version 1.21.10.

If Minecraft fails to start, check that you have the correct Minecraft version, Fabric Loader, Fabric API, and mod JAR. Remove any older Billy's Mods JAR from `mods` before adding a newer one.

## Multiplayer

Install the same compatible mod and Fabric API on the server and on every player's client. A player without the mod cannot use its custom items or flight controls.
