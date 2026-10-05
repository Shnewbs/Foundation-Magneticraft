package com.foundations.magneticraft.manual;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

/** Empty one container on a copy, committing the replacement only after an exact water drain. */
public final class SluiceWaterContainer {
    private SluiceWaterContainer() {}
    public static boolean consume(Player player) {
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        var handler = held.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return false;
        FluidStack request = new FluidStack(Fluids.WATER, WaterBudget.CAPACITY);
        FluidStack simulated = handler.drain(request, FluidAction.SIMULATE);
        if (!simulated.is(Fluids.WATER) || simulated.getAmount() != WaterBudget.CAPACITY) return false;
        if (player.getAbilities().instabuild) return true;
        FluidStack paid = handler.drain(request, FluidAction.EXECUTE);
        if (!paid.is(Fluids.WATER) || paid.getAmount() != WaterBudget.CAPACITY) return false;
        ItemStack replacement = handler.getContainer();
        if (held.getCount() == 1) player.setItemInHand(InteractionHand.MAIN_HAND, replacement);
        else {
            held.shrink(1);
            if (!replacement.isEmpty() && !player.getInventory().add(replacement)) player.drop(replacement, false);
        }
        return true;
    }
}
