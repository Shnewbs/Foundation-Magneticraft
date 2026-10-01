package com.foundations.magneticraft.integration;

import com.mojang.logging.LogUtils;
import java.io.Reader;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

public final class ScriptRecipeSources {
    private ScriptRecipeSources() {}
    public static Map<String, String> merge(Map<ResourceLocation, Resource> resources, String kind) {
        Map<String, String> json = new TreeMap<>();
        String prefix = "magneticraft/" + kind + "/";
        for (var entry : resources.entrySet()) {
            String path = entry.getKey().getPath();
            String id = entry.getKey().getNamespace() + ":" + path.substring(prefix.length(), path.length() - 5);
            try (Reader reader = entry.getValue().openAsReader()) {
                StringBuilder contents = new StringBuilder();
                char[] buffer = new char[4096];
                for (int count; (count = reader.read(buffer)) != -1;) contents.append(buffer, 0, count);
                json.put(id, contents.toString());
            } catch (Exception error) {
                LogUtils.getLogger().warn("Unable to read Magneticraft {} recipe {}", kind, id, error);
            }
        }
        json.putAll(RecipeOverrides.snapshot(kind));
        return json;
    }
}
