package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeBinding;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import me.jellysquid.mods.sodium.client.gl.buffer.GlBuffer;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.tessellation.TessellationBinding;
import me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import net.coderbot.iris.compat.sodium.impl.vertex_format.terrain_xhfp.XHFPModelVertexType;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;
import java.util.List;

@Mixin(DefaultChunkRenderer.class)
public abstract class MixinRegionChunkRenderer extends ShaderChunkRenderer {
	public MixinRegionChunkRenderer(RenderDevice device, ChunkVertexType vertexType) {
		super(device, vertexType);
	}

	@Redirect(remap = false, method = "createRegionTessellation", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/gl/tessellation/TessellationBinding;forVertexBuffer(Lme/jellysquid/mods/sodium/client/gl/buffer/GlBuffer;[Lme/jellysquid/mods/sodium/client/gl/attribute/GlVertexAttributeBinding;)Lme/jellysquid/mods/sodium/client/gl/tessellation/TessellationBinding;"))
	private TessellationBinding iris$onInit(GlBuffer buffer, GlVertexAttributeBinding[] attributes) {
		if (!BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat()) {
			return TessellationBinding.forVertexBuffer(buffer, attributes);
		}


		attributes = new GlVertexAttributeBinding[]{
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.ATTRIBUTE_POSITION_ID,
				XHFPModelVertexType.VERTEX_FORMAT.getAttribute(IrisChunkMeshAttributes.POSITION_MATERIAL_MESH)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.ATTRIBUTE_COLOR,
				XHFPModelVertexType.VERTEX_FORMAT.getAttribute(IrisChunkMeshAttributes.COLOR_SHADE)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE,
				XHFPModelVertexType.VERTEX_FORMAT.getAttribute(IrisChunkMeshAttributes.BLOCK_TEXTURE)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE,
				XHFPModelVertexType.VERTEX_FORMAT.getAttribute(IrisChunkMeshAttributes.LIGHT_TEXTURE)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.MID_BLOCK,
				XHFPModelVertexType.VERTEX_FORMAT.getAttribute(IrisChunkMeshAttributes.MID_BLOCK)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.BLOCK_ID,
				XHFPModelVertexType.VERTEX_FORMAT.getAttribute(IrisChunkMeshAttributes.BLOCK_ID)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.MID_TEX_COORD,
				XHFPModelVertexType.VERTEX_FORMAT.getAttribute(IrisChunkMeshAttributes.MID_TEX_COORD)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.TANGENT,
				XHFPModelVertexType.VERTEX_FORMAT.getAttribute(IrisChunkMeshAttributes.TANGENT)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.NORMAL,
				XHFPModelVertexType.VERTEX_FORMAT.getAttribute(IrisChunkMeshAttributes.NORMAL))
		};

		return TessellationBinding.forVertexBuffer(buffer, attributes);
	}
}
