package com.foundations.magneticraft.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Server scripts use explicit IDs. Changes never mutate built-in datapack resources. */
public final class MagneticraftScripts {
    private final String owner;
    public MagneticraftScripts(String owner) { this.owner = owner; }
    private static void ingredient(String value) {
        Identifier.parse(value.startsWith("#") ? value.substring(1) : value);
        if (!value.startsWith("#")) output(value, 1);
    }
    private static void output(String id, int count) {
        var item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(id))
            .orElseThrow(() -> new IllegalArgumentException("Unknown item " + id));
        ItemStack stack = new ItemStack(item, count);
        if (stack.isEmpty() || count < 1 || count > stack.getMaxStackSize())
            throw new IllegalArgumentException("Invalid count for " + id);
    }
    public static String crushingJson(String input, String output, int count, int miningLevel) {
        ingredient(input); output(output, count);
        if (miningLevel < 0 || miningLevel > 4) throw new IllegalArgumentException("Mining level must be 0-4");
        JsonObject data = new JsonObject(); data.addProperty("ingredient", input);
        JsonObject result = new JsonObject(); result.addProperty("id", output); result.addProperty("count", count);
        data.add("result", result); data.addProperty("mining_level", miningLevel);
        return data.toString();
    }
    public static String sluiceJson(String input, String outputsJson) {
        ingredient(input);
        JsonArray outputs = JsonParser.parseString(outputsJson).getAsJsonArray();
        if (outputs.isEmpty()) throw new IllegalArgumentException("Sluice needs at least one output");
        for (var element : outputs) {
            JsonObject entry = element.getAsJsonObject();
            output(entry.get("id").getAsString(), entry.has("count") ? entry.get("count").getAsInt() : 1);
            double chance = entry.get("chance").getAsDouble();
            if (!Double.isFinite(chance) || chance < 0 || chance > 1)
                throw new IllegalArgumentException("Chance must be finite and 0-1");
        }
        JsonObject data = new JsonObject(); data.addProperty("ingredient", input); data.add("outputs", outputs);
        return data.toString();
    }
    public void addCrushing(String id, String input, String output, int count, int miningLevel) {
        RecipeOverrides.put(owner, "crushing", id, crushingJson(input, output, count, miningLevel));
    }
    public void addSluice(String id, String input, String outputsJson) {
        RecipeOverrides.put(owner, "sluice", id, sluiceJson(input, outputsJson));
    }
    public void removeCrushing(String id) { RecipeOverrides.put(owner, "crushing", id, "{\"enabled\":false}"); }
    public void removeSluice(String id) { RecipeOverrides.put(owner, "sluice", id, "{\"enabled\":false}"); }
}
