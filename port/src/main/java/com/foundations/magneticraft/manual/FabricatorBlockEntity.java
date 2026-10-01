package com.foundations.magneticraft.manual;

import com.foundations.magneticraft.FoundationsMagneticraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class FabricatorBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private final net.minecraft.world.SimpleContainer ghosts = new net.minecraft.world.SimpleContainer(9) {
        @Override public void setChanged() { FabricatorBlockEntity.this.setChanged(); }
    };
    private long lastRequest = Long.MIN_VALUE;
    public net.minecraft.world.Container ghosts() { return ghosts; }
    public boolean requestCraft() {
        if (level == null || level.isClientSide() || lastRequest == level.getGameTime()) return false;
        lastRequest = level.getGameTime();
        return FabricatorCrafting.craft(this);
    }
    public FabricatorBlockEntity(BlockPos pos, BlockState state) { super(FoundationsMagneticraft.FABRICATOR_ENTITY.get(), pos, state); }
    @Override public int getContainerSize() { return 9; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override protected Component getDefaultName() { return Component.translatable("block.magneticraft.fabricator"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new FabricatorMenu(id, inventory, this); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
        for (int i = 0; i < 9; i++) if (!ghosts.getItem(i).isEmpty()) tag.put("ghost_" + i, ghosts.getItem(i).copyWithCount(1).save(registries));
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(9, ItemStack.EMPTY);
        if (!tryLoadLootTable(tag)) ContainerHelper.loadAllItems(tag, items, registries);
        for (int i = 0; i < 9; i++) ghosts.setItem(i, ItemStack.parseOptional(registries, tag.getCompound("ghost_" + i)).copyWithCount(1));
    }
}
