# Processing integrations and automation

Both target builds include optional JEI recipe categories and Jade manual-machine tooltips. No optional dependency is bundled or required to play.

| Target | JEI compiled API | Jade compiled artifact | Native scripting adapters |
| --- | --- | --- | --- |
| 1.21.1 | 19.57.0.451 | 15.10.6+neoforge | KubeJS / CraftTweaker; see SCRIPTING.md |
| 26.3 | 31.9.0.58 | 26.3.5+neoforge | Shared recipe API and datapacks; native adapters are not included |

Install the target's JEI/Jade versions or versions allowed by the mod metadata. API compilation is verified; a running client with each dependency present/absent still requires gameplay validation.

## JEI

Crushing and sluicing have dedicated categories and machine catalysts. Inputs include tag alternatives; outputs show stack sizes and independent probabilities. Sluicing shows a water bucket as the representative water input, the 1000 mB cost and 80-tick cycle. Capability-compatible water containers may also supply water. Crushing shows the recipe tier; the actual input block's mining tier may impose an additional requirement.

The server sends its effective recipes on login and datapack reload. Script owner revisions are coalesced once per server tick. Removed and changed recipes are hidden; new versions are added to JEI. An unchanged recipe is never added twice, and disconnect clears the active client catalog. Ghost-grid recipe transfer is not implemented. JEI retains hidden historical recipe entries during a session; reconnect/restart its runtime to clear that history after extensive script edits.

Synchronization is bounded to 4096 recipe entries, 64 outputs per entry and 524288 JSON characters. IDs, tiers, counts and probabilities are validated before display. If a modpack exceeds the total payload budget, the server logs a warning and sends an empty display catalog; processing itself remains available. Oversized catalogs need a future chunked sync protocol.

## Jade

Server-provided tooltips show sluice input, remaining ticks and water buffer; crushing progress and stored count; box/fabricator occupied slots and fabricator pattern count. The outlet half resolves to the loaded central sluice block without forcing a chunk load. Tooltips do not grant inventory access or run crafting.

## Sluice automation

The center block exposes the target's item and fluid capabilities. Hoppers/pipes insert only recipe-valid, matching inputs up to ten items (also respecting the item's stack limit). The input can be extracted while idle. Active cycles reject input insertion/extraction and incoming pipe water.

A persisted water-only buffer accepts up to 1000 mB, including partial fills. A full buffer starts a cycle on the next block tick only when valid input is present. It consumes exactly 1000 mB. Outputs retain the original drop-at-outlet behavior; a hopper below the outlet can collect them. Water extraction from the processing buffer is unavailable. The automatic water buffer is a port addition; the original processing time, capacity and yield rolls are preserved.

Right-clicking with a capable container drains exactly one bucket of water. Failed/insufficient drains do not activate a cycle. Creative use retains its container. 1.21.1 drains a single-item copy and commits the replacement only after success. 26.3 uses ItemAccess and transactions, including stashing extra container items.

On 26.3, item and water views share one snapshot journal: a combined transaction restores both views on abort and sends updates only after root commit. External handlers must honor the NeoForge capability contract.

## Processing effects and configuration

Original crushing hit/final and sluice flow/end OGG sounds are restored. Crushing has server-dispatched hit particles. Blaze rods restore the source's five-second fire behavior; it also applies to the finishing hit in this port. Disable `crushingTableCausesFire` in the server config if desired. 1.21.1 uses the world server config; 26.3 uses synced configuration with the loader's world override support.

Live container, pipe/hopper, chain, multiplayer, packet-order and optional-dependency tests are still pending. This is an early alpha, not complete Magneticraft parity.


R9 addition: passive sluicing accepts a loaded water-tag source (including a waterlogged source) immediately behind or on either horizontal side of the main intake, at the same block height. Outlet-side, flowing non-source, above and below water do not qualify. The source is never drained or replaced. Source-fed batches retain the 80-tick duration and yields and preserve any pipe buffer. Removing the source pauses the remaining work and downstream delay; replacing it resumes, or a water container can finish a paused batch. Three adjacent positions are checked once per second, staggered by position, and immediately after loading. The source supplies continuous animated vanilla flowing-water geometry through both halves even when empty; no world water is spawned at the outlet. Block state updates occur only when flow/processing state changes. Live visual, multiplayer and automation gameplay verification remains pending.
