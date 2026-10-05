# Foundations Magneticraft - Dual-Version Port Roadmap

Updated: 2026-10-05
Status: dual-target early alpha in development. R8 processing expansion prepared; paired publication follows successful release checks. See PORT_STATUS.md for exact implementation and verification limits.

## Project direction

Port the original Magneticraft to Minecraft **1.21.1 and 26.3 concurrently**. Maintain two development branches, `1.21.1` and `26.3`. Prepare the 26.3 implementation for a later 26.4 port. Neither target is a deferred follow-up.

The user authorizes routine GitHub management, including branch creation, commits, pushes, workflows, tags, and paired releases. Execution remains subject to available tool permissions.

## Verified source baseline

- Repository: https://github.com/Shnewbs/Foundation-Magneticraft
- Original branch: `1.12`
- Inspected source tree: `4108ca9bb332d11965c30e0c592b310d0858f251`
- Build: Magneticraft 2.8.5, Minecraft 1.12.2, Forge 14.23.5.2847, Java 8, Kotlin 1.3.50, ForgeGradle 2.3.
- Legacy dependencies include Forgelin, ModelLoader, JEI, CraftTweaker/MTLib, Tinkers/Mantle, and BuildCraft.
- Repository metadata identifies GPL-2.0. Preserve the license and upstream attribution; audit individual asset notices before redistribution.
- Review scope so far: repository metadata, README, build.gradle, and recursive file tree. Complete behavior and asset auditing is still required.
- Preserve `1.12` as the upstream reference; do not overwrite or delete it.

## Branch and release policy

| Target | Development branch | Artifact convention | Tag convention |
| --- | --- | --- | --- |
| Minecraft 1.21.1 | `1.21.1` | `Foundations-Magneticraft-1.21.1-<version>.jar` | `mc1.21.1-v<version>` |
| Minecraft 26.3 | `26.3` | `Foundations-Magneticraft-26.3-<version>.jar` | `mc26.3-v<version>` |

Start at `0.0.1a`; use `.R2`, `.R3`, etc. for revisions before the next base alpha. Use the same mod version for each paired release. Every feature or fix must be implemented, reviewed, and tested on both targets before the version is considered complete.

Each pair uses two GitHub Releases, with target-specific JARs, changelogs, source references, checksums, and dependency requirements. Alpha/beta releases are marked prerelease. Do not label a target supported until its artifact passes its own checks. If either target fails, hold the paired publication and report the blocker.

## Target toolchains

- **1.21.1:** NeoForge 21.1.252, Java 21, Gradle 9.2.1, ModDevGradle 2.0.148.
- **26.3:** NeoForge 26.3.0.39-beta, Java 25, Gradle 9.2.1, ModDevGradle 2.0.147. Target-specific capabilities, configuration, rendering and serialization are implemented separately.
- **26.4 preparation:** isolate version-sensitive registration, serialization, networking, capabilities, menus, rendering, and worldgen code. Track official changes when available; do not invent future API requirements or claim 26.4 compatibility.
- Prefer Java for newly rewritten platform code to simplify maintenance. Retain useful Kotlin only with a verified modern runtime/build strategy; no dependency on legacy Forgelin.

## Current implemented subset

Both branches currently contain 96 item/block-item registrations, 71 crafting/smelting recipes, 34 crushing recipes, 16 sluice recipes, four default ore distributions, crushing/sluice/storage/fabricator implementations, scripting support as documented, and paired release automation. The processing expansion adds sluice water-container/item/fluid compatibility, optional JEI/Jade, recipe synchronization and caching, source sounds and server configuration. Full client/server gameplay validation and the remaining electricity/heat/fluid/multiblock/logistics content are still pending. Completed source implementation does not imply an entire milestone's exit checks have passed.

## Milestones - apply to both branches

### 0.0.1a - Source audit and bootable platforms

