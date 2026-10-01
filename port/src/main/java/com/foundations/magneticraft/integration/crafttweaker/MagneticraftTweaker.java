package com.foundations.magneticraft.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.base.IUndoableAction;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.foundations.magneticraft.integration.MagneticraftScripts;
import com.foundations.magneticraft.integration.RecipeOverrides;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister(modDeps = "crafttweaker")
@ZenCodeType.Name("mods.magneticraft.Recipes")
public final class MagneticraftTweaker {
    private MagneticraftTweaker() {}
    @ZenCodeType.Method public static void addCrushing(String id, String input, String output, int count, int miningLevel) {
        apply("crushing", id, MagneticraftScripts.crushingJson(input, output, count, miningLevel));
    }
    @ZenCodeType.Method public static void addSluice(String id, String input, String outputsJson) {
        apply("sluice", id, MagneticraftScripts.sluiceJson(input, outputsJson));
    }
    @ZenCodeType.Method public static void removeCrushing(String id) { apply("crushing", id, "{\"enabled\":false}"); }
    @ZenCodeType.Method public static void removeSluice(String id) { apply("sluice", id, "{\"enabled\":false}"); }
    private static void apply(String kind, String id, String json) {
        CraftTweakerAPI.apply(new IUndoableAction() {
            private String previous;
            @Override public void apply() { previous = RecipeOverrides.put("crafttweaker", kind, id, json); }
            @Override public void undo() { RecipeOverrides.put("crafttweaker", kind, id, previous); }
            @Override public String describe() { return "Set Magneticraft " + kind + " recipe " + id; }
            @Override public String describeUndo() { return "Restore Magneticraft " + kind + " recipe " + id; }
            @Override public String systemName() { return "Magneticraft"; }
        });
    }
}
