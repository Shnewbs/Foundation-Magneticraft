package com.foundations.magneticraft.manual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class FabricatorInventory {
    public record Source(ResourceHandler<ItemResource> handler, int slot, ItemStack stack, int available) {}
    public record Remainder(Source source, ItemStack stack) {}
    private FabricatorInventory() {}
    public static List<Source> sources(FabricatorBlockEntity box) {
        List<Source> result = new ArrayList<>();
        var seen = Collections.newSetFromMap(new IdentityHashMap<ResourceHandler<ItemResource>, Boolean>());
        List<ResourceHandler<ItemResource>> handlers = new ArrayList<>();
        handlers.add(VanillaContainerWrapper.of(box));
        var level = box.getLevel();
        for (Direction direction : Direction.values()) {
            var pos = box.getBlockPos().relative(direction);
            if (!level.hasChunkAt(pos)) continue;
            var handler = level.getCapability(Capabilities.Item.BLOCK, pos, direction.getOpposite());
            if (handler != null) handlers.add(handler);
        }
        for (var handler : handlers) {
            if (!seen.add(handler)) continue;
            for (int slot = 0; slot < handler.size(); slot++) {
                var resource = handler.getResource(slot);
                if (resource.isEmpty()) continue;
                int available;
                try (var simulation = Transaction.openRoot()) {
                    available = handler.extract(slot, resource, Math.min(9, handler.getAmountAsInt(slot)), simulation);
                }
                if (available > 0) result.add(new Source(handler, slot, resource.toStack(), available));
            }
        }
        return result;
    }
    public static boolean commit(FabricatorBlockEntity box, List<Source> selected, List<Remainder> remainders, ItemStack output) {
        List<ItemStack> overflow = new ArrayList<>();
        var buffer = VanillaContainerWrapper.of(box);
        try (var transaction = Transaction.openRoot()) {
            for (Source source : selected) {
                if (source.handler().extract(source.slot(), ItemResource.of(source.stack()), 1, transaction) != 1) return false;
            }
            for (Remainder remainder : remainders) {
                var resource = ItemResource.of(remainder.stack());
                int rest = remainder.stack().getCount();
                rest -= remainder.source().handler().insert(remainder.source().slot(), resource, rest, transaction);
                if (rest > 0) rest -= remainder.source().handler().insert(resource, rest, transaction);
                if (rest > 0) rest -= buffer.insert(resource, rest, transaction);
                if (rest > 0) overflow.add(resource.toStack(rest));
            }
            var resultResource = ItemResource.of(output);
            int rest = output.getCount() - buffer.insert(resultResource, output.getCount(), transaction);
            if (rest > 0) overflow.add(resultResource.toStack(rest));
            transaction.commit();
        }
        // Spawn overflow only after extraction, remainder insertion and output insertion commit.
        var pos = box.getBlockPos();
        for (var stack : overflow) Containers.dropItemStack(box.getLevel(), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, stack);
        box.setChanged();
        return true;
    }
}
