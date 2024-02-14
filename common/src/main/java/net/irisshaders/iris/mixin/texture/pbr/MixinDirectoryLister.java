package net.irisshaders.iris.mixin.texture.pbr;

import net.irisshaders.iris.texture.pbr.PBRType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.BiConsumer;

@Mixin(DirectoryLister.class)
public class MixinDirectoryLister {
	@ModifyArg(index = 0, method = "run(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/renderer/texture/atlas/SpriteSource$Output;)V", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V", remap = false, ordinal = 0))
	private BiConsumer<? super ResourceLocation, ? super Resource> iris$modifyForEachAction(BiConsumer<ResourceLocation, Resource> action) {
		BiConsumer<? super ResourceLocation, ? super Resource> wrappedAction = (location, resource) -> {
			String basePath = PBRType.removeSuffix(location.getPath());
			if (basePath != null) {
				ResourceLocation baseLocation = location.withPath(basePath);
				if (Minecraft.getInstance().getResourceManager().getResource(baseLocation).isPresent()) {
					return;
				}
			}
			action.accept(location, resource);
		};
		return wrappedAction;
	}
}
