package net.irisshaders.iris.compat.sodium.mixin.font;

import net.caffeinemc.mods.sodium.client.render.frapi.SpriteFinderCache;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// A hack to fix the most ridiculous Loom bug I've seen so far.
@Mixin(SpriteFinderCache.ReloadListener.class)
public abstract class MixinSpriteFinderCache {
	@Shadow
	public abstract void method_14491(ResourceManager manager);

	@SuppressWarnings("all")
	public void onResourceManagerReload(ResourceManager manager) {
		this.method_14491(manager);
	}
}
