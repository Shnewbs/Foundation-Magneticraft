package com.foundations.magneticraft.manual;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** ItemAccess handles single-container exchange, stashing and creative-mode semantics. */
public final class SluiceWaterContainer {
    private SluiceWaterContainer() {}
    public static boolean consume(Player player) {
        var access = ItemAccess.forPlayerInteraction(player, InteractionHand.MAIN_HAND).oneByOne();
        var handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null) return false;
        try (var transaction = Transaction.openRoot()) {
            if (handler.extract(FluidResource.of(Fluids.WATER), WaterBudget.CAPACITY, transaction) != WaterBudget.CAPACITY) return false;
            transaction.commit();
            return true;
        }
    }
}
