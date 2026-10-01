# Manual processing port

The crushing table is the first migrated machine, on both targets. Right-click its top with an input, then use a stone, iron, or steel hammer. Damage threshold is 40; speeds are 8/10/15, so completion takes 5/4/3 hits. Hammer durability is 130/250/750 and each valid survival hit costs one durability. A weak hammer spends neither durability nor progress. Outputs remain on the table until retrieved by an empty hand or another item. An empty table remembers the last input for hammer quick-loading from the player inventory.

Inventory and partial work persist and sync from the server. Breaking/replacing the block drops remaining contents once; pistons cannot move it. Creative insertion and hammering do not consume items or durability. Failed/partial retrieval keeps the remainder. Only valid recipe inputs can be inserted. The action bar shows contents/progress; the original table geometry and textures are migrated. Displayed item rendering, custom hit sounds/particles and optional blaze-rod fire are pending.

34 source-derived crushing recipes cover ores (shared c:ores tags, including optional compatibility metals), pyrite, storage-block plates, steel plates, bones/rods, skulls and stone variants. Original limestone recipes await limestone registration. Steel has no production chain yet, so its hammer is primarily for creative/integration use. Weapon combat properties are pending.

Datapack extension: place JSON at data/<namespace>/magneticraft/crushing/<name>.json. Format: ingredient is an item ID or #item-tag; result contains id and count; mining_level is 0–4. Set enabled:false to disable an existing recipe by overriding its same path. Exact items precede tag matches; ties use resource ID order. Resources refresh on /reload; invalid recipes are logged and skipped. This dedicated loader is not yet integrated with the vanilla recipe registry, JEI or KubeJS. This is a temporary bridge while their APIs are migrated.

Next: sluice box two-block placement, batch water processing and downstream activation (80 ticks per cycle, capacity 10, chain delay 20 ticks); then box/fabricator inventory and the power-system migration.

Validation: compile/package checks and pure Java threshold/reset/restore regression checks. Client/world/server gameplay validation remains pending. Check insertion, all hammer tiers, full inventories, breaking, restart, chunk reload and datapack reload in a test world before production use.
