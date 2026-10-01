package com.foundations.magneticraft.manual;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Server-only recipe lookup; a craft always builds a fresh plan. */
public final class FabricatorCrafting {
    public record Preview(ItemStack output, int foundMask, boolean ready) {}
    private record Plan(ItemStack output, List<FabricatorInventory.Source> sources,
                        List<FabricatorInventory.Remainder> remainders, int foundMask) {}
    private FabricatorCrafting() {}
    public static Preview preview(FabricatorBlockEntity box) {
        Plan plan = plan(box);
        return plan == null ? new Preview(ItemStack.EMPTY, 0, false)
            : new Preview(plan.output(), plan.foundMask(), plan.sources() != null);
    }
    public static boolean craft(FabricatorBlockEntity box) {
        Plan plan = plan(box);
        return plan != null && plan.sources() != null
            && FabricatorInventory.commit(box, plan.sources(), plan.remainders(), plan.output());
    }
    private static ItemStack assemble(CraftingRecipe recipe, CraftingInput input, Level level) {
        return recipe.assemble(input);
    }
    private static boolean sameOutput(ItemStack a, ItemStack b) {
        return a.getCount() == b.getCount() && ItemStack.isSameItemSameComponents(a, b);
    }
    private static Plan plan(FabricatorBlockEntity box) {
        Level level = box.getLevel();
        if (level == null || level.isClientSide()) return null;
        List<ItemStack> grid = new ArrayList<>();
        List<Integer> required = new ArrayList<>();
        int left = 3, top = 3;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = box.ghosts().getItem(slot);
            grid.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
            if (!stack.isEmpty()) { required.add(slot); left = Math.min(left, slot % 3); top = Math.min(top, slot / 3); }
        }
        if (required.isEmpty()) return null;
        CraftingInput template = CraftingInput.of(3, 3, grid);
        var holder = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, template, level);
        if (holder.isEmpty()) return null;
        CraftingRecipe recipe = holder.get().value();
        ItemStack output = assemble(recipe, template, level);
        if (output.isEmpty()) return null;
        List<FabricatorInventory.Source> candidates = FabricatorInventory.sources(box);
        int[] available = candidates.stream().mapToInt(FabricatorInventory.Source::available).toArray();
        boolean[][] eligible = new boolean[required.size()][candidates.size()];
        int mask = 0;
        for (int input = 0; input < required.size(); input++) {
            int slot = required.get(input);
            for (int source = 0; source < candidates.size(); source++) {
                List<ItemStack> trialGrid = copyGrid(grid);
                trialGrid.set(slot, candidates.get(source).stack().copy());
                CraftingInput trial = CraftingInput.of(3, 3, trialGrid);
                if (recipe.matches(trial, level) && sameOutput(output, assemble(recipe, trial, level))) {
                    eligible[input][source] = true;
                    mask |= 1 << slot;
                }
            }
        }
        int[] choice = FabricatorPlanner.allocate(available, eligible);
        if (choice == null) return new Plan(output, null, List.of(), mask);
        List<ItemStack> actualGrid = copyGrid(grid);
        List<FabricatorInventory.Source> selected = new ArrayList<>();
        for (int input = 0; input < required.size(); input++) {
            var source = candidates.get(choice[input]);
            selected.add(source);
            actualGrid.set(required.get(input), source.stack().copy());
        }
        CraftingInput actual = CraftingInput.of(3, 3, actualGrid);
        if (!recipe.matches(actual, level) || !sameOutput(output, assemble(recipe, actual, level))) {
            return new Plan(output, null, List.of(), mask);
        }
        var remaining = recipe.getRemainingItems(actual);
        List<FabricatorInventory.Remainder> remainders = new ArrayList<>();
        for (int input = 0; input < required.size(); input++) {
            int slot = required.get(input);
            int trimmed = (slot / 3 - top) * actual.width() + slot % 3 - left;
            if (trimmed < remaining.size() && !remaining.get(trimmed).isEmpty()) {
                remainders.add(new FabricatorInventory.Remainder(selected.get(input), remaining.get(trimmed).copy()));
            }
        }
        return new Plan(output.copy(), selected, remainders, mask);
    }
    private static List<ItemStack> copyGrid(List<ItemStack> grid) {
        return grid.stream().map(ItemStack::copy).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
}
