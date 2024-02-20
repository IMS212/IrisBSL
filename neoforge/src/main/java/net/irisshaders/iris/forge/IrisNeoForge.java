package net.irisshaders.iris.forge;

import net.irisshaders.iris.Iris;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod(Iris.MODID)
public final class IrisNeoForge {
    public IrisNeoForge() {
        // Run our common setup.
		//event.addListener(this::registerKeys);
    }

	public void registerKeys(RegisterKeyMappingsEvent event) {
		event.register(Iris.reloadKeybind);
		event.register(Iris.toggleShadersKeybind);
		event.register(Iris.shaderpackScreenKeybind);
	}
}
