# Manual processing port

The crushing table is the first migrated machine, on both targets. Right-click its top with an input, then use a stone, iron, or steel hammer. Damage threshold is 40; speeds are 8/10/15, so completion takes 5/4/3 hits. Hammer durability is 130/250/750 and each valid survival hit costs one durability. A weak hammer spends neither durability nor progress. Outputs remain on the table until retrieved by an empty hand or another item. An empty table remembers the last input for hammer quick-loading from the player inventory.

Inventory and partial work persist and sync from the server. Breaking/replacing the block drops remaining contents once; pistons cannot move it. Creative insertion and hammering do not consume items or durability. Failed/partial retrieval keeps the remainder. Only valid recipe inputs can be inserted. The action bar shows contents/progress; the original table geometry and textures are migrated. Original hit/final sounds, hit particles and configurable blaze-rod fire are implemented. Displayed item rendering is still pending.

34 source-derived crushing recipes cover ores (shared c:ores tags, including optional compatibility metals), pyrite, storage-block plates, steel plates, bones/rods, skulls and stone variants. Original limestone recipes await limestone registration. Steel has no production chain yet, so its hammer is primarily for creative/integration use. Weapon combat properties are pending.

Datapack extension: place JSON at data/<namespace>/magneticraft/crushing/<name>.json. Format: ingredient is an item ID or #item-tag; result contains id and count; mining_level is 0–4. Set enabled:false to disable an existing recipe by overriding its same path. Exact items precede tag matches; ties use resource ID order. Resources refresh on /reload; invalid recipes are logged and skipped. The dedicated loader supports the optional 1.21.1 KubeJS/CraftTweaker adapters described in SCRIPTING.md. The dedicated loader is exposed through server-synchronized JEI categories. Vanilla machine recipe serializer registration remains pending.

## Sluice box

Place with a clear adjacent block in the facing direction; both halves form one machine. Main-hand interaction on either half inserts up to 10 matching recipe inputs while idle. An empty hand retrieves remaining input while idle. A water bucket starts an 80-tick batch and returns an empty bucket in survival; creative keeps its bucket. Outputs drop at the outlet after processing. A box two blocks forward and one block lower activates after 20 ticks if its chunk is loaded; busy boxes cannot restart. Empty boxes can pass activation downstream. Input and exact cycle/chain timing persist.

16 source-derived recipes cover 14 rocky chunks, gravel and sand. Every input rolls each output independently, preserving guaranteed chunks and byproduct probabilities, including guaranteed lead and silver from galena. Recipe removal during a cycle keeps inputs recoverable. Datapacks use data/<namespace>/magneticraft/sluice/<name>.json with ingredient as an item ID or #item-tag, outputs as [{id,count,chance}], and optional enabled:false. Lookup/reload rules match crushing.

The original body/water geometry, UVs and textures are converted to native models with automated reconstruction checks. Ten fill models update in eight-tick steps. Capability-compatible water containers and item/fluid automation are implemented; see INTEGRATIONS.md. Continuous renderer animation, world water interactions and gameplay verification remain pending. Insertion/retrieval is restricted to idle batches to keep consumption deterministic. Pistons cannot move the pair; only the main half has block loot.

## Box

The source-derived box has 27 persistent slots, a vanilla three-row inventory menu with shift-click support, comparator output and the target's item automation capability. The original box texture and stick/plank crafting recipe are migrated. Pistons cannot move it. Placement, break drops, full inventories, hopper/mod transfer, multiplayer and save/reload need gameplay verification.

The sluice, mesh and box now have recipe-book unlock advancements. Obtain a mesh to unlock the sluice recipe, an iron light plate for mesh, or a stick for the box. The sluice uses two planks, two sticks, one mesh and three smooth stone slabs.

The fabricator is implemented below. Power migration, remaining source visuals and gameplay validation are still pending. See SCRIPTING.md and INTEGRATIONS.md for optional integrations.

Validation: compile/package checks and pure Java threshold/reset/restore regression checks. Client/world/server gameplay validation remains pending. Check insertion, all hammer tiers, full inventories, breaking, restart, chunk reload and datapack reload in a test world before production use.

## Fabricator

Craft one from copper ingot, iron ingot, redstone and a crafting table in a two-by-two grid (copper/iron above redstone/table). Open the block; left-click one of the nine pattern slots with a held item to copy a one-item ghost. Right-click clears that ghost. Clear removes the whole pattern. Ghosts never consume, store or dispense real items.

Put ingredients in the nine storage slots or an adjacent inventory. Craft performs one server-validated craft and puts output in the storage buffer, dropping overflow above the block. Container remainders return to their ingredient source, with buffer/drop fallback. Shift-click moves real inventory stacks only. Neighbors in unloaded chunks are ignored.

This is manual crafting, without a timer or redstone auto-crafting. Templates and real storage persist independently. Hoppers access only real storage; breaking the block drops real storage, never ghosts or preview output. The screen uses native target menu APIs. Live client/server, multiplayer and third-party inventory compatibility checks are pending.

## Processing expansion

See [INTEGRATIONS.md](INTEGRATIONS.md) for sluice water containers, piped water and item automation; server-synchronized JEI recipes; Jade tooltips; restored source sounds; and the configurable blaze-rod fire behavior. Recipe lookups cache a generation per resource manager and script revision instead of scanning datapacks on every interaction. Piped-water automatic starting is a port addition; source time, input capacity and independent yield rolls are preserved.
