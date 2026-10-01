package com.foundations.magneticraft.integration;

public final class RecipeOverridesTest {
    private static void check(boolean pass, String message) { if (!pass) throw new AssertionError(message); }
    public static void main(String[] args) {
        RecipeOverrides.clearAll();
        long before = RecipeOverrides.revision();
        RecipeOverrides.put("kubejs", "crushing", "pack:test", "kube");
        check(RecipeOverrides.revision() > before, "Cache stays stale");
        RecipeOverrides.put("crafttweaker", "crushing", "pack:test", "ct");
        check(RecipeOverrides.snapshot("crushing").get("pack:test").equals("ct"), "Conflict precedence");
        check(RecipeOverrides.snapshot("sluice").isEmpty(), "Machine layers leak");
        String previous = RecipeOverrides.put("crafttweaker", "crushing", "pack:test", "disabled");
        RecipeOverrides.put("crafttweaker", "crushing", "pack:test", previous);
        check(RecipeOverrides.snapshot("crushing").get("pack:test").equals("ct"), "Undo loses previous override");
        RecipeOverrides.clear("kubejs");
        check(RecipeOverrides.snapshot("crushing").get("pack:test").equals("ct"), "Reload clears another owner");
        RecipeOverrides.clear("crafttweaker");
        check(RecipeOverrides.snapshot("crushing").isEmpty(), "Deleted script keeps recipes");
        try { RecipeOverrides.put("kubejs", "crushing", "bad id", "x"); throw new AssertionError("Bad id accepted"); }
        catch (IllegalArgumentException expected) {}
        RecipeOverrides.clearAll();
        System.out.println("Script owner isolation, reload, precedence, rollback and cache checks passed");
    }
}
