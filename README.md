# Billy's Mods

Billy's Mods is a Fabric mod for Minecraft Java Edition 1.21.10. It is a single mod project for experimenting with multiple features over time.

## Current features

- **Elixir of Life**: drink it to gain persistent immortality. You can be reduced to half a heart, but cannot be killed while the effect is active.
- **Elixir of Mortality**: drink it to remove immortality.
- Both elixirs are in the **Billy's Mods** creative tab. Drinking either plays an activation sound and sends a private chat message to the player who drank it.

## Download the mod

The mod is built by GitHub Actions; no local build is needed. Open the [latest successful Build mod run](https://github.com/hughes2604/billys-mods/actions/workflows/build.yml), open the run, and download its `billys-mods-...` artifact. Extract the ZIP and place the `.jar` in your Minecraft `mods` folder. The artifact is retained for 30 days.

Use Minecraft 1.21.10 with Fabric Loader 0.19.5 and Fabric API 0.138.4+1.21.10.

## Multiplayer

Install Billy's Mods and Fabric API on the server and on each client. The immortality effect is applied and checked by the server, so it behaves consistently in multiplayer.

## Build requirements

The GitHub Actions workflow builds remotely using Java 25 and Gradle 9.7.1. The mod targets Java 21 bytecode for Minecraft 1.21.10.
