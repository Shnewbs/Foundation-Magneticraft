# Scripting compatibility

## Targets

| Minecraft | KubeJS | CraftTweaker |
| --- | --- | --- |
| 1.21.1 | Optional native plugin; compile target 2101.7.2-build.377 | Optional ZenCode API; compile target 21.0.38 |
| 26.3 | Common recipe override API and datapacks available; native adapter pending a verified compatible upstream build | Common recipe override API and datapacks available; native adapter pending a verified compatible upstream build |

Neither scripting mod is bundled or required. Do not install 1.21.1 or 26.1 binaries on 26.3. Native adapter compilation is verified in CI; game/script execution with dependencies present/absent remains pending.

## KubeJS 1.21.1

Copy port/examples/kubejs/magneticraft.js.example to kubejs/server_scripts/magneticraft.js. The plugin supplies a server-only Magneticraft binding. Use it inside ServerEvents.recipes. Methods accept explicit namespace:path recipe IDs and item IDs or #item-tags:

- addCrushing(id, input, output, count, miningLevel): count must fit a stack, tier 0–4.
- addSluice(id, input, outputsJson): outputsJson is JSON.stringify([{id, count, chance}, ...]); chance is finite and 0–1.
- removeCrushing(id), removeSluice(id): disable the recipe with that exact ID.

For example, magneticraft:sand refers to data/magneticraft/magneticraft/sluice/sand.json. Reusing an ID replaces its recipe. Vanilla crafting/smelting remain editable with normal KubeJS recipe events; the dedicated machine loaders do not use event.custom or event.remove.

## CraftTweaker 1.21.1

Copy port/examples/crafttweaker/magneticraft.zs.example to scripts/magneticraft.zs. Import mods.magneticraft.Recipes and use the same four method signatures. Sluice outputs are passed as a JSON string. Changes run through CraftTweaker undoable runtime actions, so script reload removes stale edits and rolls back old values.

## Reload and conflicts

Datapacks are the base layer. KubeJS overrides them; CraftTweaker overrides KubeJS on identical machine/recipe IDs. Clearing one script owner leaves the other intact. Recipe cache revisions make changes visible on the next server lookup. Exact-item recipes precede tags; recipe-ID order breaks ties. Inputs, output IDs, counts and probabilities are validated before scripted insertion. Server shutdown clears all script layers.

The built-in recipe counts are unchanged by optional scripts until the user installs an example or supplies their own scripts. Disabling a sluice recipe during a running cycle leaves its input recoverable.

## Datapack fallback on both targets

Use data/<namespace>/magneticraft/crushing/<name>.json or data/<namespace>/magneticraft/sluice/<name>.json with the formats in MANUAL_PROCESSING.md. An enabled:false override disables a built-in recipe. This remains supported without scripting mods.

Upstream references: https://github.com/kube-mods/kubejs/tree/2101 and https://github.com/CraftTweaker/CraftTweaker/tree/1.21.1. Availability checked 2026-10-01 UTC.
