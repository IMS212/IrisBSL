package net.irisshaders.iris.vertices.sodium;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.caffeinemc.mods.sodium.api.memory.MemoryIntrinsics;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.NormalHelper;
import org.lwjgl.system.MemoryUtil;

public class ModelToEntityVertexSerializer implements VertexSerializer {
	private final VertexFormat FORMAT;

	private static final int MODEL_COLOR = DefaultVertexFormat.NEW_ENTITY.getOffset(VertexFormatElement.COLOR);
	private static final int MODEL_UV = DefaultVertexFormat.NEW_ENTITY.getOffset(VertexFormatElement.UV0);
	private static final int MODEL_LIGHT = DefaultVertexFormat.NEW_ENTITY.getOffset(VertexFormatElement.UV2);
	private static final int MODEL_NORMAL = DefaultVertexFormat.NEW_ENTITY.getOffset(VertexFormatElement.NORMAL);
	private static final int MODEL_TANGENT = DefaultVertexFormat.NEW_ENTITY.getOffset(IrisVertexFormats.TANGENT_ELEMENT);
	private static final int MODEL_MID_COORD = DefaultVertexFormat.NEW_ENTITY.getOffset(IrisVertexFormats.MID_TEXTURE_ELEMENT);
	private static final int MODEL_BLOCK_ID = DefaultVertexFormat.NEW_ENTITY.getOffset(IrisVertexFormats.ENTITY_ELEMENT);
	private static final int MODEL_MID_BLOCK = DefaultVertexFormat.NEW_ENTITY.getOffset(IrisVertexFormats.MID_BLOCK_ELEMENT);

	private final int FORMAT_COLOR, FORMAT_ENTITY_ID, FORMAT_UV, FORMAT_LIGHT, FORMAT_NORMAL, FORMAT_TANGENT, FORMAT_MID_COORD;

	public ModelToEntityVertexSerializer(VertexFormat entityFormat) {
		FORMAT = entityFormat;
		FORMAT_COLOR = entityFormat.getOffset(VertexFormatElement.COLOR);
		FORMAT_ENTITY_ID = entityFormat.getOffset(IrisVertexFormats.ENTITY_ID_ELEMENT);
		FORMAT_UV = entityFormat.getOffset(VertexFormatElement.UV);
		FORMAT_LIGHT = entityFormat.getOffset(VertexFormatElement.UV2);
		FORMAT_NORMAL = entityFormat.getOffset(VertexFormatElement.NORMAL);
		FORMAT_TANGENT = entityFormat.getOffset(IrisVertexFormats.TANGENT_ELEMENT);
		FORMAT_MID_COORD = entityFormat.getOffset(IrisVertexFormats.MID_TEXTURE_ELEMENT);
	}

	@Override
	public void serialize(long src, long dst, int vertexCount) {
		// Only accept quads, to be safe
		int quadCount = vertexCount / 4;
		for (int i = 0; i < quadCount; i++) {
			int normal = MemoryUtil.memGetInt(src + 32);
			int tangent = NormalHelper.computeTangent(null, NormI8.unpackX(normal), NormI8.unpackY(normal), NormI8.unpackZ(normal), MemoryUtil.memGetFloat(src), MemoryUtil.memGetFloat(src + 4), MemoryUtil.memGetFloat(src + 8), MemoryUtil.memGetFloat(src + 16), MemoryUtil.memGetFloat(src + 20),
				MemoryUtil.memGetFloat(src + EntityVertex.STRIDE), MemoryUtil.memGetFloat(src + 4 + EntityVertex.STRIDE), MemoryUtil.memGetFloat(src + 8 + EntityVertex.STRIDE), MemoryUtil.memGetFloat(src + 16 + EntityVertex.STRIDE), MemoryUtil.memGetFloat(src + 20 + EntityVertex.STRIDE),
				MemoryUtil.memGetFloat(src + EntityVertex.STRIDE + EntityVertex.STRIDE), MemoryUtil.memGetFloat(src + 4 + EntityVertex.STRIDE + EntityVertex.STRIDE), MemoryUtil.memGetFloat(src + 8 + EntityVertex.STRIDE + EntityVertex.STRIDE), MemoryUtil.memGetFloat(src + 16 + EntityVertex.STRIDE + EntityVertex.STRIDE), MemoryUtil.memGetFloat(src + 20 + EntityVertex.STRIDE + EntityVertex.STRIDE));
			float midU = 0, midV = 0;
			for (int vertex = 0; vertex < 4; vertex++) {
				midU += MemoryUtil.memGetFloat(src + FORMAT_UV + (EntityVertex.STRIDE * vertex));
				midV += MemoryUtil.memGetFloat(src + FORMAT_UV + 4 + (EntityVertex.STRIDE * vertex));
			}

			midU /= 4;
			midV /= 4;

			for (int j = 0; j < 4; j++) {
				MemoryIntrinsics.copyMemory(src, dst, 36);
				MemoryUtil.memPutShort(dst + FORMAT_ENTITY_ID, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
				MemoryUtil.memPutShort(dst + FORMAT_ENTITY_ID + 2, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
				MemoryUtil.memPutShort(dst + FORMAT_ENTITY_ID + 4, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());
				MemoryUtil.memPutFloat(dst + FORMAT_MID_COORD, midU);
				MemoryUtil.memPutFloat(dst + FORMAT_MID_COORD + 4, midV);
				MemoryUtil.memPutInt(dst + FORMAT_TANGENT, tangent);

				src += EntityVertex.STRIDE;
				dst += FORMAT.getVertexSize();
			}
		}
	}
}
