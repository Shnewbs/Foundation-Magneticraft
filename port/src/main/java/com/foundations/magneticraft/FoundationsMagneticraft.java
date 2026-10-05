package com.foundations.magneticraft;

import com.mojang.logging.LogUtils;
import com.foundations.magneticraft.manual.FabricatorBlock;
import com.foundations.magneticraft.manual.FabricatorBlockEntity;
import com.foundations.magneticraft.manual.FabricatorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import com.foundations.magneticraft.manual.CrushingTableBlock;
import com.foundations.magneticraft.manual.BoxBlock;
import com.foundations.magneticraft.manual.BoxBlockEntity;
import com.foundations.magneticraft.integration.RecipeOverrides;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import com.foundations.magneticraft.manual.SluiceBoxBlock;
import com.foundations.magneticraft.manual.SluiceBoxBlockEntity;
import com.foundations.magneticraft.manual.CrushingTableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.PushReaction;
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
    public static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent, net.minecraft.sounds.SoundEvent> CRUSHING_HIT = sound("crushing_hit");
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent, net.minecraft.sounds.SoundEvent> CRUSHING_FINAL = sound("crushing_final");
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent, net.minecraft.sounds.SoundEvent> WATER_FLOW = sound("water_flow");
    public static final DeferredHolder<net.minecraft.sounds.SoundEvent, net.minecraft.sounds.SoundEvent> WATER_FLOW_END = sound("water_flow_end");
    private static DeferredHolder<net.minecraft.sounds.SoundEvent, net.minecraft.sounds.SoundEvent> sound(String path) {
        return SOUNDS.register(path, () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, path)));
    }
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredBlock<Block> COPPER_ORE = BLOCKS.registerSimpleBlock("copper_ore", BlockBehaviour.Properties.of().strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> COPPER_BLOCK = BLOCKS.registerSimpleBlock("copper_block", BlockBehaviour.Properties.of().strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> LEAD_ORE = BLOCKS.registerSimpleBlock("lead_ore", BlockBehaviour.Properties.of().strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> LEAD_BLOCK = BLOCKS.registerSimpleBlock("lead_block", BlockBehaviour.Properties.of().strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> COBALT_ORE = BLOCKS.registerSimpleBlock("cobalt_ore", BlockBehaviour.Properties.of().strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> COBALT_BLOCK = BLOCKS.registerSimpleBlock("cobalt_block", BlockBehaviour.Properties.of().strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> TUNGSTEN_ORE = BLOCKS.registerSimpleBlock("tungsten_ore", BlockBehaviour.Properties.of().strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> TUNGSTEN_BLOCK = BLOCKS.registerSimpleBlock("tungsten_block", BlockBehaviour.Properties.of().strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> PYRITE_ORE = BLOCKS.registerSimpleBlock("pyrite_ore", BlockBehaviour.Properties.of().strength(1.5F, 10.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> PYRITE_BLOCK = BLOCKS.registerSimpleBlock("pyrite_block", BlockBehaviour.Properties.of().strength(1.5F, 10.0F).requiresCorrectToolForDrops());

    public static final DeferredBlock<CrushingTableBlock> CRUSHING_TABLE = BLOCKS.register("crushing_table", () -> new CrushingTableBlock(BlockBehaviour.Properties.of().strength(1.5F).noOcclusion().pushReaction(PushReaction.BLOCK)));
    public static final DeferredBlock<SluiceBoxBlock> SLUICE_BOX = BLOCKS.register("sluice_box", () -> new SluiceBoxBlock(BlockBehaviour.Properties.of().strength(1.5F).noOcclusion().pushReaction(PushReaction.BLOCK)));
    public static final DeferredBlock<BoxBlock> BOX = BLOCKS.register("box", () -> new BoxBlock(BlockBehaviour.Properties.of().strength(1.5F).sound(net.minecraft.world.level.block.SoundType.WOOD).pushReaction(PushReaction.BLOCK)));
    public static final DeferredBlock<FabricatorBlock> FABRICATOR = BLOCKS.register("fabricator", () -> new FabricatorBlock(BlockBehaviour.Properties.of().strength(1.5F).sound(net.minecraft.world.level.block.SoundType.WOOD).pushReaction(PushReaction.BLOCK)));
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrushingTableBlockEntity>> CRUSHING_TABLE_ENTITY =
            BLOCK_ENTITIES.register("crushing_table", () -> BlockEntityType.Builder.of(CrushingTableBlockEntity::new, CRUSHING_TABLE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluiceBoxBlockEntity>> SLUICE_BOX_ENTITY =
            BLOCK_ENTITIES.register("sluice_box", () -> BlockEntityType.Builder.of(SluiceBoxBlockEntity::new, SLUICE_BOX.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoxBlockEntity>> BOX_ENTITY =
            BLOCK_ENTITIES.register("box", () -> BlockEntityType.Builder.of(BoxBlockEntity::new, BOX.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FabricatorBlockEntity>> FABRICATOR_ENTITY =
            BLOCK_ENTITIES.register("fabricator", () -> BlockEntityType.Builder.of(FabricatorBlockEntity::new, FABRICATOR.get()).build(null));
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<FabricatorMenu>> FABRICATOR_MENU =
            MENUS.register("fabricator", () -> new MenuType<>(FabricatorMenu::new, FeatureFlags.VANILLA_SET));

    static {
        ITEMS.registerSimpleBlockItem("fabricator", FABRICATOR);
        ITEMS.registerSimpleBlockItem("box", BOX);
        ITEMS.registerSimpleBlockItem("sluice_box", SLUICE_BOX);
        ITEMS.registerSimpleItem("mesh", new Item.Properties());
        ITEMS.registerSimpleBlockItem("crushing_table", CRUSHING_TABLE);
        ITEMS.registerSimpleItem("stone_hammer", new Item.Properties().durability(130));
        ITEMS.registerSimpleItem("iron_hammer", new Item.Properties().durability(250));
        ITEMS.registerSimpleItem("steel_hammer", new Item.Properties().durability(750));
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
        ITEMS.registerSimpleItem("sulfur", new Item.Properties());

        ITEMS.registerSimpleItem("iron_light_plate", new Item.Properties());
        ITEMS.registerSimpleItem("iron_heavy_plate", new Item.Properties());
        ITEMS.registerSimpleItem("iron_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("iron_dust", new Item.Properties());
        ITEMS.registerSimpleItem("iron_rocky_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("gold_light_plate", new Item.Properties());
        ITEMS.registerSimpleItem("gold_heavy_plate", new Item.Properties());
        ITEMS.registerSimpleItem("gold_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("gold_dust", new Item.Properties());
        ITEMS.registerSimpleItem("gold_rocky_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("copper_ingot", new Item.Properties());
        ITEMS.registerSimpleItem("copper_nugget", new Item.Properties());
        ITEMS.registerSimpleItem("copper_light_plate", new Item.Properties());
        ITEMS.registerSimpleItem("copper_heavy_plate", new Item.Properties());
        ITEMS.registerSimpleItem("copper_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("copper_dust", new Item.Properties());
        ITEMS.registerSimpleItem("copper_rocky_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("lead_ingot", new Item.Properties());
        ITEMS.registerSimpleItem("lead_nugget", new Item.Properties());
        ITEMS.registerSimpleItem("lead_light_plate", new Item.Properties());
        ITEMS.registerSimpleItem("lead_heavy_plate", new Item.Properties());
        ITEMS.registerSimpleItem("lead_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("lead_dust", new Item.Properties());
        ITEMS.registerSimpleItem("lead_rocky_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("cobalt_ingot", new Item.Properties());
        ITEMS.registerSimpleItem("cobalt_nugget", new Item.Properties());
        ITEMS.registerSimpleItem("cobalt_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("cobalt_dust", new Item.Properties());
        ITEMS.registerSimpleItem("cobalt_rocky_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("tungsten_ingot", new Item.Properties());
        ITEMS.registerSimpleItem("tungsten_nugget", new Item.Properties());
        ITEMS.registerSimpleItem("tungsten_light_plate", new Item.Properties());
        ITEMS.registerSimpleItem("tungsten_heavy_plate", new Item.Properties());
        ITEMS.registerSimpleItem("tungsten_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("tungsten_dust", new Item.Properties());
        ITEMS.registerSimpleItem("tungsten_rocky_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("steel_ingot", new Item.Properties());
        ITEMS.registerSimpleItem("steel_nugget", new Item.Properties());
        ITEMS.registerSimpleItem("steel_light_plate", new Item.Properties());
        ITEMS.registerSimpleItem("steel_heavy_plate", new Item.Properties());
        ITEMS.registerSimpleItem("steel_dust", new Item.Properties());
        ITEMS.registerSimpleItem("aluminium_ingot", new Item.Properties());
        ITEMS.registerSimpleItem("aluminium_nugget", new Item.Properties());
        ITEMS.registerSimpleItem("aluminium_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("aluminium_dust", new Item.Properties());
        ITEMS.registerSimpleItem("aluminium_rocky_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("galena_rocky_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("mithril_ingot", new Item.Properties());
        ITEMS.registerSimpleItem("mithril_nugget", new Item.Properties());
        ITEMS.registerSimpleItem("mithril_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("mithril_dust", new Item.Properties());
        ITEMS.registerSimpleItem("mithril_rocky_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("nickel_ingot", new Item.Properties());
        ITEMS.registerSimpleItem("nickel_nugget", new Item.Properties());
        ITEMS.registerSimpleItem("nickel_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("nickel_dust", new Item.Properties());
        ITEMS.registerSimpleItem("nickel_rocky_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("osmium_ingot", new Item.Properties());
        ITEMS.registerSimpleItem("osmium_nugget", new Item.Properties());
        ITEMS.registerSimpleItem("osmium_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("osmium_dust", new Item.Properties());
        ITEMS.registerSimpleItem("osmium_rocky_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("silver_ingot", new Item.Properties());
        ITEMS.registerSimpleItem("silver_nugget", new Item.Properties());
        ITEMS.registerSimpleItem("silver_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("silver_dust", new Item.Properties());
        ITEMS.registerSimpleItem("silver_rocky_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("tin_ingot", new Item.Properties());
        ITEMS.registerSimpleItem("tin_nugget", new Item.Properties());
        ITEMS.registerSimpleItem("tin_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("tin_dust", new Item.Properties());
        ITEMS.registerSimpleItem("tin_rocky_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("zinc_ingot", new Item.Properties());
        ITEMS.registerSimpleItem("zinc_nugget", new Item.Properties());
        ITEMS.registerSimpleItem("zinc_chunk", new Item.Properties());
        ITEMS.registerSimpleItem("zinc_dust", new Item.Properties());
        ITEMS.registerSimpleItem("zinc_rocky_chunk", new Item.Properties());
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIALS =
            TABS.register("materials", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.magneticraft.materials"))
                    .icon(() -> ITEMS.getEntries().iterator().next().get().getDefaultInstance())
                    .displayItems((parameters, output) ->
                            ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build());

    public FoundationsMagneticraft(IEventBus modBus, net.neoforged.fml.ModContainer container) {
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, com.foundations.magneticraft.manual.ProcessingConfig.SPEC);
        SOUNDS.register(modBus);
        com.foundations.magneticraft.integration.ProcessingRecipeSync.register(modBus);
        modBus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) ->
            event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                BOX_ENTITY.get(), (box, side) -> new net.neoforged.neoforge.items.wrapper.InvWrapper(box)));
        modBus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) ->
            event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                FABRICATOR_ENTITY.get(), (box, side) -> new net.neoforged.neoforge.items.wrapper.InvWrapper(box)));
        modBus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                SLUICE_BOX_ENTITY.get(), (box, side) -> box.automation().items());
            event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                SLUICE_BOX_ENTITY.get(), (box, side) -> box.automation().water());
        });
        MENUS.register(modBus);
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
            RecipeOverrides.clearAll();
            com.foundations.magneticraft.manual.CrushingRecipes.clearCache();
            com.foundations.magneticraft.manual.SluiceRecipes.clearCache();
        });
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        LogUtils.getLogger().info("Foundations Magneticraft: 1.21.1 ore progression and manual crushing table");
    }
}
