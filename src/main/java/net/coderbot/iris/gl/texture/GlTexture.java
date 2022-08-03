package net.coderbot.iris.gl.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class GlTexture extends GlResource {
	private final TextureType target;

	public GlTexture(TextureType target, int sizeX, int sizeY, int sizeZ, int internalFormat, int format, int pixelType, byte[] pixels) {
		super(GlStateManager._genTexture());
		IrisRenderSystem.bindTexture(target.getGlType(), getGlId());

		ByteBuffer buffer = MemoryUtil.memAlloc(pixels.length);
		buffer.put(pixels);
		buffer.flip();
		target.apply(sizeX, sizeY, sizeZ, internalFormat, format, pixelType, buffer);
		MemoryUtil.memFree(buffer);

		IrisRenderSystem.bindTexture(target.getGlType(), 0);

		this.target = target;
	}

	public TextureType getTarget() {
		return target;
	}

	public void bind() {
		IrisRenderSystem.bindTexture(target.getGlType(), getGlId());
	}

	public int getId() {
		return getGlId();
	}

	@Override
	protected void destroyInternal() {
		GlStateManager._deleteTexture(getGlId());
	}
}
