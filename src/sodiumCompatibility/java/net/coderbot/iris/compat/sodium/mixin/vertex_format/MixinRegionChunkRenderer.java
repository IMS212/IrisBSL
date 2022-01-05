package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.opengl.array.VertexArray;
import me.jellysquid.mods.sodium.opengl.array.VertexArrayDescription;
import me.jellysquid.mods.sodium.opengl.array.VertexArrayResourceBinding;
import me.jellysquid.mods.sodium.opengl.attribute.VertexAttributeBinding;
import me.jellysquid.mods.sodium.opengl.device.RenderDevice;
import me.jellysquid.mods.sodium.render.chunk.draw.ChunkRenderer;
import me.jellysquid.mods.sodium.render.chunk.draw.DefaultChunkRenderer;
import me.jellysquid.mods.sodium.render.chunk.draw.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.render.terrain.format.TerrainMeshAttribute;
import me.jellysquid.mods.sodium.render.terrain.format.TerrainVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import net.coderbot.iris.compat.sodium.mixin.BufferTargetInterface;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(DefaultChunkRenderer.class)
public abstract class MixinRegionChunkRenderer extends ShaderChunkRenderer {

	public MixinRegionChunkRenderer(RenderDevice device, TerrainVertexType vertexType) {
		super(device, vertexType);
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/opengl/device/RenderDevice;createVertexArray(Lme/jellysquid/mods/sodium/opengl/array/VertexArrayDescription;)Lme/jellysquid/mods/sodium/opengl/array/VertexArray;"))
	private VertexArray<DefaultChunkRenderer.BufferTarget> a(RenderDevice instance, VertexArrayDescription<DefaultChunkRenderer.BufferTarget> tVertexArrayDescription) {
		List<VertexArrayResourceBinding<DefaultChunkRenderer.BufferTarget>> list = new ArrayList<>();
		list.add(new VertexArrayResourceBinding<>(DefaultChunkRenderer.BufferTarget.VERTICES, new VertexAttributeBinding[]{new VertexAttributeBinding(1, this.vertexFormat.getAttribute(TerrainMeshAttribute.POSITION_ID)), new VertexAttributeBinding(2, this.vertexFormat.getAttribute(TerrainMeshAttribute.COLOR)), new VertexAttributeBinding(3, this.vertexFormat.getAttribute(TerrainMeshAttribute.BLOCK_TEXTURE)), new VertexAttributeBinding(4, this.vertexFormat.getAttribute(TerrainMeshAttribute.LIGHT_TEXTURE)), new VertexAttributeBinding(5, this.vertexFormat.getAttribute(IrisChunkMeshAttributes.NORMAL)), new VertexAttributeBinding(6, this.vertexFormat.getAttribute(IrisChunkMeshAttributes.TANGENT)), new VertexAttributeBinding(7, this.vertexFormat.getAttribute(IrisChunkMeshAttributes.MID_TEX_COORD)), new VertexAttributeBinding(8, this.vertexFormat.getAttribute(IrisChunkMeshAttributes.BLOCK_ID))}));
		return instance.createVertexArray(new VertexArrayDescription<>(DefaultChunkRenderer.BufferTarget.class, list));
	}

}
