package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.render.chunk.compile.buffers.ChunkMeshBuilder;
import me.jellysquid.mods.sodium.render.chunk.compile.buffers.IndexBufferBuilder;
import me.jellysquid.mods.sodium.render.terrain.FluidRenderer;
import me.jellysquid.mods.sodium.render.terrain.quad.properties.ModelQuadWinding;
import me.jellysquid.mods.sodium.render.vertex.VertexSink;
import net.coderbot.iris.compat.sodium.impl.vertex_format.xhfp.XHFPModelVertexBufferWriterNio;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Modifies fluid rendering to disable the back face vertex deduplication optimization with the extended vertex format.
 * This is needed because with the extended vertex format, vertex normals are dependent on quad orientation, so using
 * the same vertices for each side of the quad does not work.
 */
@Mixin(FluidRenderer.class)
public class MixinFluidRenderer {
	@SuppressWarnings("mapping")
	@Redirect(method = "render",
			at = @At(value = "INVOKE", target = "add",
					ordinal = 0),
	slice = @Slice(
			from = @At(value = "INVOKE", target = "net/minecraft/world/level/material/FluidState.shouldRenderBackwardUpFace" +
					"(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z")
	))
	private void iris$fixBackwardUpFaceNormal(IndexBufferBuilder indices, int vertexStart, ModelQuadWinding winding,
											  BlockAndTintGetter world, FluidState fluidState, BlockPos pos,
											  BlockPos offset, ChunkMeshBuilder buffers) {
		iris$addIndicesFixNormals(indices, vertexStart, winding, buffers);
	}

	@SuppressWarnings("mapping")
	@Redirect(method = "render",
			at = @At(value = "INVOKE", target = "me/jellysquid/mods/sodium/client/model/IndexBufferBuilder.add" +
					"(ILme/jellysquid/mods/sodium/client/model/quad/properties/ModelQuadWinding;)V",
					ordinal = 1),
			slice = @Slice(
					from = @At(value = "FIELD",
							target = "me/jellysquid/mods/sodium/common/util/DirectionUtil.HORIZONTAL_DIRECTIONS : [Lnet/minecraft/core/Direction;")
			))
	private void iris$fixSidewaysInnerFaceNormal(IndexBufferBuilder indices, int vertexStart, ModelQuadWinding winding,
												 BlockAndTintGetter world, FluidState fluidState, BlockPos pos,
												 BlockPos offset, ChunkMeshBuilder buffers) {
		iris$addIndicesFixNormals(indices, vertexStart, winding, buffers);
	}

	@Unique
	private void iris$addIndicesFixNormals(IndexBufferBuilder indices, int vertexStart, ModelQuadWinding winding,
										   ChunkMeshBuilder buffers) {
		if (winding == ModelQuadWinding.COUNTERCLOCKWISE) {
			VertexSink sink = buffers.getVertexSink();

			if (sink instanceof XHFPModelVertexBufferWriterNio) {
				((XHFPModelVertexBufferWriterNio) sink).copyQuadAndFlipNormal();

				indices.add(vertexStart + 4, winding);

				return;
			}
		}

		indices.add(vertexStart, winding);
	}
}
