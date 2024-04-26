package net.irisshaders.iris.shadows;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.GlResource;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.texture.DepthBufferFormat;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL43C;

public class ArrayDepthTexture extends GlResource {
	public ArrayDepthTexture(String name, int width, int height, int depth, DepthBufferFormat format) {
		super(IrisRenderSystem.createTexture(GL33C.GL_TEXTURE_2D_ARRAY));
		int texture = getGlId();

		resize(width, height, depth, format);
		GLDebug.nameObject(GL43C.GL_TEXTURE, texture, name);

		IrisRenderSystem.texParameteri(texture, GL33C.GL_TEXTURE_2D_ARRAY, GL33C.GL_TEXTURE_MIN_FILTER, GL33C.GL_NEAREST);
		IrisRenderSystem.texParameteri(texture, GL33C.GL_TEXTURE_2D_ARRAY, GL33C.GL_TEXTURE_MAG_FILTER, GL33C.GL_NEAREST);
		IrisRenderSystem.texParameteri(texture, GL33C.GL_TEXTURE_2D_ARRAY, GL33C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
		IrisRenderSystem.texParameteri(texture, GL33C.GL_TEXTURE_2D_ARRAY, GL33C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);

		GlStateManager._bindTexture(0);
	}

	void resize(int width, int height, int depth, DepthBufferFormat format) {
		IrisRenderSystem.texImage3D(getTextureId(), GL33C.GL_TEXTURE_2D_ARRAY, 0, format.getGlInternalFormat(), width, height, depth, 0,
			format.getGlType(), format.getGlFormat(), null);
	}

	public int getTextureId() {
		return getGlId();
	}

	@Override
	protected void destroyInternal() {
		GlStateManager._deleteTexture(getGlId());
	}
}
