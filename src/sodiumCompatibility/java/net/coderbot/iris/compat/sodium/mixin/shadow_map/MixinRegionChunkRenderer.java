package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import me.jellysquid.mods.sodium.render.chunk.draw.DefaultChunkRenderer;
import net.coderbot.iris.shadows.ShadowRenderingState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DefaultChunkRenderer.class)
public class MixinRegionChunkRenderer {
	@Shadow(remap = false)
	@Final
	private boolean isBlockFaceCullingEnabled;

	@Redirect(method = "buildDrawBatches", remap = false,
			at = @At(value = "FIELD",
					target = "Lme/jellysquid/mods/sodium/render/chunk/draw/DefaultChunkRenderer;isBlockFaceCullingEnabled:Z"))
	private boolean iris$disableBlockFaceCullingInShadowPass(DefaultChunkRenderer renderer) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return false;
		} else {
			return isBlockFaceCullingEnabled;
		}
	}
}
