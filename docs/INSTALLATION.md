# Install Billy's Mods

This guide explains how to download the ready-to-use mod file. GitHub builds it for you.

## Before you start

Use Minecraft Java Edition **1.21.1** with:

- **Fabric Loader 0.19.5**
- **Fabric API 0.116.15+1.21.1**
- **Ad Astra 1.16.26 for Fabric** (optional; required for its planets and oxygen compatibility)

The Minecraft version must match across the game, Fabric, Ad Astra, and Billy's Mods.

## Download and install

1. Open the [Build mod workflow page](https://github.com/hughes2604/billys-mods/actions/workflows/build.yml).
2. Choose the newest successful run and download its `billys-mods-...` artifact.
3. Open the downloaded ZIP and extract the JAR inside.
4. Put that JAR in the Minecraft `mods` folder.
5. Launch Minecraft using the Fabric 1.21.1 profile.

GitHub keeps each build artifact for 30 days. If one has expired, use a newer successful run.

## Multiplayer

Install matching versions of Fabric API, Ad Astra (if used), and Billy's Mods on the server and each client.
