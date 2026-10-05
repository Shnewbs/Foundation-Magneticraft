package com.foundations.magneticraft.manual;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** Automation accesses only real input and a bounded, water-only buffer. */
public final class SluiceAutomation {
    private final SluiceBoxBlockEntity box;
    public SluiceAutomation(SluiceBoxBlockEntity box) { this.box = box; }
    private final IItemHandler items = new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { return slot == 0 ? box.input() : ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return slot == 0 ? box.insertInput(stack, simulate) : stack.copy(); }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return slot == 0 ? box.extractInput(amount, simulate) : ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return slot == 0 ? SluiceCycle.CAPACITY : 0; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot == 0 && !stack.isEmpty() && SluiceRecipes.find(box.getLevel(), stack) != null; }
    };
    private final IFluidHandler water = new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) { return tank == 0 ? new FluidStack(Fluids.WATER, box.waterAmount()) : FluidStack.EMPTY; }
        @Override public int getTankCapacity(int tank) { return tank == 0 ? WaterBudget.CAPACITY : 0; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return tank == 0 && stack.is(Fluids.WATER); }
        @Override public int fill(FluidStack stack, FluidAction action) { return stack.is(Fluids.WATER) ? box.insertWater(stack.getAmount(), action.simulate()) : 0; }
        @Override public FluidStack drain(FluidStack stack, FluidAction action) { return FluidStack.EMPTY; }
        @Override public FluidStack drain(int amount, FluidAction action) { return FluidStack.EMPTY; }
    };
    public IItemHandler items() { return items; }
    public IFluidHandler water() { return water; }
}
