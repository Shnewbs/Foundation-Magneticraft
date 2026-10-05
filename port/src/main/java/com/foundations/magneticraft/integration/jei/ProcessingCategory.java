package com.foundations.magneticraft.integration.jei;

import com.foundations.magneticraft.FoundationsMagneticraft;
import com.foundations.magneticraft.integration.ProcessingCatalog;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ProcessingCategory implements IRecipeCategory<ProcessingCatalog.Entry> {
    private final boolean sluice;
    private final IDrawable background;
    private final IDrawable icon;
    public ProcessingCategory(IGuiHelper helper, boolean sluice) {
        this.sluice = sluice;
        background = helper.createBlankDrawable(220, 180);
        icon = helper.createDrawableIngredient(mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
            new ItemStack(sluice ? FoundationsMagneticraft.SLUICE_BOX.get() : FoundationsMagneticraft.CRUSHING_TABLE.get()));
    }
    @Override public RecipeType<ProcessingCatalog.Entry> getRecipeType() { return sluice ? MagneticraftJeiPlugin.SLUICE : MagneticraftJeiPlugin.CRUSHING; }
    @Override public Component getTitle() { return Component.translatable(sluice ? "block.magneticraft.sluice_box" : "block.magneticraft.crushing_table"); }
    public IDrawable getBackground() { return background; }
    @Override public int getWidth() { return 220; }
    @Override public int getHeight() { return 180; }
    @Override public IDrawable getIcon() { return icon; }
    @Override public ResourceLocation getRegistryName(ProcessingCatalog.Entry recipe) { return ResourceLocation.parse(recipe.id()); }
    private static List<ItemStack> inputs(String ingredient) {
        if (ingredient.startsWith("#")) {
            var tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(ingredient.substring(1)));
            return BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(stack -> stack.is(tag)).toList();
        }
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(ingredient)).map(i -> List.of(new ItemStack(i))).orElse(List.of());
    }
    @Override public void setRecipe(IRecipeLayoutBuilder builder, ProcessingCatalog.Entry recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 8).addItemStacks(inputs(recipe.ingredient()));
        if (sluice) builder.addSlot(RecipeIngredientRole.INPUT, 4, 34).addItemStack(new ItemStack(Items.WATER_BUCKET));
        for (int i = 0; i < recipe.outputs().size(); i++) {
            var output = recipe.outputs().get(i);
            ItemStack stack = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(output.item())).map(item -> new ItemStack(item, output.count())).orElse(ItemStack.EMPTY);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 68 + (i % 8) * 18, 8 + (i / 8) * 18).addItemStack(stack)
                .addRichTooltipCallback((slot, tooltip) -> tooltip.add(Component.translatable("jei.magneticraft.chance", String.format(java.util.Locale.ROOT, "%.4f", output.chance() * 100))));
        }
    }
    @Override public void draw(ProcessingCatalog.Entry recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable(sluice ? "jei.magneticraft.water" : "jei.magneticraft.tier", sluice ? 1000 : recipe.miningLevel()), 4, 151, 0x404040, false);
        if (sluice) graphics.drawString(font, Component.translatable("jei.magneticraft.sluice_time"), 4, 165, 0x404040, false);
    }
}
