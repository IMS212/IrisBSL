package net.irisshaders.iris.fabric;

import net.irisshaders.iris.Iris;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public final class IrisFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        //ExampleMod.init();
		KeyBindingHelper.registerKeyBinding(Iris.reloadKeybind);
		KeyBindingHelper.registerKeyBinding(Iris.toggleShadersKeybind);
		KeyBindingHelper.registerKeyBinding(Iris.shaderpackScreenKeybind);
    }
}
