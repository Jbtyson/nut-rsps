# Agent guide

## Project and code map

This repository contains the Elvarg RuneScape private server and its Java desktop
client. They are **separate Gradle builds**, not subprojects of a root build.
Client/server protocol and cache compatibility matter when changing either side.

- `ElvargServer\game\src\main\java\com\elvarg\Server.java` starts the server.
  In its `game` package, `GameBuilder` loads definitions and plugins, `GameEngine`
  schedules ticks, and `World` processes game state. The tick interval is defined
  in `GameConstants` (currently 600 ms).
- `ElvargServer\game\src\main\java\com\elvarg\game\` contains gameplay:
  `content` for features, `entity` for players/NPCs, `model` for shared models,
  commands and dialogues, `task` for scheduled behavior, and `collision` for
  movement/clipping. `definition\loader` loads the JSON definitions.
- `ElvargServer\game\src\main\java\com\elvarg\net\` owns Netty networking,
  login, codecs and packets. `packet\PacketExecutor.java` defines the incoming
  handler interface; `packet\PacketSender.java` handles outgoing messages.
- `ElvargServer\game\src\main\kotlin\com\elvarg\plugin\` implements the
  Kotlin script/event framework. Actual plugins live in
  `ElvargServer\plugin\src\main\kotlin\`; `TestEvents.plugin.kts` is a small
  event-handler example, **not an automated test**.
- `ElvargServer\data\definitions\` contains game definitions and spawns;
  `ElvargServer\data\clipping\` contains collision data.
- `ElvargClient\src\main\java\com\runescape\GameWindow.java` is the Swing
  entry point. `Client.java` in that directory handles much of the client logic;
  nearby `graphics`, `scene`, `cache`, `io` and `net` packages own rendering,
  world display, assets and protocol support. `Configuration.java` controls
  connection and client options.
- `ElvargClient\src\main\java\app\rsps\` contains the Discord OAuth integration.
  `ElvargClient\Cache\` contains bundled binary game assets, not build output.

## Local workflow

Use each component's checked-in Gradle wrapper. The server uses Gradle 8.1.1,
Kotlin 1.8.10 and a **JDK 17 toolchain**. The client uses Gradle 7.2 and Java 8
source compatibility; its CI uses JDK 11. JDK 17 can run both builds and the
desktop client. Avoid assuming the legacy Applet-based client works on newer
JDKs that remove those APIs.

The following are PowerShell commands. Run them from the directory shown,
relative to the repository root; there is no root `gradlew.bat`.

| Working directory | Command | Purpose |
| --- | --- | --- |
| `ElvargServer` | `.\gradlew.bat :game:classes --console=plain` | Compile core Java/Kotlin and resources |
| `ElvargServer` | `.\gradlew.bat :plugin:classes --console=plain` | Compile plugins and their core dependency |
| `ElvargServer` | `.\gradlew.bat build --console=plain` | Full server build, as used by CI |
| `ElvargClient` | `.\gradlew.bat classes --console=plain` | Compile client and resources |
| `ElvargClient` | `.\gradlew.bat build --console=plain` | Full client build, as used by CI |
| `ElvargServer` | `.\gradlew.bat :game:run --console=plain` | Start the game server; explicit runtime authorization only |
| `ElvargClient` | `.\gradlew.bat run --console=plain` | Open the desktop client; explicit runtime authorization only |

The Java/Kotlin Gradle plugins supply compilation and test tasks, but there are
currently no automated test source sets populated or test-framework dependencies.
There is no configured standalone lint/format task. The workflows named
"Pull Request Tests" run `build`; a successful build is not gameplay coverage.
For a narrow source change, start with the applicable compilation command.
Server `:game:compileKotlin` finalizes with `:plugin:build`, so core compilation
also invokes plugin work (see `ElvargServer\game\build.gradle.kts`).

Dependencies and wrappers may require network access on first use. Server
dependency versions come from `ElvargServer\gradle\libs.versions.toml` and
`ElvargServer\versions.properties`; `_` versions are resolved by refreshVersions.
Do not replace them with guessed versions.

## Patterns and compatibility constraints

- NPC implementations are discovered under
  `com.elvarg.game.entity.impl.npc.impl` by `Systems.init()` using `@Ids`.
  Follow `ElvargServer\game\src\main\java\com\elvarg\game\entity\impl\npc\impl\Banker.java`
  for an `NPC` implementing `NPCInteraction`; include all relevant NPC IDs.
- Plugins use the `Script` template and `.plugin.kts` extension. Discovery in
  `PluginManager` scans `com.elvarg.plugin`; keep plugin packages under that
  namespace and follow the `on<Event> { then { ... } }` example above.
  The plugin module depends on `game`; `game` loads `plugin` at runtime.
- Match the client `Configuration` port/UID and server `NetworkConstants` /
  `GameConstants`. Development defaults connect to `localhost:43595`.
  Protocol changes need coordinated client and server handling.
- RSA login framing has a one-byte block length. The current protocol uses a
  1024-bit key; increasing it requires coordinated framing changes. Server
  `ELVARG_RSA_PRIVATE_EXPONENT` and optional `ELVARG_RSA_MODULUS` overrides must
  match the client public key in
  `ElvargClient\src\main\java\com\runescape\io\Buffer.java`.
  Bundled keys are development defaults, not production secrets.
- `ELVARG_DEV_TOOLS` enables developer/stress tooling and must stay disabled in
  production. `PLAYER_BOT_PASSWORD` configures the bot password.

## Runtime and file boundaries

Server data paths are relative to **`ElvargServer\game`** (`..\data\...`).
The `:game:run` task supplies that working directory; preserve it for manual
launches. The client resolves `.\Cache\` relative to **`ElvargClient`**.
Launching either application from the repository root with a bare Java command
can therefore break asset/data loading.

`GameConstants.PLAYER_PERSISTENCE` selects JSON-file persistence by default,
writing characters under `ElvargServer\data\saves\characters\`; normal local
startup does not require DynamoDB. The alternate DynamoDB implementation is in
`ElvargServer\game\src\main\java\com\elvarg\game\entity\impl\player\persistence\`
and reads `PLAYER_TABLE_NAME` when selected.

Treat `ElvargServer\data\saves\` as mutable runtime/player data, not fixtures;
some ban/mute files there are tracked. Do not commit private saves or incidental
runtime changes. Client `Cache\settings.dat` is local, ignored state. Gradle
`.gradle` directories and component `build` directories are generated; edit
sources rather than compiled output. Cache binaries and server clipping data
are inputs, not disposable caches. Starting/stopping servers or clients and
changing live data require explicit authorization, not just a code-change request.

## Further reading

- [README](README.md): project background and upstream contribution workflow.
- [Server CI](.github/workflows/server-tests.yml) and
  [client CI](.github/workflows/client-tests.yml): build commands and path filters.
  Server CI selects JDK 18, while the build explicitly requires a JDK 17 toolchain;
  local setup should follow the toolchain requirement.
