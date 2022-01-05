package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.render.terrain.format.TerrainVertexFormats;
import me.jellysquid.mods.sodium.render.terrain.format.standard.TerrainVertexType;
import me.jellysquid.mods.sodium.render.vertex.type.BufferVertexFormat;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderRegion.RenderRegionArenas.class)
public class MixinRenderRegionArenas {
	@Redirect(method = "<init>", remap = false,
			at = @At(value = "INVOKE",
					target = "Lme/jellysquid/mods/sodium/render/vertex/type/BufferVertexFormat;getStride()I",
					remap = false))
	private int iris$useExtendedStride(BufferVertexFormat format) {
		return Iris.isPackActive() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP.getBufferVertexFormat().getStride() : TerrainVertexFormats.STANDARD.getBufferVertexFormat().getStride();
	}
}
