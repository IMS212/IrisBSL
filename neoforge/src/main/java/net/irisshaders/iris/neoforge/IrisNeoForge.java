package net.irisshaders.iris.neoforge;

import net.irisshaders.iris.Iris;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(Iris.MODID)
public final class IrisNeoForge {
    public IrisNeoForge(IEventBus event) {
        // Run our common setup.
		event.addListener(this::registerKeys);
    }

	public void registerKeys(RegisterKeyMappingsEvent event) {
		event.register(Iris.reloadKeybind);
		event.register(Iris.toggleShadersKeybind);
		event.register(Iris.shaderpackScreenKeybind);
	}
}
