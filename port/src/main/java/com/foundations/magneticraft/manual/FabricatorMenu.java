package com.foundations.magneticraft.manual;

import com.foundations.magneticraft.FoundationsMagneticraft;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class FabricatorMenu extends AbstractContainerMenu {
    private final FabricatorBlockEntity box;
    private final Container ghosts;
    private final SimpleContainer preview = new SimpleContainer(1);
    private final SimpleContainerData data = new SimpleContainerData(2);
    private int refreshTicks;
    public FabricatorMenu(int id, Inventory player) { this(id, player, null); }
    public FabricatorMenu(int id, Inventory player, FabricatorBlockEntity box) {
        super(FoundationsMagneticraft.FABRICATOR_MENU.get(), id);
        this.box = box;
        ghosts = box == null ? new SimpleContainer(9) : box.ghosts();
        Container storage = box == null ? new SimpleContainer(9) : box;
        for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++)
            addSlot(readOnly(ghosts, row * 3 + col, 18 + col * 18, 30 + row * 18));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++)
            addSlot(new Slot(storage, row * 3 + col, 112 + col * 18, 30 + row * 18));
        addSlot(readOnly(preview, 0, 84, 48));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(player, col + row * 9 + 9, 8 + col * 18, 138 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(player, col, 8 + col * 18, 196));
        addDataSlots(data);
        refreshPreview();
    }
    private static Slot readOnly(Container container, int index, int x, int y) {
        return new Slot(container, index, x, y) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return false; }
        };
    }
    public boolean ready() { return data.get(0) == 1; }
    public int foundMask() { return data.get(1); }
    private void refreshPreview() {
        if (box == null) return;
        var state = FabricatorCrafting.preview(box);
        preview.setItem(0, state.output());
        data.set(0, state.ready() ? 1 : 0);
        data.set(1, state.foundMask());
    }
    @Override public void broadcastChanges() {
        if (++refreshTicks >= 10) { refreshTicks = 0; refreshPreview(); }
        super.broadcastChanges();
    }
    @Override public void clicked(int slot, int button, ContainerInput type, Player player) {
        if (!stillValid(player)) return;
        if (slot >= 0 && slot < 9) {
            if (type == ContainerInput.PICKUP && (button == 0 || button == 1)) {
                ItemStack carried = getCarried();
                ghosts.setItem(slot, button == 1 || carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
                refreshPreview();
                broadcastChanges();
            }
            return;
        }
        if (slot == 18) return;
        super.clicked(slot, button, type, player);
    }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (box == null || !stillValid(player)) return false;
        if (button == 0) box.requestCraft();
        else if (button == 1) for (int slot = 0; slot < 9; slot++) ghosts.setItem(slot, ItemStack.EMPTY);
        else return false;
        refreshPreview();
        broadcastChanges();
        return true;
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 9 || index == 18 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 18 ? !moveItemStackTo(stack, 19, 55, true) : !moveItemStackTo(stack, 9, 18, false))
            return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        refreshPreview();
        return original;
    }
    @Override public boolean stillValid(Player player) { return box == null || box.stillValid(player); }
}
