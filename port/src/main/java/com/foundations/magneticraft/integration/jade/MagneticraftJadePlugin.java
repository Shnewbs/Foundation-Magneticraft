package com.foundations.magneticraft.integration.jade;

import com.foundations.magneticraft.manual.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public final class MagneticraftJadePlugin implements IWailaPlugin {
    private static final Provider PROVIDER = new Provider();
    private static final Class<?>[] BLOCKS = {CrushingTableBlock.class, SluiceBoxBlock.class, FabricatorBlock.class, BoxBlock.class};
    @Override public void register(IWailaCommonRegistration registration) {
        for (var block : BLOCKS) registration.registerBlockDataProvider(PROVIDER, block);
    }
    @Override public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(PROVIDER, CrushingTableBlock.class);
        registration.registerBlockComponent(PROVIDER, SluiceBoxBlock.class);
        registration.registerBlockComponent(PROVIDER, FabricatorBlock.class);
        registration.registerBlockComponent(PROVIDER, BoxBlock.class);
    }
    private static final class Provider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        @Override public Identifier getUid() { return Identifier.fromNamespaceAndPath("magneticraft", "manual_processing"); }
        @Override public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            BlockEntity entity = accessor.getBlockEntity();
            if (accessor.getBlockState().getBlock() instanceof SluiceBoxBlock) {
                var pos = SluiceBoxBlock.mainPos(accessor.getBlockState(), accessor.getPosition());
                if (accessor.getLevel().hasChunkAt(pos)) entity = accessor.getLevel().getBlockEntity(pos);
            }
            if (entity instanceof SluiceBoxBlockEntity box) {
                data.putString("magneticraft_kind", "sluice");
                data.putInt("magneticraft_count", box.input().getCount());
                data.putInt("magneticraft_remaining", box.remainingTicks());
                data.putInt("magneticraft_water", box.waterAmount());
            } else if (entity instanceof CrushingTableBlockEntity table) {
                data.putString("magneticraft_kind", "crushing");
                data.putInt("magneticraft_count", table.input().getCount());
                data.putInt("magneticraft_progress", table.damage());
            } else if (entity instanceof net.minecraft.world.Container container) {
                data.putString("magneticraft_kind", entity instanceof FabricatorBlockEntity ? "fabricator" : "box");
                int used = 0;
                for (int slot = 0; slot < container.getContainerSize(); slot++) if (!container.getItem(slot).isEmpty()) used++;
                data.putInt("magneticraft_used", used);
                data.putInt("magneticraft_slots", container.getContainerSize());
                if (entity instanceof FabricatorBlockEntity box) {
                    int pattern = 0;
                    for (int slot = 0; slot < 9; slot++) if (!box.ghosts().getItem(slot).isEmpty()) pattern++;
                    data.putInt("magneticraft_pattern", pattern);
                }
            }
        }
        @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var data = accessor.getServerData();
            String kind = data.getString("magneticraft_kind").orElse("");
            if (kind.equals("sluice")) {
                tooltip.add(Component.translatable("jade.magneticraft.sluice_input", data.getInt("magneticraft_count").orElse(0)));
                tooltip.add(Component.translatable("jade.magneticraft.sluice_progress", data.getInt("magneticraft_remaining").orElse(0)));
                tooltip.add(Component.translatable("jade.magneticraft.water", data.getInt("magneticraft_water").orElse(0)));
            } else if (kind.equals("crushing")) {
                tooltip.add(Component.translatable("jade.magneticraft.crushing", data.getInt("magneticraft_progress").orElse(0), data.getInt("magneticraft_count").orElse(0)));
            } else if (kind.equals("fabricator") || kind.equals("box")) {
                tooltip.add(Component.translatable("jade.magneticraft.storage", data.getInt("magneticraft_used").orElse(0), data.getInt("magneticraft_slots").orElse(0)));
                if (kind.equals("fabricator")) tooltip.add(Component.translatable("jade.magneticraft.pattern", data.getInt("magneticraft_pattern").orElse(0)));
            }
        }
    }
}
