package com.foundations.magneticraft.integration;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/** Dependency-free owner layers. CraftTweaker wins conflicts; clearing KubeJS cannot erase CT. */
public final class RecipeOverrides {
    private RecipeOverrides() {}
    private static final Map<String, Map<String, String>> LAYERS = new HashMap<>();
    private static long revision;
    private static String key(String kind, String id) {
        if (!kind.equals("crushing") && !kind.equals("sluice")) throw new IllegalArgumentException("Unknown machine");
        if (!id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) throw new IllegalArgumentException("Recipe IDs require namespace:path");
        return kind + "/" + id;
    }
    private static void owner(String owner) {
        if (!owner.equals("kubejs") && !owner.equals("crafttweaker")) throw new IllegalArgumentException("Unknown script owner");
    }
    public static synchronized String put(String owner, String kind, String id, String json) {
        owner(owner);
        String key = key(kind, id);
        Map<String, String> layer = LAYERS.computeIfAbsent(owner, unused -> new HashMap<>());
        String previous = json == null ? layer.remove(key) : layer.put(key, json);
        revision++;
        return previous;
    }
    public static synchronized void clear(String owner) {
        owner(owner);
        if (LAYERS.remove(owner) != null) revision++;
    }
    public static synchronized void clearAll() { LAYERS.clear(); revision++; }
    public static synchronized long revision() { return revision; }
    public static synchronized Map<String, String> snapshot(String kind) {
        Map<String, String> merged = new TreeMap<>();
        for (String owner : new String[]{"kubejs", "crafttweaker"}) {
            for (var entry : LAYERS.getOrDefault(owner, Map.of()).entrySet()) {
                if (entry.getKey().startsWith(kind + "/"))
                    merged.put(entry.getKey().substring(kind.length() + 1), entry.getValue());
            }
        }
        return Map.copyOf(merged);
    }
}
