# Port status: 26.3

Version: 0.0.1a.R4 (early alpha)

Loader: NeoForge 26.3.0.39-beta. This revision updates the 26.3 loader baseline; the paired 1.21.1 artifact retains its existing loader.

Implemented: 77 metallic items; sulfur; five ores and storage blocks; original material textures; furnace/compression recipes; mining/compatibility tags; four ore distributions; manual crushing table and three hammer items; 34 dedicated datapack crushing recipes. See port/MANUAL_PROCESSING.md for controls and remaining parity work.

Release checks: resource/progression validation, both target compile/package builds, and crushing threshold/reset/restore regression checks are required by the paired release workflow. No client/world/dedicated-server gameplay testing is claimed.

Known limits: sluice box, box/fabricator, power, fluid processing, oil reservoirs, limestone, computers, integrations and sulfur fuel behavior remain unported. Crushing table item display, source sounds/particles, optional blaze fire and hammer combat attributes are pending. Steel production is unported. Worldgen uses modern vanilla veins with original default Y ranges. Existing 1.12 worlds are unsupported.
