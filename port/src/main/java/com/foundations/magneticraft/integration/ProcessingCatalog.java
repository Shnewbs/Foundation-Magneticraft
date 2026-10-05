package com.foundations.magneticraft.integration;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Bounded wire model; contains recipe metadata only, never mutable game stacks. */
public final class ProcessingCatalog {
    public static final int MAX_JSON = 524288;
    public record Output(String item, int count, double chance) {
        public Output {
            identifier(item);
            if (count < 1 || count > 64 || !Double.isFinite(chance) || chance < 0 || chance > 1) throw new IllegalArgumentException("Invalid output");
        }
    }
    public record Entry(String kind, String id, String ingredient, int miningLevel, List<Output> outputs) {
        public Entry {
            if (!kind.equals("crushing") && !kind.equals("sluice")) throw new IllegalArgumentException("Unknown recipe kind");
            identifier(id); identifier(ingredient.startsWith("#") ? ingredient.substring(1) : ingredient);
            if (miningLevel < 0 || miningLevel > 4 || outputs.isEmpty() || outputs.size() > 64) throw new IllegalArgumentException("Invalid recipe");
            outputs = List.copyOf(outputs);
        }
    }
    private static void identifier(String id) {
        if (id.length() > 256 || !id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) throw new IllegalArgumentException("Invalid identifier");
    }
    public static String encode(List<Entry> entries) {
        if (entries.size() > 4096) throw new IllegalArgumentException("Too many recipes");
        String json = new Gson().toJson(entries);
        if (json.length() > MAX_JSON) throw new IllegalArgumentException("Recipe catalog exceeds payload budget");
        return json;
    }
    public static List<Entry> decode(String json) {
        if (json.length() > MAX_JSON) throw new IllegalArgumentException("Recipe catalog exceeds payload budget");
        var array = JsonParser.parseString(json).getAsJsonArray();
        if (array.size() > 4096) throw new IllegalArgumentException("Too many recipes");
        List<Entry> result = new ArrayList<>();
        for (var element : array) {
            var object = element.getAsJsonObject();
            var outputsJson = object.getAsJsonArray("outputs");
            if (outputsJson.size() > 64) throw new IllegalArgumentException("Too many outputs");
            List<Output> outputs = new ArrayList<>();
            for (var value : outputsJson) {
                var output = value.getAsJsonObject();
                outputs.add(new Output(output.get("item").getAsString(), output.get("count").getAsBigDecimal().intValueExact(), output.get("chance").getAsDouble()));
            }
            result.add(new Entry(object.get("kind").getAsString(), object.get("id").getAsString(), object.get("ingredient").getAsString(), object.get("miningLevel").getAsBigDecimal().intValueExact(), outputs));
        }
        return List.copyOf(result);
    }
    private static List<Entry> client = List.of();
    private static Consumer<List<Entry>> clientListener;
    public static List<Entry> client() { return client; }
    public static void acceptClient(List<Entry> entries) { client = List.copyOf(entries); if (clientListener != null) clientListener.accept(client); }
    public static void setClientListener(Consumer<List<Entry>> listener) { clientListener = listener; if (listener != null) listener.accept(client); }
    private ProcessingCatalog() {}
}
