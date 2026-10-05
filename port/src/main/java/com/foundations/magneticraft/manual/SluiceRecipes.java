package com.foundations.magneticraft.manual;

import com.google.gson.JsonObject;
import com.foundations.magneticraft.integration.RecipeOverrides;
import com.foundations.magneticraft.integration.ScriptRecipeSources;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Server-only datapack recipes. Resource-manager identity invalidates the cache on /reload; hot lookups never scan datapacks.
 * Kept outside the vanilla recipe book until machine recipe serializers are migrated. */
public final class SluiceRecipes {
    private SluiceRecipes() {}
    public record Output(ItemStack stack, float chance) {}
    public record Recipe(String id, String ingredient, List<Output> outputs) {
        public boolean matches(ItemStack stack) {
            return ingredient.startsWith("#")
                ? stack.is(TagKey.create(Registries.ITEM, ResourceLocation.parse(ingredient.substring(1))))
                : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(ingredient);
        }
    }
    private static final com.foundations.magneticraft.integration.GenerationCache<ResourceManager, List<Recipe>> CACHE = new com.foundations.magneticraft.integration.GenerationCache<>();
    public static void clearCache() { CACHE.clear(); }

    public static Recipe find(Level level, ItemStack input) {
        if (input.isEmpty()) return null;
        for (Recipe recipe : all(level)) if (recipe.matches(input)) return recipe;
        return null;
    }
    public static List<Recipe> all(Level level) {
        if (level.getServer() == null) return List.of();
        ResourceManager manager = level.getServer().getResourceManager();
        return CACHE.get(manager, RecipeOverrides.revision(), () -> {
            var resources = manager.listResources("magneticraft/sluice", id -> id.getPath().endsWith(".json"));
            List<Recipe> recipes = new ArrayList<>();
            ScriptRecipeSources.merge(resources, "sluice").entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().toString())).forEach(entry -> {
                try {
                    JsonObject data = JsonParser.parseString(entry.getValue()).getAsJsonObject();
                    if (data.has("enabled") && !data.get("enabled").getAsBoolean()) return;
                    String ingredient = data.get("ingredient").getAsString();
                    ResourceLocation.parse(ingredient.startsWith("#") ? ingredient.substring(1) : ingredient);
                    List<Output> outputs = new ArrayList<>();
                    for (var element : data.getAsJsonArray("outputs")) {
                        JsonObject result = element.getAsJsonObject();
                        var item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(result.get("id").getAsString()))
                            .orElseThrow(() -> new IllegalArgumentException("Unknown output item"));
                        int count = result.has("count") ? result.get("count").getAsInt() : 1;
                        float chance = result.get("chance").getAsFloat();
                        ItemStack stack = new ItemStack(item, count);
                        if (stack.isEmpty() || count < 1 || count > stack.getMaxStackSize()
                                || !Float.isFinite(chance) || chance < 0 || chance > 1)
                            throw new IllegalArgumentException("Invalid output count or chance");
                        outputs.add(new Output(stack, chance));
                    }
                    if (outputs.isEmpty() || outputs.size() > 64) throw new IllegalArgumentException("No outputs");
                    recipes.add(new Recipe(entry.getKey(), ingredient, List.copyOf(outputs)));
                } catch (Exception error) {
                    LogUtils.getLogger().warn("Ignoring invalid sluice recipe {}", entry.getKey(), error);
                }
            });
            // Specific item recipes override broad compatibility tags.
            recipes.sort(Comparator.comparing(recipe -> recipe.ingredient().startsWith("#")));
            return List.copyOf(recipes);
        });
    }
}
