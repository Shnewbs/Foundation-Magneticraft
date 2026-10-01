# Ore and material progression (26.3)

Five legacy ores and five storage blocks use original textures. Mining levels match the source: stone for copper/lead/pyrite; iron for cobalt/tungsten. Default ore drops remain the ore block.

Furnace yields: ore/dust/rocky chunk gives one ingot; clean chunk gives two; galena rocky chunk gives two lead ingots. Pyrite has no furnace recipe. Sulfur storage recipes use nine sulfur, matching the original.

Only copper, lead, tungsten and pyrite generate by default, with source vein counts, sizes and exclusive upper Y bounds. Cobalt is registered but has no default worldgen. Generation uses modern vanilla veins and stone-replaceable tags in Overworld biomes. The exact legacy voxel algorithm is not reproduced. No retrogen is performed.

Data-pack configuration: override configured/placed features to change vein size, frequency and heights; override data/magneticraft/neoforge/biome_modifier/<metal>_ore.json with {"type":"neoforge:none"} to disable an ore. Restart the world/server; only new chunks are affected.

26.3 uses worldgen/feature with flattened configuration, new recipe ingredients and loot conditions. 1.21.1 uses worldgen/configured_feature and its own codecs.

Manual machines, oil reservoirs, limestone generation, sulfur fuel behavior, and integration categories remain pending. This is an early progression slice, not the completed phase.
