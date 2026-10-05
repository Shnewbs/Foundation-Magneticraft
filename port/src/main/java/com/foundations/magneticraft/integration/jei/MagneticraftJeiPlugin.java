package com.foundations.magneticraft.integration.jei;

import com.foundations.magneticraft.FoundationsMagneticraft;
import com.foundations.magneticraft.integration.ProcessingCatalog;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

@JeiPlugin
public final class MagneticraftJeiPlugin implements IModPlugin {
    public static final RecipeType<ProcessingCatalog.Entry> CRUSHING = RecipeType.create("magneticraft", "crushing", ProcessingCatalog.Entry.class);
    public static final RecipeType<ProcessingCatalog.Entry> SLUICE = RecipeType.create("magneticraft", "sluice", ProcessingCatalog.Entry.class);
    private List<ProcessingCatalog.Entry> shown = List.of();
    private final Set<ProcessingCatalog.Entry> known = new HashSet<>();
    @Override public ResourceLocation getPluginUid() { return ResourceLocation.fromNamespaceAndPath("magneticraft", "processing"); }
    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ProcessingCategory(registration.getJeiHelpers().getGuiHelper(), false), new ProcessingCategory(registration.getJeiHelpers().getGuiHelper(), true));
    }
    private static List<ProcessingCatalog.Entry> select(List<ProcessingCatalog.Entry> entries, boolean sluice) {
        return entries.stream().filter(r -> r.kind().equals(sluice ? "sluice" : "crushing")).toList();
    }
    @Override public void registerRecipes(IRecipeRegistration registration) {
        shown = ProcessingCatalog.client(); known.addAll(shown);
        registration.addRecipes(CRUSHING, select(shown, false));
        registration.addRecipes(SLUICE, select(shown, true));
        registration.addItemStackInfo(new net.minecraft.world.item.ItemStack(FoundationsMagneticraft.SLUICE_BOX.get()), net.minecraft.network.chat.Component.translatable("jei.magneticraft.sluice_help"));
    }
    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new net.minecraft.world.item.ItemStack(FoundationsMagneticraft.CRUSHING_TABLE.get()), CRUSHING);
        registration.addRecipeCatalyst(new net.minecraft.world.item.ItemStack(FoundationsMagneticraft.SLUICE_BOX.get()), SLUICE);
    }
    @Override public void onRuntimeAvailable(IJeiRuntime runtime) {
        ProcessingCatalog.setClientListener(next -> {
            var manager = runtime.getRecipeManager();
            var removed = shown.stream().filter(r -> !next.contains(r)).toList();
            manager.hideRecipes(CRUSHING, select(removed, false)); manager.hideRecipes(SLUICE, select(removed, true));
            var added = next.stream().filter(r -> !known.contains(r)).toList();
            var restored = next.stream().filter(known::contains).toList();
            manager.addRecipes(CRUSHING, select(added, false)); manager.addRecipes(SLUICE, select(added, true));
            manager.unhideRecipes(CRUSHING, select(restored, false)); manager.unhideRecipes(SLUICE, select(restored, true));
            known.addAll(added); shown = next;
        });
    }
    @Override public void onRuntimeUnavailable() { ProcessingCatalog.setClientListener(null); known.clear(); shown = List.of(); }
}
