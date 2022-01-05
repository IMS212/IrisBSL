package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.render.terrain.format.TerrainVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManager {
	@ModifyArg(method = "<init>", remap = false,
			at = @At(value = "INVOKE",
					target = "Lme/jellysquid/mods/sodium/render/chunk/draw/DefaultChunkRenderer;" +
							"<init>(Lme/jellysquid/mods/sodium/opengl/device/RenderDevice;" +
							"Lme/jellysquid/mods/sodium/render/terrain/format/TerrainVertexType;)V"))
	private TerrainVertexType iris$useExtendedVertexFormat$1(TerrainVertexType vertexType) {
		return Iris.isPackActive() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : vertexType;
	}

	@ModifyArg(method = "<init>", remap = false,
			at = @At(value = "INVOKE",
					target = "Lme/jellysquid/mods/sodium/render/chunk/compile/ChunkBuilder;<init>" +
							"(Lme/jellysquid/mods/sodium/render/terrain/format/TerrainVertexType;)V"))
	private TerrainVertexType iris$useExtendedVertexFormat$2(TerrainVertexType vertexType) {
		return Iris.isPackActive() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : vertexType;
	}
}
