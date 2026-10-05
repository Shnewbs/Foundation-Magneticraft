package com.foundations.magneticraft.manual;

import com.google.gson.JsonObject;
import com.foundations.magneticraft.integration.RecipeOverrides;
import com.foundations.magneticraft.integration.ScriptRecipeSources;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Server-only datapack recipes. Resource-manager identity invalidates the cache on /reload; hot lookups never scan datapacks.
 * Kept outside the vanilla recipe book until machine recipe serializers are migrated. */
public final class CrushingRecipes {
    private CrushingRecipes() {}
    public record Recipe(String id, String ingredient, ItemStack output, int miningLevel) {
        public boolean matches(ItemStack stack) {
            return ingredient.startsWith("#")
                ? stack.is(TagKey.create(Registries.ITEM, Identifier.parse(ingredient.substring(1))))
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
            var resources = manager.listResources("magneticraft/crushing", id -> id.getPath().endsWith(".json"));
            List<Recipe> recipes = new ArrayList<>();
            ScriptRecipeSources.merge(resources, "crushing").entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().toString())).forEach(entry -> {
                try {
                    JsonObject data = JsonParser.parseString(entry.getValue()).getAsJsonObject();
                    if (data.has("enabled") && !data.get("enabled").getAsBoolean()) return;
                    String ingredient = data.get("ingredient").getAsString();
                    Identifier.parse(ingredient.startsWith("#") ? ingredient.substring(1) : ingredient);
                    JsonObject result = data.getAsJsonObject("result");
                    var item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(result.get("id").getAsString()))
                        .orElseThrow(() -> new IllegalArgumentException("Unknown output item"));
                    int count = result.has("count") ? result.get("count").getAsInt() : 1;
                    int tier = data.has("mining_level") ? data.get("mining_level").getAsInt() : 0;
                    ItemStack output = new ItemStack(item, count);
                    if (output.isEmpty() || count < 1 || count > output.getMaxStackSize() || tier < 0 || tier > 4)
                        throw new IllegalArgumentException("Invalid output count or mining level");
                    recipes.add(new Recipe(entry.getKey(), ingredient, output, tier));
                } catch (Exception error) {
                    LogUtils.getLogger().warn("Ignoring invalid crushing recipe {}", entry.getKey(), error);
                }
            });
            // Specific item recipes override broad compatibility tags.
            recipes.sort(Comparator.comparing(recipe -> recipe.ingredient().startsWith("#")));
            return List.copyOf(recipes);
        });
    }
}
