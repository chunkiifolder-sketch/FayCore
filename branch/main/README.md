# FayCore

A Fabric utility / quality-of-life mod for **Minecraft 26.1.2**.

**Package:** `chunk.faye.mod_tog.faycore`
**Mod id:** `faycore`

## Features

- **Anti-crash / exploit protection** — guards against malicious NBT, packets, components, entities, particles, chat, titles, item tooltips, boss bars, XP orbs, and more.
- **Macro engine** — group-based macros with custom variables and expression evaluation.
- **Command managers** — repeating commands, one-shot commands, and "ghost" commands.
- **Skill system** — press / hold / release casting (laser, TNT, fling, delete, teleport).
- **Wings** — particle wings with several styles and sizes.
- **Guns** — raycast firing with custom kill/death messages.
- **Invsee** — remote inventory viewer.
- **FastRun / selection / region tools.**

## Added in this build

| Feature | Key |
|---------|-----|
| Dash | `X` |
| Auto-sprint (toggle) | `K` |
| Fly (toggle) | `H` |
| Fullbright (toggle) | `C` |
| Auto-tool (toggle) | `N` |
| Waypoint — save / teleport | `Y` / `U` |
| Zoom (hold) | `Z` |

## Project layout

```
src/main/java/chunk/faye/mod_tog/faycore/   # source (renamed package)
src/main/resources/                          # fabric.mod.json, mixins, access widener, assets
releases/                                    # built jars
```

## Build

- Java **25**
- Gradle **9.7+** + Fabric Loom
- Minecraft **26.1.2** (de-obfuscated — no mappings declaration needed)
- Fabric Loader `>=0.19.3`, Fabric API `0.155.3+26.1.2`

> Note: the Java source in `src/` is a decompiled reconstruction of the compiled mod
> (no original source exists for 1.1.0-beta.1), so it is provided for reference and editing.
> The ready-to-run build is in `releases/`.

## Credits

- FayeCruz, Chunkiifolder

## License

Not specified.
