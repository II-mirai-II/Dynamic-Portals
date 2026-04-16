Title: NeoForge Update Checker | NeoForged docs

URL Source: https://docs.neoforged.net/docs/1.21.1/misc/updatechecker

Markdown Content:
# NeoForge Update Checker | NeoForged docs

[Skip to main content](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker#__docusaurus_skipToContent_fallback)

[![Image 1: NeoForged Logo](https://docs.neoforged.net/img/logo.svg) **Homepage**](https://docs.neoforged.net/)[NeoForge Documentation](https://docs.neoforged.net/docs/1.21.1/gettingstarted/)[Toolchain Features](https://docs.neoforged.net/toolchain/docs/)[Primers](https://docs.neoforged.net/primer/docs/)[User Guide](https://docs.neoforged.net/user/docs/)[Modpack Development](https://docs.neoforged.net/modpack/docs/)

[1.21 - 1.21.1](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker)
*   [1.21.11](https://docs.neoforged.net/docs/misc/updatechecker)
*   [1.21.9 - 1.21.10](https://docs.neoforged.net/docs/1.21.10/misc/updatechecker)
*   [1.21.6 - 1.21.8](https://docs.neoforged.net/docs/1.21.8/misc/updatechecker)
*   [1.21.5](https://docs.neoforged.net/docs/1.21.5/misc/updatechecker)
*   [1.21.4](https://docs.neoforged.net/docs/1.21.4/misc/updatechecker)
*   [1.21.2 - 1.21.3](https://docs.neoforged.net/docs/1.21.3/misc/updatechecker)
*   [1.21 - 1.21.1](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker)
*   [1.20.5 - 1.20.6](https://docs.neoforged.net/docs/1.20.6/misc/updatechecker)
*   [1.20.3 - 1.20.4](https://docs.neoforged.net/docs/1.20.4/misc/updatechecker)

[Contributing](https://docs.neoforged.net/contributing)[GitHub](https://github.com/neoforged/documentation)

Search K

*   [Getting Started](https://docs.neoforged.net/docs/1.21.1/gettingstarted/) 
*   [Concepts](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker#) 
*   [Blocks](https://docs.neoforged.net/docs/1.21.1/blocks/) 
*   [Items](https://docs.neoforged.net/docs/1.21.1/items/) 
*   [Block Entities](https://docs.neoforged.net/docs/1.21.1/blockentities/) 
*   [Resources](https://docs.neoforged.net/docs/1.21.1/resources/) 
*   [Inventories & Transfers](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker#) 
*   [Data Storage](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker#) 
*   [GUIs](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker#) 
*   [Worldgen](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker#) 
*   [Networking](https://docs.neoforged.net/docs/1.21.1/networking/) 
*   [Advanced Topics](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker#) 
*   [Miscellaneous](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker#) 
    *   [Configuration](https://docs.neoforged.net/docs/1.21.1/misc/config)
    *   [Debug Profiler](https://docs.neoforged.net/docs/1.21.1/misc/debugprofiler)
    *   [Game Tests](https://docs.neoforged.net/docs/1.21.1/misc/gametest)
    *   [Key Mappings](https://docs.neoforged.net/docs/1.21.1/misc/keymappings)
    *   [Resource Locations](https://docs.neoforged.net/docs/1.21.1/misc/resourcelocation)
    *   [NeoForge Update Checker](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker)

This is documentation for NeoForged**1.21 - 1.21.1**, which is no longer actively maintained.

For up-to-date documentation, see the **[latest version](https://docs.neoforged.net/docs/misc/updatechecker)** (1.21.11).

*   [](https://docs.neoforged.net/)
*   Miscellaneous
*   NeoForge Update Checker

Version: 1.21 - 1.21.1

On this page

# NeoForge Update Checker

NeoForge provides a very lightweight, opt-in, update-checking framework. If any mods have an available update, it will show a flashing icon on the 'Mods' button of the main menu and mod list along with the respective changelogs. It _does not_ download updates automatically.

## Getting Started[​](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker#getting-started "Direct link to Getting Started")

The first thing you want to do is specify the `updateJSONURL` parameter in your `mods.toml` file. The value of this parameter should be a valid URL pointing to an update JSON file. This file can be hosted on your own web server, GitHub, or wherever you want as long as it can be reliably reached by all users of your mod.

## Update JSON format[​](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker#update-json-format "Direct link to Update JSON format")

The JSON itself has a relatively simple format as follows:

`{    "homepage": "<homepage/download page for your mod>",    "<mcversion>": {        "<modversion>": "<changelog for this version>",         // List all versions of your mod for the given Minecraft version, along with their changelogs        // ...    },    "promos": {        "<mcversion>-latest": "<modversion>",        // Declare the latest "bleeding-edge" version of your mod for the given Minecraft version        "<mcversion>-recommended": "<modversion>",        // Declare the latest "stable" version of your mod for the given Minecraft version        // ...    }}`

This is fairly self-explanatory, but some notes:

*   The link under `homepage` is the link the user will be shown when the mod is outdated.

*   NeoForge uses an internal algorithm to determine whether one version string of your mod is "newer" than another. Most versioning schemes should be compatible, but see the `ComparableVersion` class if you are concerned about whether your scheme is supported. Adherence to [Maven versioning](https://docs.neoforged.net/docs/1.21.1/gettingstarted/versioning) is highly recommended.

*   The changelog string can be separated into lines using `\n`. Some prefer to include a abbreviated changelog, then link to an external site that provides a full listing of changes.

*   Manually inputting data can be chore. You can configure your `build.gradle` to automatically update this file when building a release as Groovy has native JSON parsing support. Doing this is left as an exercise to the reader.

*   Some examples can be found here for [nocubes](https://cadiboo.github.io/projects/nocubes/update.json), [Corail Tombstone](https://github.com/Corail31/tombstone_lite/blob/master/update.json) and [Chisels & Bits 2](https://github.com/Aeltumn/Chisels-and-Bits-2/blob/master/update.json).

## Retrieving Update Check Results[​](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker#retrieving-update-check-results "Direct link to Retrieving Update Check Results")

You can retrieve the results of the NeoForge Update Checker using `VersionChecker#getResult(IModInfo)`. You can obtain your `IModInfo` via `ModContainer#getModInfo`, where `ModContainer` can be added as a parameter to your mod constructor. You can obtain any other mod's `ModContainer` using `ModList.get().getModContainerById(<modId>)`. The returned object has a method `#status` which indicates the status of the version check.

| Status | Description |
| --- | --- |
| `FAILED` | The version checker could not connect to the URL provided. |
| `UP_TO_DATE` | The current version is equal to the recommended version. |
| `AHEAD` | The current version is newer than the recommended version if there is not latest version. |
| `OUTDATED` | There is a new recommended or latest version. |
| `BETA_OUTDATED` | There is a new latest version. |
| `BETA` | The current version is equal to or newer than the latest version. |
| `PENDING` | The result requested has not finished yet, so you should try again in a little bit. |

The returned object will also have the target version and any changelog lines as specified in `update.json`.

[Previous Resource Locations](https://docs.neoforged.net/docs/1.21.1/misc/resourcelocation)

*   [Getting Started](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker#getting-started)
*   [Update JSON format](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker#update-json-format)
*   [Retrieving Update Check Results](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker#retrieving-update-check-results)

Docs

*   [NeoForge Documentation](https://docs.neoforged.net/docs/gettingstarted/)
*   [Toolchain Features](https://docs.neoforged.net/toolchain/docs/)
*   [Primers](https://docs.neoforged.net/primer/docs/)
*   [User Guide](https://docs.neoforged.net/user/docs/)
*   [Modpack Development](https://docs.neoforged.net/modpack/docs/)
*   [Contributing to the Documentation](https://docs.neoforged.net/contributing)

Links

*   [Discord](https://discord.neoforged.net/)
*   [Main Website](https://neoforged.net/)
*   [GitHub](https://github.com/neoforged/documentation)

NOT AN OFFICIAL MINECRAFT WEBSITE. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.

Copyright © 2026, under the MIT license. Built with Docusaurus.