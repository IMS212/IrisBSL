package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import me.jellysquid.mods.sodium.opengl.types.RenderPipeline;
import me.jellysquid.mods.sodium.render.chunk.passes.ChunkRenderPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkRenderPass.class)
public class MixinBlockRenderPass {
	@Shadow(remap = false)
	@Final
	@Mutable
	private float alphaCutoff;

    @Inject(method = "<init>", at = @At("RETURN"))
	public void changeAlphaCutoff(RenderPipeline pipeline, boolean mipped, float alphaCutoff, CallbackInfo ci) {
		if (mipped && alphaCutoff == 0.5F) {
			this.alphaCutoff = 0.1F;
		}
	}
}
