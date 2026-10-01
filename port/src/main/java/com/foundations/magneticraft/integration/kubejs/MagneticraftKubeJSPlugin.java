package com.foundations.magneticraft.integration.kubejs;

import com.foundations.magneticraft.integration.MagneticraftScripts;
import com.foundations.magneticraft.integration.RecipeOverrides;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;

public final class MagneticraftKubeJSPlugin implements KubeJSPlugin {
    @Override public void registerBindings(BindingRegistry bindings) {
        if (bindings.type() == ScriptType.SERVER)
            bindings.add("Magneticraft", new MagneticraftScripts("kubejs"));
    }
    @Override public void beforeScriptsLoaded(ScriptManager manager) {
        if (manager.scriptType == ScriptType.SERVER) RecipeOverrides.clear("kubejs");
    }
}
