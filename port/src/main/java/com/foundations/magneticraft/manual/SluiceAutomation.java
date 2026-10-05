package com.foundations.magneticraft.manual;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Item and fluid views share one journal, so a multi-capability transaction rolls back together. */
public final class SluiceAutomation {
    private final SluiceBoxBlockEntity box;
    public SluiceAutomation(SluiceBoxBlockEntity box) { this.box = box; }
    private final SnapshotJournal<SluiceBoxBlockEntity.AutomationState> journal = new SnapshotJournal<>() {
        @Override protected SluiceBoxBlockEntity.AutomationState createSnapshot() { return box.automationState(); }
        @Override protected void revertToSnapshot(SluiceBoxBlockEntity.AutomationState state) { box.restoreAutomation(state); }
        @Override protected void onRootCommit(SluiceBoxBlockEntity.AutomationState state) { box.flushAutomation(); }
    };
    private final ResourceHandler<ItemResource> items = new ResourceHandler<>() {
        @Override public int size() { return 1; }
        @Override public ItemResource getResource(int index) { return index == 0 ? ItemResource.of(box.input()) : ItemResource.EMPTY; }
        @Override public long getAmountAsLong(int index) { return index == 0 ? box.input().getCount() : 0; }
        @Override public long getCapacityAsLong(int index, ItemResource resource) { return index == 0 ? Math.min(SluiceCycle.CAPACITY, resource.isEmpty() ? 64 : resource.getMaxStackSize()) : 0; }
        @Override public boolean isValid(int index, ItemResource resource) { return index == 0 && !resource.isEmpty() && SluiceRecipes.find(box.getLevel(), resource.toStack()) != null; }
        @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index != 0 || resource.isEmpty() || amount <= 0) return 0;
            ItemStack incoming = resource.toStack(amount);
            int accepted = amount - box.insertInput(incoming, true).getCount();
            if (accepted > 0) {
                journal.updateSnapshots(transaction);
                ItemStack next = box.input();
                if (next.isEmpty()) next = resource.toStack(accepted); else next.grow(accepted);
                box.setAutomation(next, box.waterAmount());
            }
            return accepted;
        }
        @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index != 0 || resource.isEmpty() || amount <= 0 || !resource.matches(box.input())) return 0;
            int extracted = box.extractInput(amount, true).getCount();
            if (extracted > 0) {
                journal.updateSnapshots(transaction);
                ItemStack next = box.input(); next.shrink(extracted);
                box.setAutomation(next, box.waterAmount());
            }
            return extracted;
        }
    };
    private final ResourceHandler<FluidResource> water = new ResourceHandler<>() {
        @Override public int size() { return 1; }
        @Override public FluidResource getResource(int index) { return index == 0 && box.waterAmount() > 0 ? FluidResource.of(Fluids.WATER) : FluidResource.EMPTY; }
        @Override public long getAmountAsLong(int index) { return index == 0 ? box.waterAmount() : 0; }
        @Override public long getCapacityAsLong(int index, FluidResource resource) { return index == 0 && (resource.isEmpty() || resource.getFluid() == Fluids.WATER) ? WaterBudget.CAPACITY : 0; }
        @Override public boolean isValid(int index, FluidResource resource) { return index == 0 && !resource.isEmpty() && resource.getFluid() == Fluids.WATER; }
        @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (!isValid(index, resource) || amount <= 0) return 0;
            int accepted = box.insertWater(amount, true);
            if (accepted > 0) { journal.updateSnapshots(transaction); box.setAutomation(box.input(), box.waterAmount() + accepted); }
            return accepted;
        }
        @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) { return 0; }
    };
    public ResourceHandler<ItemResource> items() { return items; }
    public ResourceHandler<FluidResource> water() { return water; }
}
