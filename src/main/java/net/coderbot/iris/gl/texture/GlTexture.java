package net.coderbot.iris.gl.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class GlTexture extends GlResource {
	private final TextureType target;

	public GlTexture(TextureType target, boolean blur, boolean clamp, int sizeX, int sizeY, int sizeZ, InternalTextureFormat internalFormat, PixelFormat format, PixelType pixelType, byte[] pixels) {
		super(GlStateManager._genTexture());
		IrisRenderSystem.bindTexture(target.getGlType(), getGlId());
		Iris.logger.warn("Creating texture: " + target + " " + sizeX + "x" + sizeY + "y" + sizeZ + "z " + internalFormat.name() + " " + format.name() + " " + pixelType.name());

		ByteBuffer buffer = MemoryUtil.memAlloc(pixels.length);
		buffer.put(pixels);
		buffer.flip();
		target.apply(getGlId(), sizeX, sizeY, sizeZ, internalFormat.getGlFormat(), format.getGlFormat(), pixelType.getGlFormat(), buffer);
		MemoryUtil.memFree(buffer);
		this.target = target;

		if (blur) {
			RenderSystem.texParameter(getTarget().getGlType(), GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
			RenderSystem.texParameter(getTarget().getGlType(), GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
		}

		if (clamp) {
			RenderSystem.texParameter(getTarget().getGlType(), GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
			RenderSystem.texParameter(getTarget().getGlType(), GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);
			RenderSystem.texParameter(getTarget().getGlType(), GL30C.GL_TEXTURE_WRAP_R, GL13C.GL_CLAMP_TO_EDGE);
		}
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
