# Port status: 1.21.1

Version: 0.0.1a.R9 (early alpha)

Loader: NeoForge 21.1.252.

This update expands manual processing and modpack compatibility. Sluices accept capability-compatible water containers and expose item/fluid automation. A persisted 1000 mB water-only buffer accepts partial pipe fills and starts an 80-tick batch only when a full buffer and valid input are present. Hopper input is limited to matching recipe inputs and rejects transfer while busy. Outputs retain the source outlet-drop behavior. Automatic piped-water starting is a port addition; source time, capacity and independent yields are preserved.

Optional JEI categories show effective server crushing/sluice recipes, tag alternatives, outputs, probabilities, water cost and processing time. Recipe catalogs synchronize on login, datapack reload and coalesced script revisions; disconnect clears active client state. Optional Jade displays server machine progress, water/input counts, storage and fabricator pattern count, including the sluice outlet half. Neither dependency is bundled or required. Compiled integration versions: JEI 19.57.0.451, Jade 15.10.6+neoforge. See port/INTEGRATIONS.md for protocol budgets and runtime limits.

Recipe lookups now cache once per resource-manager generation and script revision, removing repeated datapack resource scans from hot interactions. A regression check performs 100000 hits with a single loader call and covers reload, script changes, cache clear and failed loads. This is an algorithmic improvement; no factory TPS/FPS benchmark is claimed.

Original crushing hit/final and sluice flow/end sounds are restored. Crushing adds server hit particles and five-second blaze-rod fire, including the finishing hit; the fire option is configurable through the world server configuration.

Existing implementation includes materials/ores, four source ore distributions, crushing table and three hammers, sluice/mesh, a persistent 27-slot box, and the fabricator with nine ghost inputs, nine real slots, adjacent inventories, server craft requests and remainder handling. 1.21.1 uses standard item/fluid capabilities. Fabricator failure recovery restores actual extracted items. Ghosts are never exposed through automation or dropped as real items.

Scripting: 1.21.1 includes optional KubeJS/CraftTweaker adapters. Both targets include the shared recipe override API and datapack loaders. Native 26.3 scripting adapters are not included. See port/SCRIPTING.md. Existing native adapters remain compiled against KubeJS 2101.7.2-build.377 and CraftTweaker 21.0.38.

Implemented totals: 96 unique item/block-item registrations; 71 crafting/smelting recipes; 34 crushing recipes; 16 sluice recipes. Both target implementation builds passed resource/model validation, optional integration API compilation and regression checks for crushing, sluice cycles, water capacity/busy behavior, fabricator allocation, script owner layers, recipe cache generations and bounded catalog serialization. Release automation rebuilds the exact paired commits and validates uploaded checksums before publication.

Known limits: live client/world/dedicated-server, multiplayer, fluid-container/pipe/hopper compatibility, GUI rendering, JEI/Jade present/absent, script execution, packet ordering and save/reload gameplay checks remain pending. JEI ghost-grid transfer is absent; its hidden historical entries persist until runtime restart after extensive script edits. Oversized catalogs clear client displays with a warning rather than sending an unbounded packet. Crushing stored-item rendering/combat attributes and sluice live visual validation remains pending. Power, heat, steel production, fluid/oil/limestone systems, computers, multiblocks and the remaining logistics remain unported. Existing 1.12 worlds are unsupported. This does not complete the next roadmap milestone.


R9 addition: passive sluicing accepts a loaded water-tag source (including a waterlogged source) immediately behind or on either horizontal side of the main intake, at the same block height. Outlet-side, flowing non-source, above and below water do not qualify. The source is never drained or replaced. Source-fed batches retain the 80-tick duration and yields and preserve any pipe buffer. Removing the source pauses the remaining work and downstream delay; replacing it resumes, or a water container can finish a paused batch. Three adjacent positions are checked once per second, staggered by position, and immediately after loading. The source supplies continuous animated vanilla flowing-water geometry through both halves even when empty; no world water is spawned at the outlet. Block state updates occur only when flow/processing state changes. Live visual, multiplayer and automation gameplay verification remains pending.

Both target R9 implementation builds passed compilation, resource/model checks and the new source preference, buffer fallback and pause/resume regression checks. Live in-game validation remains pending.
