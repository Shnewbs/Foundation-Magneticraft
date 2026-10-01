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

/** Server-only datapack recipes. Resource identity invalidates the cache on /reload.
 * Kept outside the vanilla recipe book until machine recipe serializers are migrated. */
public final class SluiceRecipes {
    private SluiceRecipes() {}
    public record Output(ItemStack stack, float chance) {}
    public record Recipe(String ingredient, List<Output> outputs) {
        public boolean matches(ItemStack stack) {
            return ingredient.startsWith("#")
                ? stack.is(TagKey.create(Registries.ITEM, Identifier.parse(ingredient.substring(1))))
                : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(ingredient);
        }
    }
    private record Snapshot(Map<Identifier, Resource> resources, long scriptRevision, List<Recipe> recipes) {}
    private static final Map<ResourceManager, Snapshot> CACHE = new WeakHashMap<>();

    public static Recipe find(Level level, ItemStack input) {
        if (input.isEmpty() || level.getServer() == null) return null;
        ResourceManager manager = level.getServer().getResourceManager();
        Map<Identifier, Resource> resources = manager.listResources(
            "magneticraft/sluice", id -> id.getPath().endsWith(".json"));
        long scriptRevision = RecipeOverrides.revision();
        Snapshot snapshot = CACHE.get(manager);
        if (snapshot == null || !snapshot.resources().equals(resources) || snapshot.scriptRevision() != scriptRevision) {
            List<Recipe> recipes = new ArrayList<>();
            ScriptRecipeSources.merge(resources, "sluice").entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().toString())).forEach(entry -> {
                try {
                    JsonObject data = JsonParser.parseString(entry.getValue()).getAsJsonObject();
                    if (data.has("enabled") && !data.get("enabled").getAsBoolean()) return;
                    String ingredient = data.get("ingredient").getAsString();
                    Identifier.parse(ingredient.startsWith("#") ? ingredient.substring(1) : ingredient);
                    List<Output> outputs = new ArrayList<>();
                    for (var element : data.getAsJsonArray("outputs")) {
                        JsonObject result = element.getAsJsonObject();
                        var item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(result.get("id").getAsString()))
                            .orElseThrow(() -> new IllegalArgumentException("Unknown output item"));
                        int count = result.has("count") ? result.get("count").getAsInt() : 1;
                        float chance = result.get("chance").getAsFloat();
                        ItemStack stack = new ItemStack(item, count);
                        if (stack.isEmpty() || count < 1 || count > stack.getMaxStackSize()
                                || !Float.isFinite(chance) || chance < 0 || chance > 1)
                            throw new IllegalArgumentException("Invalid output count or chance");
                        outputs.add(new Output(stack, chance));
                    }
                    if (outputs.isEmpty()) throw new IllegalArgumentException("No outputs");
                    recipes.add(new Recipe(ingredient, List.copyOf(outputs)));
                } catch (Exception error) {
                    LogUtils.getLogger().warn("Ignoring invalid sluice recipe {}", entry.getKey(), error);
                }
            });
            // Specific item recipes override broad compatibility tags.
            recipes.sort(Comparator.comparing(recipe -> recipe.ingredient().startsWith("#")));
            snapshot = new Snapshot(resources, scriptRevision, List.copyOf(recipes));
            CACHE.put(manager, snapshot);
        }
        for (Recipe recipe : snapshot.recipes()) if (recipe.matches(input)) return recipe;
        return null;
    }
}
