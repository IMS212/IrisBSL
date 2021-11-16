package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeBinding;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RegionChunkRenderer;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import net.coderbot.iris.shadows.ShadowRenderingState;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RegionChunkRenderer.class)
public abstract class MixinMultidrawChunkRenderBackend implements ChunkRenderer {


	@Shadow
	@Final
	@Mutable
	private GlVertexAttributeBinding[] vertexAttributeBindings;

	@Shadow
	@Final
	@Mutable
	private boolean isBlockFaceCullingEnabled;

	@Inject(method = "<init>", at = @At("RETURN"))
    private void iris$addAdditionalBindings(RenderDevice commandList, ChunkVertexType vertexFormat, CallbackInfo ci) {
        vertexAttributeBindings = ArrayUtils.addAll(vertexAttributeBindings,
                new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.BLOCK_ID,
                        vertexFormat.getCustomVertexFormat().getAttribute(IrisChunkMeshAttributes.BLOCK_ID)),
                new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.MID_TEX_COORD,
                        vertexFormat.getCustomVertexFormat().getAttribute(IrisChunkMeshAttributes.MID_TEX_COORD)),
                new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.TANGENT,
                        vertexFormat.getCustomVertexFormat().getAttribute(IrisChunkMeshAttributes.TANGENT)),
                new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.NORMAL,
                        vertexFormat.getCustomVertexFormat().getAttribute(IrisChunkMeshAttributes.NORMAL))
        );
		this.isBlockFaceCullingEnabled = this.isBlockFaceCullingEnabled && !ShadowRenderingState.areShadowsCurrentlyBeingRendered();
	}
}
