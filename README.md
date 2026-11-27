# Exposure 1.9.18 for Minecraft 26.1.2

> **Unofficial Fabric Port, Patch 3**

This repository contains an unofficial Minecraft 26.1.2 Fabric port of [Exposure](https://github.com/mortuusars/Exposure), the camera mod created by [Mortuusars](https://github.com/mortuusars). It is based on Exposure 1.9.18 at fixed upstream commit [`9383e0c9237099947a1e6399fcace5772f496c7d`](https://github.com/mortuusars/Exposure/commit/9383e0c9237099947a1e6399fcace5772f496c7d).

Filmorae maintains this port but is not the original author. Problems specific to this repository must **not** be reported to the official Exposure issue tracker; use this fork's [issue tracker](https://github.com/Filmorae/Exposure-26.1.2-Port/issues).

## Status

- Fabric only.
- Port version `1.9.18-port.3+mc26.1.2` is available on [GitHub Releases](https://github.com/Filmorae/Exposure-26.1.2-Port/releases).
- Patch 3 fixes dark or incomplete captures with shader and resolution-scaling combinations, stabilizes timed Camera Stand shots, and restores selfie poses with the Fresh Animations player extension.
- The mod id remains `exposure` for dependent-mod recognition.
- Compatibility with old Exposure worlds, albums, photographs, and repository data is not guaranteed. Back up a world before testing.

The port has controlled coverage for the camera and viewfinder, handheld and Camera Stand capture, photograph storage, rendering and export, film development and printing, albums, frames, recipes, commands, and game startup and shutdown. See [COMPATIBILITY.md](COMPATIBILITY.md) for the tested combinations and [KNOWN_ISSUES.md](KNOWN_ISSUES.md) for support limits.

## Requirements

- Minecraft 26.1.2
- Java 25
- Fabric Loader 0.19.3 or compatible newer 0.19.x version
- [Fabric API](https://modrinth.com/mod/fabric-api) 0.150.0+26.1.2 or newer compatible 26.1.2 build
- [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) 26.1.4

The selfie compatibility layer is optional. It was tested with EMF 3.3.12, ETF 7.2.7, and Fresh Animations: Player Extension 1.1.

## Installation

1. Install Minecraft 26.1.2 with Fabric Loader and Java 25.
2. Install the required Fabric API and Forge Config API Port versions above.
3. Place the port JAR in the instance's `mods` directory.
4. Start with a backed-up test world and confirm the loaded version identifies this unofficial port.

Do not download this port from the official Exposure CurseForge or Modrinth project pages; those pages belong to the upstream project.

## Shader and rendering boundary

Exposure reads the completed world frame before the GUI is drawn. Handheld capture uses the first eligible frame by default. The existing `force_direct_capture` settings now control an optional extra frame delay. If you enabled the setting as a workaround for dark photos, you can set it back to `false` to restore the default shutter timing. See [COMPATIBILITY.md](COMPATIBILITY.md) for shader, scaling, and player-model test results.

## Reporting a bug

Use the repository bug template and include the Minecraft/port versions, Fabric Loader/API versions, complete mod list, Shader/Iris/Sodium versions, exact reproduction steps, `latest.log`, `debug.log`, any crash report, and a screenshot or exported photograph. State whether the problem reproduces in a minimal environment.

## Building

Use Java 25 and the checked-in Gradle Wrapper:

```text
./gradlew :common:compileJava :fabric:compileJava :common:compileTestJava
./gradlew :common:test --rerun-tasks
./gradlew :common:validateAccessWidener :fabric:validateAccessWidener
./gradlew --rerun-tasks build
```

Read [AGENTS.md](AGENTS.md), [PORTING.md](PORTING.md), and [CONTRIBUTING.md](CONTRIBUTING.md) before changing the port. Historical migration evidence is indexed under [docs/migration](docs/migration/README.md).

## License and attribution

Exposure is licensed under the [MIT License](LICENSE.md). Copyright and credit remain with Mortuusars and the upstream contributors. This repository preserves the original license and identifies Filmorae only as the maintainer of the unofficial port.
