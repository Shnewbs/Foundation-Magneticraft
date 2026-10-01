package com.foundations.magneticraft.manual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public final class FabricatorInventory {
    public record Source(IItemHandler handler, int slot, ItemStack stack, int available) {}
    public record Remainder(Source source, ItemStack stack) {}
    private FabricatorInventory() {}
    public static List<Source> sources(FabricatorBlockEntity box) {
        List<Source> result = new ArrayList<>();
        var seen = Collections.newSetFromMap(new IdentityHashMap<IItemHandler, Boolean>());
        List<IItemHandler> handlers = new ArrayList<>();
        handlers.add(new InvWrapper(box));
        var level = box.getLevel();
        for (Direction direction : Direction.values()) {
            var pos = box.getBlockPos().relative(direction);
            if (!level.hasChunkAt(pos)) continue;
            var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction.getOpposite());
            if (handler != null) handlers.add(handler);
        }
        for (var handler : handlers) {
            if (!seen.add(handler)) continue;
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (stack.isEmpty()) continue;
                ItemStack simulated = handler.extractItem(slot, Math.min(9, stack.getCount()), true);
                if (simulated.isEmpty() || !ItemStack.isSameItemSameComponents(stack, simulated)) continue;
                result.add(new Source(handler, slot, stack.copyWithCount(1), Math.min(9, simulated.getCount())));
            }
        }
        return result;
    }
    public static boolean commit(FabricatorBlockEntity box, List<Source> selected, List<Remainder> remainders, ItemStack output) {
        var counts = new LinkedHashMap<Source, Integer>();
        for (Source source : selected) counts.merge(source, 1, Integer::sum);
        for (var entry : counts.entrySet()) {
            var source = entry.getKey();
            var stack = source.handler().extractItem(source.slot(), entry.getValue(), true);
            if (stack.getCount() != entry.getValue() || !ItemStack.isSameItemSameComponents(stack, source.stack())) return false;
        }
        List<Remainder> extracted = new ArrayList<>();
        for (Source source : selected) {
            ItemStack stack = source.handler().extractItem(source.slot(), 1, false);
            if (!stack.isEmpty()) extracted.add(new Remainder(source, stack.copy()));
            if (stack.getCount() != 1 || !ItemStack.isSameItemSameComponents(stack, source.stack())) {
                // Return only actual extracted stacks; never manufacture a replacement.
                for (Remainder paid : extracted) returnToSource(box, paid);
                return false;
            }
        }
        for (Remainder remainder : remainders) returnToSource(box, remainder);
        storeOrDrop(box, output.copy());
        box.setChanged();
        return true;
    }
    private static void returnToSource(FabricatorBlockEntity box, Remainder remainder) {
        ItemStack rest = remainder.source().handler().insertItem(remainder.source().slot(), remainder.stack().copy(), false);
        for (int slot = 0; slot < remainder.source().handler().getSlots() && !rest.isEmpty(); slot++) {
            rest = remainder.source().handler().insertItem(slot, rest, false);
        }
        storeOrDrop(box, rest);
    }
    private static void storeOrDrop(FabricatorBlockEntity box, ItemStack rest) {
        var buffer = new InvWrapper(box);
        for (int slot = 0; slot < buffer.getSlots() && !rest.isEmpty(); slot++) rest = buffer.insertItem(slot, rest, false);
        if (!rest.isEmpty()) {
            var pos = box.getBlockPos();
            Containers.dropItemStack(box.getLevel(), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, rest);
        }
    }
}
