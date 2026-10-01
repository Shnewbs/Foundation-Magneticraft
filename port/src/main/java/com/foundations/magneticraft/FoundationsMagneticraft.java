package com.foundations.magneticraft;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(FoundationsMagneticraft.MOD_ID)
public final class FoundationsMagneticraft {
    public static final String MOD_ID = "magneticraft";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    static {
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

    public FoundationsMagneticraft(IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
        LogUtils.getLogger().info("Foundations Magneticraft: 1.21.1 material bootstrap; machines not yet ported");
    }
}