- [x] Create both branches from the pinned baseline; protect the original source history.
- [ ] Inventory actual registered blocks, items, fluids, recipes, multiblocks, tools, computers, transport, generators, GUIs, assets, and integrations.
- [ ] Build a parity manifest: source ID/path, behavior, dependencies, target implementation, tests, and status for each target.
- [ ] Separate active registered content from abandoned `ignore/test` code; do not count unused prototypes as required gameplay.
- [x] Replace legacy Gradle/Forge setup and metadata on both targets.
- [ ] Establish registration, configuration, logging, client/server separation, and data generation.
- [ ] Build both JARs and start each client and dedicated server.

**Exit:** both targets boot and build; complete inventory is committed. This release is a platform preview, not a playable full port.

### 0.0.2a - Materials and early progression

- [ ] Port registered materials, ores, decorative blocks, fluids, tools, and manual processing.
- [ ] Convert metadata variants into explicit modern registrations/states.
- [ ] Replace OreDictionary use with tags.
- [ ] Port crafting, loot, mining requirements, worldgen, and localization.
- [ ] Implement crushing table, sluice box, storage box and fabricator; include sieve/kiln only if verified in the active registered inventory.
- [ ] Validate survival progression, recipe reloads, drops, placement orientation, and item persistence.

**Exit:** early progression functions on both targets with correct assets and recipes.

### 0.0.3a - Power, heat, and steam

- [ ] Audit the original electricity model and preserve meaningful voltage/current/network behavior.
- [ ] Port generation, storage, conductors, connectors, poles, transformers/converters where registered, heat handling, and steam systems.
- [ ] Provide FE interoperability through explicit adapters without erasing native simulation.
- [ ] Define units, conversion ratios, losses, throughput limits, rounding, and server configuration.
- [ ] Test source  ->  cable  ->  consumer, branching, loops, reverse flow, full storage, simulated transfer, break/reconnect, chunk boundaries, unload/reload, and restart.
- [ ] Enforce conservation and prevent duplication, overflow, and stale connections.
- [ ] Add optional EU/J adapters only for verified target APIs. Treat Create rotation as mechanical power requiring an explicit converter, not an FE synonym.

**Exit:** real measured power reaches consumers; both targets pass conservation and persistence checks. Compatibility is listed per tested mod/version.

### 0.0.4a - Processing and multiblocks

- [ ] Port machine recipe types, serializers, automation sides, inventories, tanks, and block entities.
- [ ] Cover the source's grinder, hydraulic press, refinery, oil heater, pumpjack, combustion chambers, electric furnace, steam boiler/engine/turbine, solar structures, and shelving unit according to the manifest.
- [ ] Validate formation, rotation, controller ownership, ports, blocked outputs, dismantling, chunk transitions, and saved state.
- [ ] Preserve processing yields, resource consumption, timing, and operating conditions; document intentional changes.

**Exit:** every listed processing chain runs end to end on both targets.

### 0.0.5a - Logistics, computers, and automation

- [ ] Port conveyors, inserters, pipes, pneumatic tubes, filters, relays, storage, and related active features.
- [ ] Audit and port the registered computer, robot, floppy/device, and programmable automation features.
- [ ] Validate sided transfer, components on items, redstone controls, congestion, routing, unloaded destinations, and network rebuilding.
- [ ] Bound work per tick; rebuild graphs on topology changes rather than scanning entire networks every tick.

**Exit:** automated factories and active programmable features work on both targets without duplication or forced chunk loading.

### 0.0.6a - Complete visuals, animations, and GUIs

- [ ] Audit and convert upstream textures, models, UVs, blockstates, sound resources, and animations.
- [ ] Replace legacy ModelLoader/OpenGL rendering with each target's supported rendering APIs.
- [ ] Match original machine silhouettes and animation behavior; no missing textures or placeholder models in content declared complete.
- [ ] Verify every placement rotation, collision/selection shape, multiblock render boundary, and inventory model.
- [ ] Port all registered screens and menus: progress, energy, heat, steam, tanks, filters, controls, tooltips, and configuration.
- [ ] Validate server-authoritative menu actions, shift-click, GUI scaling, resizing, and multiplayer synchronization.

