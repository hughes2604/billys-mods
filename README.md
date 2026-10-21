# Billy's Mods

Billy's Mods is a Fabric mod for Minecraft Java Edition 1.21.10. It is a single mod project for experimenting with multiple features over time.

## Release milestones

- **Alpha 0.1**: introduced the Elixir of Immortality and Elixir of Mortality.
- **Alpha 0.2**: introduced the Viltrumite Genome and its strength, durability, and flight powers.

The current mod version is **0.2.0-alpha.2**.

## Current features

- **Elixir of Life**: drink it to gain persistent immortality. You can be reduced to half a heart, but cannot be killed while the effect is active.
- **Elixir of Mortality**: drink it to remove immortality.
- **Viltrumite Genome**: while carried anywhere in your main inventory, armor slots, or offhand, it grants super strength, high durability, and self-propelled flight.
  - Strength raises unarmed attack damage to 20 before armor, adds heavy knockback, and gives successful hits a strong impact sound and a short burst of particles.
  - Durability deflects arrows, prevents damage from unarmed player attacks, and adds substantial armor and toughness against other physical damage.
  - To fly, press the configurable **Toggle Viltrumite Flight** key (default **G**). Press it again to disable flight. Look to steer, hold jump to rise, and sneak to descend. Movement eases off when you release directional controls. The pose uses Minecraft's horizontal gliding animation with a subtle bank while turning.
- All items are in the **Billy's Mods** creative tab.

## Download the mod

The mod is built by GitHub Actions; no local build is needed. Open the [latest successful Build mod run](https://github.com/hughes2604/billys-mods/actions/workflows/build.yml), open the run, and download its `billys-mods-...` artifact. Extract the ZIP and place the `.jar` in your Minecraft `mods` folder. The artifact is retained for 30 days.

Use Minecraft 1.21.10 with Fabric Loader 0.19.5 and Fabric API 0.138.4+1.21.10.

## Multiplayer

Install Billy's Mods and Fabric API on the server and on each client. Ability checks and damage handling run on the server. The client entrypoint supplies the configurable flight keybind; movement and flight state are handled on the server.

## Build requirements

The GitHub Actions workflow builds remotely using Java 25 and Gradle 9.7.1. The mod targets Java 21 bytecode for Minecraft 1.21.10.
