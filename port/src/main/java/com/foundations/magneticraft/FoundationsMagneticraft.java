package com.foundations.magneticraft;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(FoundationsMagneticraft.MOD_ID)
public final class FoundationsMagneticraft {
    public static final String MOD_ID = "magneticraft";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredBlock<Block> COPPER_ORE = BLOCKS.registerSimpleBlock("copper_ore", properties -> properties.strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> COPPER_BLOCK = BLOCKS.registerSimpleBlock("copper_block", properties -> properties.strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> LEAD_ORE = BLOCKS.registerSimpleBlock("lead_ore", properties -> properties.strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> LEAD_BLOCK = BLOCKS.registerSimpleBlock("lead_block", properties -> properties.strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> COBALT_ORE = BLOCKS.registerSimpleBlock("cobalt_ore", properties -> properties.strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> COBALT_BLOCK = BLOCKS.registerSimpleBlock("cobalt_block", properties -> properties.strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> TUNGSTEN_ORE = BLOCKS.registerSimpleBlock("tungsten_ore", properties -> properties.strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> TUNGSTEN_BLOCK = BLOCKS.registerSimpleBlock("tungsten_block", properties -> properties.strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> PYRITE_ORE = BLOCKS.registerSimpleBlock("pyrite_ore", properties -> properties.strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> PYRITE_BLOCK = BLOCKS.registerSimpleBlock("pyrite_block", properties -> properties.strength(1.5F, 10.0F).requiresCorrectToolForDrops());

    static {
        ITEMS.registerSimpleBlockItem("copper_ore", COPPER_ORE);
        ITEMS.registerSimpleBlockItem("copper_block", COPPER_BLOCK);
        ITEMS.registerSimpleBlockItem("lead_ore", LEAD_ORE);
        ITEMS.registerSimpleBlockItem("lead_block", LEAD_BLOCK);
        ITEMS.registerSimpleBlockItem("cobalt_ore", COBALT_ORE);
        ITEMS.registerSimpleBlockItem("cobalt_block", COBALT_BLOCK);
        ITEMS.registerSimpleBlockItem("tungsten_ore", TUNGSTEN_ORE);
        ITEMS.registerSimpleBlockItem("tungsten_block", TUNGSTEN_BLOCK);
        ITEMS.registerSimpleBlockItem("pyrite_ore", PYRITE_ORE);
        ITEMS.registerSimpleBlockItem("pyrite_block", PYRITE_BLOCK);
        ITEMS.registerSimpleItem("sulfur", properties -> properties);

        ITEMS.registerSimpleItem("iron_light_plate", properties -> properties);
        ITEMS.registerSimpleItem("iron_heavy_plate", properties -> properties);
        ITEMS.registerSimpleItem("iron_chunk", properties -> properties);
        ITEMS.registerSimpleItem("iron_dust", properties -> properties);
        ITEMS.registerSimpleItem("iron_rocky_chunk", properties -> properties);
        ITEMS.registerSimpleItem("gold_light_plate", properties -> properties);
        ITEMS.registerSimpleItem("gold_heavy_plate", properties -> properties);
        ITEMS.registerSimpleItem("gold_chunk", properties -> properties);
        ITEMS.registerSimpleItem("gold_dust", properties -> properties);
        ITEMS.registerSimpleItem("gold_rocky_chunk", properties -> properties);
        ITEMS.registerSimpleItem("copper_ingot", properties -> properties);
        ITEMS.registerSimpleItem("copper_nugget", properties -> properties);
        ITEMS.registerSimpleItem("copper_light_plate", properties -> properties);
        ITEMS.registerSimpleItem("copper_heavy_plate", properties -> properties);
        ITEMS.registerSimpleItem("copper_chunk", properties -> properties);
        ITEMS.registerSimpleItem("copper_dust", properties -> properties);
        ITEMS.registerSimpleItem("copper_rocky_chunk", properties -> properties);
        ITEMS.registerSimpleItem("lead_ingot", properties -> properties);
        ITEMS.registerSimpleItem("lead_nugget", properties -> properties);
        ITEMS.registerSimpleItem("lead_light_plate", properties -> properties);
        ITEMS.registerSimpleItem("lead_heavy_plate", properties -> properties);
        ITEMS.registerSimpleItem("lead_chunk", properties -> properties);
        ITEMS.registerSimpleItem("lead_dust", properties -> properties);
        ITEMS.registerSimpleItem("lead_rocky_chunk", properties -> properties);
        ITEMS.registerSimpleItem("cobalt_ingot", properties -> properties);
        ITEMS.registerSimpleItem("cobalt_nugget", properties -> properties);
        ITEMS.registerSimpleItem("cobalt_chunk", properties -> properties);
        ITEMS.registerSimpleItem("cobalt_dust", properties -> properties);
        ITEMS.registerSimpleItem("cobalt_rocky_chunk", properties -> properties);
        ITEMS.registerSimpleItem("tungsten_ingot", properties -> properties);
        ITEMS.registerSimpleItem("tungsten_nugget", properties -> properties);
        ITEMS.registerSimpleItem("tungsten_light_plate", properties -> properties);
        ITEMS.registerSimpleItem("tungsten_heavy_plate", properties -> properties);
        ITEMS.registerSimpleItem("tungsten_chunk", properties -> properties);
        ITEMS.registerSimpleItem("tungsten_dust", properties -> properties);
        ITEMS.registerSimpleItem("tungsten_rocky_chunk", properties -> properties);
        ITEMS.registerSimpleItem("steel_ingot", properties -> properties);
        ITEMS.registerSimpleItem("steel_nugget", properties -> properties);
        ITEMS.registerSimpleItem("steel_light_plate", properties -> properties);
        ITEMS.registerSimpleItem("steel_heavy_plate", properties -> properties);
        ITEMS.registerSimpleItem("steel_dust", properties -> properties);
        ITEMS.registerSimpleItem("aluminium_ingot", properties -> properties);
        ITEMS.registerSimpleItem("aluminium_nugget", properties -> properties);
        ITEMS.registerSimpleItem("aluminium_chunk", properties -> properties);
        ITEMS.registerSimpleItem("aluminium_dust", properties -> properties);
        ITEMS.registerSimpleItem("aluminium_rocky_chunk", properties -> properties);
        ITEMS.registerSimpleItem("galena_rocky_chunk", properties -> properties);
        ITEMS.registerSimpleItem("mithril_ingot", properties -> properties);
        ITEMS.registerSimpleItem("mithril_nugget", properties -> properties);
        ITEMS.registerSimpleItem("mithril_chunk", properties -> properties);
        ITEMS.registerSimpleItem("mithril_dust", properties -> properties);
        ITEMS.registerSimpleItem("mithril_rocky_chunk", properties -> properties);
        ITEMS.registerSimpleItem("nickel_ingot", properties -> properties);
        ITEMS.registerSimpleItem("nickel_nugget", properties -> properties);
        ITEMS.registerSimpleItem("nickel_chunk", properties -> properties);
        ITEMS.registerSimpleItem("nickel_dust", properties -> properties);
        ITEMS.registerSimpleItem("nickel_rocky_chunk", properties -> properties);
        ITEMS.registerSimpleItem("osmium_ingot", properties -> properties);
        ITEMS.registerSimpleItem("osmium_nugget", properties -> properties);
        ITEMS.registerSimpleItem("osmium_chunk", properties -> properties);
        ITEMS.registerSimpleItem("osmium_dust", properties -> properties);
        ITEMS.registerSimpleItem("osmium_rocky_chunk", properties -> properties);
        ITEMS.registerSimpleItem("silver_ingot", properties -> properties);
        ITEMS.registerSimpleItem("silver_nugget", properties -> properties);
        ITEMS.registerSimpleItem("silver_chunk", properties -> properties);
        ITEMS.registerSimpleItem("silver_dust", properties -> properties);
        ITEMS.registerSimpleItem("silver_rocky_chunk", properties -> properties);
        ITEMS.registerSimpleItem("tin_ingot", properties -> properties);
        ITEMS.registerSimpleItem("tin_nugget", properties -> properties);
        ITEMS.registerSimpleItem("tin_chunk", properties -> properties);
        ITEMS.registerSimpleItem("tin_dust", properties -> properties);
        ITEMS.registerSimpleItem("tin_rocky_chunk", properties -> properties);
        ITEMS.registerSimpleItem("zinc_ingot", properties -> properties);
        ITEMS.registerSimpleItem("zinc_nugget", properties -> properties);
        ITEMS.registerSimpleItem("zinc_chunk", properties -> properties);
        ITEMS.registerSimpleItem("zinc_dust", properties -> properties);
        ITEMS.registerSimpleItem("zinc_rocky_chunk", properties -> properties);
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIALS =
            TABS.register("materials", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.magneticraft.materials"))
                    .icon(() -> ITEMS.getEntries().iterator().next().get().getDefaultInstance())
                    .displayItems((parameters, output) ->
                            ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build());

    public FoundationsMagneticraft(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);
        LogUtils.getLogger().info("Foundations Magneticraft: 26.3 ore and material progression; machines not yet ported");
    }
}
