# Port status: 26.3

Version: 0.0.1a.R5 (early alpha)

Loader: NeoForge 26.3.0.39-beta.

This revision adds the sluice box and mesh. Both halves share one server-side batch inventory: capacity 10, 80 ticks per water bucket, downstream activation after 20 ticks, and saved inventory/cycle timing. Busy batches cannot restart. 16 source-derived sluice recipes preserve per-input independent rolls, including guaranteed lead and silver from galena. The original body/water geometry and UVs are converted to native models; fill animates in eight-tick steps. See port/MANUAL_PROCESSING.md for controls and datapack extension.

Implemented totals: 94 unique item/block-item registrations (77 metallic items, sulfur, five ores and five storage blocks, crushing table, three hammers, mesh and sluice box); 69 crafting/smelting recipes; 34 crushing recipes; 16 sluice recipes; original textures; mining/compatibility tags; four source-derived ore distributions.

Release checks: resource/progression validation, source model reconstruction/parity, both target compile/package builds, crushing threshold/reset/restore regression checks, and sluice timing/capacity/busy-start/persistence regression checks are required by the paired release workflow. No client/world/dedicated-server gameplay testing is claimed.

Known limits: sluice general fluid-container compatibility, continuous renderer animation, source flow sounds/water tint, world water interactions and gameplay verification remain pending. Idle-only batch insertion/retrieval is intentional. Box/fabricator, power, fluid processing, oil reservoirs, limestone, computers, integrations and sulfur fuel behavior remain unported. Crushing table item display, source sounds/particles, optional blaze fire and hammer combat attributes are pending. Steel production is unported. Worldgen uses modern vanilla veins with original default Y ranges. Existing 1.12 worlds are unsupported.
