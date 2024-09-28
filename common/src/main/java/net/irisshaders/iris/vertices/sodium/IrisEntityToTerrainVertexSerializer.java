package net.irisshaders.iris.vertices.sodium;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.lwjgl.system.MemoryUtil;

public class IrisEntityToTerrainVertexSerializer implements VertexSerializer {
	private final VertexFormat FORMAT;

	private static final int TERRAIN_COLOR = IrisVertexFormats.TERRAIN.getOffset(VertexFormatElement.COLOR);
	private static final int TERRAIN_UV = IrisVertexFormats.TERRAIN.getOffset(VertexFormatElement.UV0);
	private static final int TERRAIN_LIGHT = IrisVertexFormats.TERRAIN.getOffset(VertexFormatElement.UV2);
	private static final int TERRAIN_NORMAL = IrisVertexFormats.TERRAIN.getOffset(VertexFormatElement.NORMAL);
	private static final int TERRAIN_TANGENT = IrisVertexFormats.TERRAIN.getOffset(IrisVertexFormats.TANGENT_ELEMENT);
	private static final int TERRAIN_MID_COORD = IrisVertexFormats.TERRAIN.getOffset(IrisVertexFormats.MID_TEXTURE_ELEMENT);
	private static final int TERRAIN_BLOCK_ID = IrisVertexFormats.TERRAIN.getOffset(IrisVertexFormats.ENTITY_ELEMENT);
	private static final int TERRAIN_MID_BLOCK = IrisVertexFormats.TERRAIN.getOffset(IrisVertexFormats.MID_BLOCK_ELEMENT);

	private final int FORMAT_COLOR, FORMAT_UV, FORMAT_LIGHT, FORMAT_NORMAL, FORMAT_TANGENT, FORMAT_MID_COORD;

	public IrisEntityToTerrainVertexSerializer(VertexFormat entityFormat) {
		FORMAT = entityFormat;
		FORMAT_COLOR = entityFormat.getOffset(VertexFormatElement.COLOR);
		FORMAT_UV = entityFormat.getOffset(VertexFormatElement.UV);
		FORMAT_LIGHT = entityFormat.getOffset(VertexFormatElement.UV2);
		FORMAT_NORMAL = entityFormat.getOffset(VertexFormatElement.NORMAL);
		FORMAT_TANGENT = entityFormat.getOffset(IrisVertexFormats.TANGENT_ELEMENT);
		FORMAT_MID_COORD = entityFormat.getOffset(IrisVertexFormats.MID_TEXTURE_ELEMENT);
	}

	@Override
	public void serialize(long src, long dst, int vertexCount) {
		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
			// Position
			MemoryUtil.memPutFloat(dst, MemoryUtil.memGetFloat(src));
			MemoryUtil.memPutFloat(dst + 4, MemoryUtil.memGetFloat(src + 4L));
			MemoryUtil.memPutFloat(dst + 8, MemoryUtil.memGetFloat(src + 8L));

			// Color
			MemoryUtil.memPutInt(dst + TERRAIN_COLOR, MemoryUtil.memGetInt(src + FORMAT_COLOR));

			// UV
			MemoryUtil.memPutFloat(dst + TERRAIN_UV, MemoryUtil.memGetFloat(src + FORMAT_UV));
			MemoryUtil.memPutFloat(dst + TERRAIN_UV + 4, MemoryUtil.memGetFloat(src + FORMAT_UV + 4));

			// Light
			MemoryUtil.memPutInt(dst + TERRAIN_LIGHT, MemoryUtil.memGetInt(src + FORMAT_LIGHT));

			// The rest
			MemoryUtil.memPutInt(dst + TERRAIN_NORMAL, MemoryUtil.memGetInt(src + FORMAT_NORMAL));
			MemoryUtil.memPutInt(dst + TERRAIN_TANGENT, MemoryUtil.memGetInt(src + FORMAT_TANGENT));
			MemoryUtil.memPutFloat(dst + TERRAIN_MID_COORD, MemoryUtil.memGetInt(src + FORMAT_MID_COORD));
			MemoryUtil.memPutFloat(dst + TERRAIN_MID_COORD + 4, MemoryUtil.memGetInt(src + FORMAT_MID_COORD + 4));

			// Filling in the blanks
			MemoryUtil.memPutInt(dst + TERRAIN_MID_BLOCK, 0);
			MemoryUtil.memPutInt(dst + TERRAIN_BLOCK_ID, 0);

			src += FORMAT.getVertexSize();
			dst += IrisVertexFormats.TERRAIN.getVertexSize();
		}

	}
}
