# Port status: 1.21.1

Version: 0.0.1a.R7 (early alpha)

Loader: NeoForge 21.1.252.

Fabricator implementation: nine persistent ghost slots and a nine-slot real buffer, manual craft/clear buttons, server recipe preview and ingredient allocation across the buffer and six loaded neighbors. Crafting rechecks inputs; ghost slots cannot yield items. Remainders return to their source with buffer/drop fallback. 1.21.1 simulates costs before extraction and restores actual extracted stacks on failure; custom inventory handlers must honor the standard capability contract. Original top/side/bottom textures and the copper/iron/redstone/crafting-table recipe are migrated. The bounded allocation regression program checks shared capacities and overlapping ingredient candidates.

The port also includes the original 27-slot box with persistent inventory, a vanilla three-row menu, shift-click, comparator output and the target's item automation capability. The original texture and stick/plank recipe are migrated. Sluice, mesh and box crafting now unlock in the recipe book. Sluice crafting uses two planks, two sticks, one mesh and three smooth stone slabs; mesh requires eight strings and one iron light plate.

Scripting: optional native KubeJS and CraftTweaker adapters are included on 1.21.1. They add/replace/disable crushing and sluice recipes by namespace:path ID, validate inputs/outputs, keep independent owner layers, invalidate lookup caches and clean up on script reload/server stop. CraftTweaker edits use undoable runtime actions. Example scripts and target support matrix: port/SCRIPTING.md. 26.3 includes the shared override API and datapack support; native adapters await verified compatible upstream builds. Neither scripting dependency is bundled or required.

Implemented totals: 96 unique item/block-item registrations; 71 crafting/smelting recipes; 34 crushing recipes; 16 sluice recipes; original material/ore/manual-machine textures; four source-derived ore distributions; mining/compatibility tags; crushing table and three hammers; sluice/mesh; storage box; fabricator.

Release checks: resource/progression validation, source model reconstruction/parity, both target compile/package builds, crushing and sluice regression programs, script owner isolation/precedence/reload/rollback/cache regression checks, fabricator allocation capacity/overlap/backtracking checks, and 1.21.1 adapter compilation against KubeJS 2101.7.2-build.377 and CraftTweaker 21.0.38. No client/world/dedicated-server or live scripting execution is claimed.

Known limits: fabricator GUI, persistence, ghost-slot safety, container remainders, neighboring inventories and multiplayer still require in-game validation. Power, fluids, oil reservoirs, limestone, computers, JEI/Jade and other integrations remain pending. Sluice general fluid-container compatibility, continuous animation, source flow sounds/water tint and gameplay verification are pending. Crushing table item display, source effects/fire and hammer combat attributes are pending. Steel production is unported. Existing 1.12 worlds are unsupported. Check box persistence, drops, full inventories, transfers and multiplayer, plus scripting reload with each dependency present/absent in a test world.
