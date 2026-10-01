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
    @Override protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        if (!trySaveLootTable(output)) ContainerHelper.saveAllItems(output, items);
        for (int i = 0; i < 9; i++) output.store("ghost_" + i, ItemStack.OPTIONAL_CODEC, ghosts.getItem(i).copyWithCount(1));
    }
    @Override protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(9, ItemStack.EMPTY);
        if (!tryLoadLootTable(input)) ContainerHelper.loadAllItems(input, items);
        for (int i = 0; i < 9; i++) ghosts.setItem(i, input.read("ghost_" + i, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY).copyWithCount(1));
    }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            net.minecraft.world.Containers.dropContents(level, pos, this);
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        super.preRemoveSideEffects(pos, state);
    }
}
