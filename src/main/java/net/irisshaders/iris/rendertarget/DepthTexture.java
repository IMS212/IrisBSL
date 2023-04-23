package net.irisshaders.iris.rendertarget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.gl.GlResource;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.texture.DepthBufferFormat;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL45C;

public class DepthTexture extends GlResource {
	public DepthTexture(int width, int height, DepthBufferFormat format) {
		super(IrisRenderSystem.createTexture(GL11C.GL_TEXTURE_2D));
		int texture = getGlId();

		GL45C.glTextureStorage2D(getTextureId(), 1, format.getGlInternalFormat(), width, height);

		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
		IrisRenderSystem.texParameteri(texture, GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);

		GlStateManager._bindTexture(0);
	}

	void resize(int width, int height, DepthBufferFormat format) {
		GlStateManager._deleteTexture(getGlId());

		changeId(IrisRenderSystem.createTexture(GL11C.GL_TEXTURE_2D));

		GL45C.glTextureStorage2D(getTextureId(), 1, format.getGlInternalFormat(), width, height);
	}

	public int getTextureId() {
		return getGlId();
	}

	@Override
	protected void destroyInternal() {
		GlStateManager._deleteTexture(getGlId());
	}
}
