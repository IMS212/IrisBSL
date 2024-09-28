package net.irisshaders.iris.vertices.sodium;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.caffeinemc.mods.sodium.api.vertex.attributes.common.*;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.lwjgl.system.MemoryUtil;

public final class IrisEntityVertex {
	public final VertexFormat FORMAT;

	public final int STRIDE;
	public final long SCRATCH_BUFFER;

	private final int OFFSET_POSITION = 0;
	private final int OFFSET_COLOR;
	private final int OFFSET_TEXTURE;
	private final int OFFSET_OVERLAY;
	private final int OFFSET_LIGHT;
	private final int OFFSET_NORMAL;
	private final int OFFSET_TANGENT;
	private final int OFFSET_MID_COORD;
	private final int OFFSET_ENTITY_ID;
	private final int OFFSET_VELOCITY;

	public IrisEntityVertex(VertexFormat currentFormat) {
		SCRATCH_BUFFER = MemoryUtil.nmemAlignedAlloc(64, 6 * 8 * currentFormat.getVertexSize());
		OFFSET_COLOR = currentFormat.getOffset(VertexFormatElement.COLOR);
		OFFSET_TEXTURE = currentFormat.getOffset(VertexFormatElement.UV0);
		OFFSET_OVERLAY = currentFormat.getOffset(VertexFormatElement.UV1);
		OFFSET_LIGHT = currentFormat.getOffset(VertexFormatElement.UV2);
		System.out.println(currentFormat);
		OFFSET_NORMAL = currentFormat.getOffset(VertexFormatElement.NORMAL);
		OFFSET_TANGENT = currentFormat.getOffset(IrisVertexFormats.TANGENT_ELEMENT);
		OFFSET_MID_COORD = currentFormat.getOffset(IrisVertexFormats.MID_TEXTURE_ELEMENT);
		OFFSET_ENTITY_ID = currentFormat.getOffset(IrisVertexFormats.ENTITY_ID_ELEMENT);
		OFFSET_VELOCITY = currentFormat.getOffset(IrisVertexFormats.VELOCITY_ELEMENT);
		STRIDE = currentFormat.getVertexSize();
		FORMAT = currentFormat;
	}

	public void write(long ptr,
							 float x, float y, float z, float velocityX, float velocityY, float velocityZ, int color, float u, float v, int overlay, int light, int normal, int tangent,
							 float midU, float midV) {
		PositionAttribute.put(ptr + OFFSET_POSITION, x, y, z);
		ColorAttribute.set(ptr + OFFSET_COLOR, color);
		TextureAttribute.put(ptr + OFFSET_TEXTURE, u, v);
		OverlayAttribute.set(ptr + OFFSET_OVERLAY, overlay);
		LightAttribute.set(ptr + OFFSET_LIGHT, light);
		if (OFFSET_NORMAL != -1) {
			NormalAttribute.set(ptr + OFFSET_NORMAL, normal);
		}

		if (OFFSET_TANGENT != -1) {
			MemoryUtil.memPutInt(ptr + OFFSET_TANGENT, tangent);
		}

		if (OFFSET_MID_COORD != -1) {
			MemoryUtil.memPutFloat(ptr + OFFSET_MID_COORD, midU);
			MemoryUtil.memPutFloat(ptr + OFFSET_MID_COORD + 4, midV);
		}

		MemoryUtil.memPutShort(ptr + OFFSET_ENTITY_ID, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity());
		MemoryUtil.memPutShort(ptr + OFFSET_ENTITY_ID + 2, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
		MemoryUtil.memPutShort(ptr + OFFSET_ENTITY_ID + 4, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem());

		if (OFFSET_VELOCITY != -1) {
			MemoryUtil.memPutFloat(ptr + OFFSET_VELOCITY, velocityX);
			MemoryUtil.memPutFloat(ptr + OFFSET_VELOCITY + 4, velocityY);
			MemoryUtil.memPutFloat(ptr + OFFSET_VELOCITY + 8, velocityZ);
		}
	}
}