**Exit:** visual and GUI checklist passes for all active content on both versions.

### 0.0.7a - Integrations and modpack controls

- [ ] Provide JEI categories, recipe display, catalysts, and transfer where supported.
- [ ] Provide Jade machine, power, fluid, heat, and multiblock information.
- [ ] Provide data-driven recipes plus KubeJS integration for supported target releases.
- [ ] Evaluate modern CraftTweaker, Tinkers, and BuildCraft equivalents against actual available APIs.
- [ ] Keep optional integrations isolated; test with each dependency present and absent.
- [ ] Publish a per-target compatibility matrix. If an integration is unavailable on 26.3, document it explicitly rather than claiming parity.
- [ ] Expose server/modpack configuration for worldgen, machine costs, network throughput, and conversions.

**Exit:** tested integrations and clear target-specific limitations.

### 0.0.8a - Performance and release hardening

- [ ] Test dedicated servers with multiple players; validate packets, permissions, distances, and malformed input.
- [ ] Profile a representative factory on each version; record hardware, world size, tick time, network load, and memory.
- [ ] Address idle ticking, render allocation, graph traversal, packet spam, and chunk unload cleanup.
- [ ] Test saves, reloads, resource/data reloads, and upgrades from earlier port builds.
- [ ] Reconcile the parity manifest; every remaining omission is visible in release notes.

**Exit:** both targets meet recorded performance budgets and pass regression checks.

### 1.0.0 - Full port release

- [ ] All active source content has a passing parity entry or an explicitly approved exception.
- [ ] Full survival playthrough, automation factory, multiplayer, visuals, GUIs, integrations, and restart persistence pass on both targets.
- [ ] Release docs, credits, license, changelog, checksums, and install instructions are complete.
- [ ] Publish the two matching release versions together.

## GitHub automation

1. Branch pushes/PRs run target-specific compilation, appropriate tests, data/resource validation, and packaging.
2. A release workflow takes one mod version and freezes the tested commit SHA for each branch.
3. Build both targets from those SHAs. Require both checks to pass before publication.
4. Produce manifests containing Minecraft/loader/Java versions, mod version, source SHA, dependencies, and SHA-256 checksums.
5. Create both target-specific tags and draft releases; upload only the corresponding artifacts.
6. Publish both drafts after both uploads and checks succeed. Cross-link the release pair.
7. Use least-privilege workflow permissions and repository secrets; no credentials in source.
8. Keep publication idempotent and retryable: never overwrite published artifacts silently. Use a revision for a corrected release.
9. Record failures and partial publication, retain successful artifacts for retry, and verify both releases after publishing.

GitHub cannot publish two releases atomically. The workflow must prepare both drafts before publishing and detect/recover from a partial publication.

## 26.4 transition

- [ ] Monitor official Minecraft/NeoForge changes when development resumes.
- [ ] Maintain a compatibility checklist against 26.3 with affected source paths.
- [ ] Run a separate toolchain/build spike when 26.4 APIs are available.
- [ ] Decide whether to advance the 26.3 line or add a new branch at that time; do not delete 26.3 or promise save compatibility without testing.
- [ ] Repeat the full release and integration checks for 26.4.

## References

- Source build: https://github.com/Shnewbs/Foundation-Magneticraft/blob/1.12/build.gradle
- Source README: https://github.com/Shnewbs/Foundation-Magneticraft/blob/1.12/README.md
- Source tree: https://github.com/Shnewbs/Foundation-Magneticraft/tree/4108ca9bb332d11965c30e0c592b310d0858f251
- NeoForge 1.21.1 setup: https://docs.neoforged.net/docs/1.21.1/gettingstarted/
- Minecraft 26.3: https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3
