Title: Mod Files | NeoForged docs

URL Source: https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles

Markdown Content:
The mod files are responsible for determining what mods are packaged into your JAR, what information to display within the 'Mods' menu, and how your mod should be loaded in the game.

## `gradle.properties`[​](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#gradleproperties "Direct link to gradleproperties")

The `gradle.properties` file holds various common properties of your mod, such as the mod id or mod version. During building, Gradle reads the values in these files and inlines them in various places, such as the [neoforge.mods.toml](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#neoforgemodstoml) file. This way, you only need to change values in one place, and they are then applied everywhere for you.

Most values are also explained as comments in [the MDK's `gradle.properties` file](https://github.com/NeoForgeMDKs/MDK-1.21-NeoGradle/blob/main/gradle.properties).

| Property | Description | Example |
| --- | --- | --- |
| `org.gradle.jvmargs` | Allows you to pass extra JVM arguments to Gradle. Most commonly, this is used to assign more/less memory to Gradle. Note that this is for Gradle itself, not Minecraft. | `org.gradle.jvmargs=-Xmx3G` |
| `org.gradle.daemon` | Whether Gradle should use the daemon when building. | `org.gradle.daemon=false` |
| `org.gradle.debug` | Whether Gradle is set to debug mode. Debug mode mainly means more Gradle log output. Note that this is for Gradle itself, not Minecraft. | `org.gradle.debug=false` |
| `minecraft_version` | The Minecraft version you are modding on. Must match with `neo_version`. | `minecraft_version=1.20.6` |
| `minecraft_version_range` | The Minecraft version range this mod can use, as a [Maven Version Range](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html). Note that [snapshots, pre-releases and release candidates](https://docs.neoforged.net/docs/1.21.1/gettingstarted/versioning#minecraft) are not guaranteed to sort properly, as they do not follow maven versioning. | `minecraft_version_range=[1.20.6,1.21)` |
| `neo_version` | The NeoForge version you are modding on. Must match with `minecraft_version`. See [NeoForge Versioning](https://docs.neoforged.net/docs/1.21.1/gettingstarted/versioning#neoforge) for more information on how NeoForge versioning works. | `neo_version=20.6.62` |
| `neo_version_range` | The NeoForge version range this mod can use, as a [Maven Version Range](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html). | `neo_version_range=[20.6.62,20.7)` |
| `loader_version_range` | The version range of the mod loader this mod can use, as a [Maven Version Range](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html). Note that the loader versioning is decoupled from NeoForge versioning. | `loader_version_range=[1,)` |
| `mod_id` | See [The Mod ID](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#the-mod-id). | `mod_id=examplemod` |
| `mod_name` | The human-readable display name of your mod. By default, this can only be seen in the mod list, however, mods such as [JEI](https://www.curseforge.com/minecraft/mc-mods/jei) prominently display mod names in item tooltips as well. | `mod_name=Example Mod` |
| `mod_license` | The license your mod is provided under. It is suggested that this is set to the [SPDX identifier](https://spdx.org/licenses/) you are using and/or a link to the license. You can visit [https://choosealicense.com/](https://choosealicense.com/) to help pick the license you want to use. | `mod_license=MIT` |
| `mod_version` | The version of your mod, shown in the mod list. See [the page on Versioning](https://docs.neoforged.net/docs/1.21.1/gettingstarted/versioning) for more information. | `mod_version=1.0` |
| `mod_group_id` | See [The Group ID](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#the-group-id). | `mod_group_id=com.example.examplemod` |
| `mod_authors` | The authors of the mod, shown in the mod list. | `mod_authors=ExampleModder` |
| `mod_description` | The description of the mod, as a multiline string, shown in the mod list. Newline characters (`\n`) can be used and will be replaced properly. | `mod_description=Example mod description.` |

### The Mod ID[​](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#the-mod-id "Direct link to The Mod ID")

The mod ID is the main way your mod is distinguished from others. It is used in a wide variety of places, including as the namespace for your mod's [registries](https://docs.neoforged.net/docs/1.21.1/concepts/registries#deferredregister), and as your [resource and data pack](https://docs.neoforged.net/docs/1.21.1/resources/) namespaces. Having two mods with the same id will prevent the game from loading.

As such, your mod ID should be something unique and memorable. Usually, it will be your mod's display name (but lower case), or some variation thereof. Mod IDs may only contain lowercase letters, digits and underscores, and must be between 2 and 64 characters long (both inclusive).

info

Changing this property in the `gradle.properties` file will automatically apply the change everywhere, except for the [`@Mod` annotation](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#javafml-and-mod) in your main mod class. There, you need to change it manually to match the value in the `gradle.properties` file.

### The Group ID[​](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#the-group-id "Direct link to The Group ID")

While the `group` property in the `build.gradle` is only necessary if you plan to publish your mod to a maven, it is considered good practice to always properly set this. This is done for you through the `gradle.properties`'s `mod_group_id` property.

The group id should be set to your top-level package. See [Packaging](https://docs.neoforged.net/docs/1.21.1/gettingstarted/structuring#packaging) for more information.

`# In your gradle.properties filemod_group_id=com.example`

The packages within your java source (`src/main/java`) should also now conform to this structure, with an inner package representing the mod id:

`com- example (top-level package specified in group property)    - mymod (the mod id)        - MyMod.java (renamed ExampleMod.java)`

## `neoforge.mods.toml`[​](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#neoforgemodstoml "Direct link to neoforgemodstoml")

The `neoforge.mods.toml` file, located at `src/main/resources/META-INF/neoforge.mods.toml`, is a file in [TOML](https://toml.io/) format that defines the metadata of your mod(s). It also contains additional information on how your mod(s) should be loaded into the game, as well as display information that is displayed within the 'Mods' menu. The [`neoforge.mods.toml` file provided by the MDK](https://github.com/NeoForgeMDKs/MDK-1.21-NeoGradle/blob/main/src/main/resources/META-INF/neoforge.mods.toml) contains comments explaining every entry, they will be explained here in more detail.

The `neoforge.mods.toml` can be separated into three parts: the non-mod-specific properties, which are linked to the mod file; the mod properties, with a section for each mod; and the dependency configurations, with a section for each mod's or mods' dependencies. Some of the properties associated with the `neoforge.mods.toml` file are mandatory; mandatory properties require a value to be specified, otherwise an exception will be thrown.

note

In the default MDK, Gradle replaces various properties in this file with the values specified in the `gradle.properties` file. For example, the line `license="${mod_license}"` means that the `license` field is replaced by the `mod_license` property from `gradle.properties`. Values that are replaced like this should be changed in the `gradle.properties` instead of changing them here.

### Non-Mod-Specific Properties[​](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#non-mod-specific-properties "Direct link to Non-Mod-Specific Properties")

Non-mod-specific properties are properties associated with the JAR itself, indicating how to load the mod(s) and any additional global metadata.

| Property | Type | Default | Description | Example |
| --- | --- | --- | --- | --- |
| `modLoader` | string | **mandatory** | The language loader used by the mod(s). Can be used to support alternative language structures, such as Kotlin objects for the main file, or different methods of determining the entrypoint, such as an interface or method. NeoForge provides the Java loader [`"javafml"`](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#javafml-and-mod) and the lowcode/nocode loader [`"lowcodefml"`](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#lowcodefml). | `modLoader="javafml"` |
| `loaderVersion` | string | **mandatory** | The acceptable version range of the language loader, expressed as a [Maven Version Range](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html). For `javafml` and `lowcodefml`, this is currently version `1`. | `loaderVersion="[1,)"` |
| `license` | string | **mandatory** | The license the mod(s) in this JAR are provided under. It is suggested that this is set to the [SPDX identifier](https://spdx.org/licenses/) you are using and/or a link to the license. You can visit [https://choosealicense.com/](https://choosealicense.com/) to help pick the license you want to use. | `license="MIT"` |
| `showAsResourcePack` | boolean | `false` | When `true`, the mod(s)'s resources will be displayed as a separate resource pack on the 'Resource Packs' menu, rather than being combined with the 'Mod Resources' pack. | `showAsResourcePack=true` |
| `showAsDataPack` | boolean | `false` | When `true`, the mod(s)'s data files will be displayed as a separate data pack on the 'Data Packs' menu, rather than being combined with the 'Mod Data' pack. | `showAsDataPack=true` |
| `services` | array | `[]` | An array of services your mod uses. This is consumed as part of the created module for the mod from NeoForge's implementation of the Java Platform Module System. | `services=["net.neoforged.neoforgespi.language.IModLanguageProvider"]` |
| `properties` | table | `{}` | A table of substitution properties. This is used by `StringSubstitutor` to replace `${file.<key>}` with its corresponding value. | `properties={"example"="1.2.3"}` (can then be referenced by `${file.example}`) |
| `issueTrackerURL` | string | _nothing_ | A URL representing the place to report and track issues with the mod(s). | `"https://github.com/neoforged/NeoForge/issues"` |

### Mod-Specific Properties[​](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#mod-specific-properties "Direct link to Mod-Specific Properties")

Mod-specific properties are tied to the specified mod using the `[[mods]]` header. This is an [array of tables](https://toml.io/en/v1.0.0#array-of-tables); all key/value properties will be attached to that mod until the next header.

`# Properties for examplemod1[[mods]]modId = "examplemod1"# Properties for examplemod2[[mods]]modId = "examplemod2"`

| Property | Type | Default | Description | Example |
| --- | --- | --- | --- | --- |
| `modId` | string | **mandatory** | See [The Mod ID](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#the-mod-id). | `modId="examplemod"` |
| `namespace` | string | value of `modId` | An override namespace for the mod. Must also be a valid [mod ID](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#the-mod-id), but may additionally include dots or dashes. Currently unused. | `namespace="example"` |
| `version` | string | `"1"` | The version of the mod, preferably in a [variation of Maven versioning](https://docs.neoforged.net/docs/1.21.1/gettingstarted/versioning). When set to `${file.jarVersion}`, it will be replaced with the value of the `Implementation-Version` property in the JAR's manifest (displays as `0.0NONE` in a development environment). | `version="1.20.2-1.0.0"` |
| `displayName` | string | value of `modId` | The display name of the mod. Used when representing the mod on a screen (e.g., mod list, mod mismatch). | `displayName="Example Mod"` |
| `description` | string | `'''MISSING DESCRIPTION'''` | The description of the mod shown in the mod list screen. It is recommended to use a [multiline literal string](https://toml.io/en/v1.0.0#string). This value is also translatable, see [Translating Mod Metadata](https://docs.neoforged.net/docs/1.21.1/resources/client/i18n#translating-mod-metadata) for more info. | `description='''This is an example.'''` |
| `logoFile` | string | _nothing_ | The name and extension of an image file used on the mods list screen. The logo must be in the root of the JAR or directly in the root of the source set (e.g. `src/main/resources` for the main source set). | `logoFile="example_logo.png"` |
| `logoBlur` | boolean | `true` | Whether to use `GL_LINEAR*` (true) or `GL_NEAREST*` (false) to render the `logoFile`. In simpler terms, this means whether the logo should be blurred or not when trying to scale the logo. | `logoBlur=false` |
| `updateJSONURL` | string | _nothing_ | A URL to a JSON used by the [update checker](https://docs.neoforged.net/docs/1.21.1/misc/updatechecker) to make sure the mod you are playing is the latest version. | `updateJSONURL="https://example.github.io/update_checker.json"` |
| `modUrl` | string | _nothing_ | A URL to the download page of the mod. Currently unused. | `modUrl="https://neoforged.net/"` |
| `credits` | string | _nothing_ | Credits and acknowledges for the mod shown on the mod list screen. | `credits="The person over here and there."` |
| `authors` | string | _nothing_ | The authors of the mod shown on the mod list screen. | `authors="Example Person"` |
| `displayURL` | string | _nothing_ | A URL to the display page of the mod shown on the mod list screen. | `displayURL="https://neoforged.net/"` |
| `enumExtensions` | string | _nothing_ | The file path of a JSON file used for [enum extension](https://docs.neoforged.net/docs/1.21.1/advanced/extensibleenums) | `enumExtensions="META_INF/enumextensions.json"` |

#### Features[​](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#features "Direct link to Features")

The features system allows mods to demand that certain settings, software, or hardware are available when loading the system. When a feature is not satisfied, mod loading will fail, informing the user about the requirement. These configurations are created using the [array of tables](https://toml.io/en/v1.0.0#array-of-tables)`[[features.<modid>]]`, where `modid` is the identifier of the mod that consumes the feature. Currently, NeoForge provides the following features:

| Feature | Description | Example |
| --- | --- | --- |
| `javaVersion` | The acceptable version range of the Java version, expressed as a [Maven Version Range](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html). This should be the supported version used by Minecraft. | `javaVersion="[17,)"` |
| `openGLVersion` | The acceptable version range of the OpenGL version, expressed as a [Maven Version Range](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html). Minecraft requires OpenGL 3.2 or newer. If you want to require a newer OpenGL version, you can do so here. | `openGLVersion="[4.6,)"` |

#### Mod Properties[​](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#mod-properties "Direct link to Mod Properties")

The mod properties system is a map of arbitrary keys to values that are associated with a particular mod. These can be useful when a mod file defines multiple mods that provide different metadata. From there, the specific property value for some key can be obtained by getting the object value from the map via `IModInfo#getModProperties`. These configurations are created using the [array of tables](https://toml.io/en/v1.0.0#array-of-tables)`[[modproperties.<modid>]]`, where `modid` is the identifier of the mod that consumes the defined properties.

`// Assume we have two mods `mod1` and `mod2` with the following property configuration// [[modproperties.mod1]]// key="value1"// [[modproperties.mod2]]// key="value2"@Mod("mod1")public class ModOne {    private final String key;    public ModOne(ModContainer container) {        // Will store 'value1' in key        this.key = (String) container.getModInfo().getModProperties().get("key");    }}@Mod("mod2")public class ModTwo {    private final String key;    public ModTwo(ModContainer container) {        // Will store 'value2' in key        this.key = (String) container.getModInfo().getModProperties().get("key");    }}`

### Access Transformer-Specific Properties[​](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#access-transformer-specific-properties "Direct link to Access Transformer-Specific Properties")

[Access Transformer-specific properties](https://docs.neoforged.net/docs/1.21.1/advanced/accesstransformers#adding-ats) are tied to the specified access transformer using the `[[accessTransformers]]` header. This is an [array of tables](https://toml.io/en/v1.0.0#array-of-tables); all key/value properties will be attached to that access transformer until the next header. The access transformer header is optional; however, when specified, all elements are mandatory.

| Property | Type | Default | Description | Example |
| --- | --- | --- | --- | --- |
| `file` | string | **mandatory** | See [Adding ATs](https://docs.neoforged.net/docs/1.21.1/advanced/accesstransformers#adding-ats). | `file="at.cfg"` |

### Mixin Configuration Properties[​](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#mixin-configuration-properties "Direct link to Mixin Configuration Properties")

[Mixin Configuration Properties](https://github.com/SpongePowered/Mixin/wiki/Introduction-to-Mixins---The-Mixin-Environment#mixin-configuration-files) are tied to the specified mixin config using the `[[mixins]]` header. This is an [array of tables](https://toml.io/en/v1.0.0#array-of-tables); all key/value properties will be attached to that mixin block until the next header. The mixin header is optional; however, when specified, all elements are mandatory.

| Property | Type | Default | Description | Example |
| --- | --- | --- | --- | --- |
| `config` | string | **mandatory** | The location of the mixin configuration file. | `config="examplemod.mixins.json"` |

### Dependency Configurations[​](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#dependency-configurations "Direct link to Dependency Configurations")

Mods can specify their dependencies, which are checked by NeoForge before loading the mods. These configurations are created using the [array of tables](https://toml.io/en/v1.0.0#array-of-tables)`[[dependencies.<modid>]]`, where `modid` is the identifier of the mod that consumes the dependency.

| Property | Type | Default | Description | Example |
| --- | --- | --- | --- | --- |
| `modId` | string | **mandatory** | The identifier of the mod added as a dependency. | `modId="jei"` |
| `type` | string | `"required"` | Specifies the nature of this dependency: `"required"` is the default and prevents the mod from loading if this dependency is missing; `"optional"` will not prevent the mod from loading if the dependency is missing, but still validates that the dependency is compatible; `"incompatible"` prevents the mod from loading if this dependency is present; `"discouraged"` still allows the mod to load if the dependency is present, but presents a warning to the user. | `type="incompatible"` |
| `reason` | string | _nothing_ | An optional user-facing message to describe why this dependency is required, or why it is incompatible. | `reason="integration"` |
| `versionRange` | string | `""` | The acceptable version range of the language loader, expressed as a [Maven Version Range](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html). An empty string matches any version. | `versionRange="[1, 2)"` |
| `ordering` | string | `"NONE"` | Defines if the mod must load before (`"BEFORE"`) or after (`"AFTER"`) this dependency. If the ordering does not matter, return `"NONE"` | `ordering="AFTER"` |
| `side` | string | `"BOTH"` | The [physical side](https://docs.neoforged.net/docs/1.21.1/concepts/sides) the dependency must be present on: `"CLIENT"`, `"SERVER"`, or `"BOTH"`. | `side="CLIENT"` |
| `referralUrl` | string | _nothing_ | A URL to the download page of the dependency. Currently unused. | `referralUrl="https://library.example.com/"` |

danger

The `ordering` of two mods may cause a crash due to a cyclic dependency, for example if mod A must load `"BEFORE"` mod B and at the same time, mod B must load `"BEFORE"` mod A.

## Mod Entrypoints[​](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#mod-entrypoints "Direct link to Mod Entrypoints")

Now that the `neoforge.mods.toml` is filled out, we need to provide an entrypoint for the mod. Entrypoints are essentially the starting point for executing the mod. The entrypoint itself is determined by the language loader used in the `neoforge.mods.toml`.

### `javafml` and `@Mod`[​](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#javafml-and-mod "Direct link to javafml-and-mod")

`javafml` is a language loader provided by NeoForge for the Java programming language. The entrypoint is defined using a public class with the `@Mod` annotation. The value of `@Mod` must contain one of the mod ids specified within the `neoforge.mods.toml`. From there, all initialization logic (e.g. [registering events](https://docs.neoforged.net/docs/1.21.1/concepts/events) or [adding `DeferredRegister`s](https://docs.neoforged.net/docs/1.21.1/concepts/registries#deferredregister)) can be specified within the constructor of the class.

The main mod class must only have one public constructor; otherwise a `RuntimeException` will be thrown. The constructor may have **any** of the following arguments in **any** order; none of them are explicitly required. However, no duplicate parameters are allowed.

| Argument Type | Description |
| --- | --- |
| `IEventBus` | The [mod-specific event bus](https://docs.neoforged.net/docs/1.21.1/concepts/events#event-buses) (needed for registration, events, etc.) |
| `ModContainer` | The abstract container holding this mod's metadata |
| `FMLModContainer` | The actual container as defined by `javafml` holding this mod's metadata; an extension of `ModContainer` |
| `Dist` | The [physical side](https://docs.neoforged.net/docs/1.21.1/concepts/sides) this mod is loading on |

`@Mod("examplemod") // Must match a mod id in the neoforge.mods.tomlpublic class ExampleMod {    // Valid constructor, only uses two of the available argument types    public ExampleMod(IEventBus modBus, ModContainer container) {        // Initialize logic here    }}`

By default, a `@Mod` annotation is loaded on both [sides](https://docs.neoforged.net/docs/1.21.1/concepts/sides). This can be changed by specifying the `dist` parameter:

`// Must match a mod id in the neoforge.mods.toml// This mod class will only be loaded on the physical client@Mod(value = "examplemod", dist = Dist.CLIENT) public class ExampleModClient {    // Valid constructor    public ExampleModClient(FMLModContainer container, IEventBus modBus, Dist dist) {        // Initialize client-only logic here    }}`

note

An entry in `neoforge.mods.toml` does not need a corresponding `@Mod` annotation. Likewise, an entry in the `neoforge.mods.toml` can have multiple `@Mod` annotations, for example if you want to separate common logic and client only logic.

### `lowcodefml`[​](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles#lowcodefml "Direct link to lowcodefml")

`lowcodefml` is a language loader used as a way to distribute datapacks and resource packs as mods without the need of an in-code entrypoint. It is specified as `lowcodefml` rather than `nocodefml` for minor additions in the future that might require minimal coding.