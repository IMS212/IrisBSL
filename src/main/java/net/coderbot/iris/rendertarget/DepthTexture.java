package net.coderbot.iris.rendertarget;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.GlResource;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL32C;

public class DepthTexture extends GlResource {
	public DepthTexture(int width, int height) {
		super(GlStateManager._genTexture());
		GlStateManager._bindTexture(getGlId());

		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);
		resize(width, height);

		GlStateManager._bindTexture(0);
	}

	void resize(int width, int height) {
		GlStateManager._bindTexture(getGlId());

		// TODO: Is there a better format to use? This is the easiest format for centerDepthSmooth to process async.
		GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, GL32C.GL_DEPTH_COMPONENT32F, width, height, 0, GL11C.GL_DEPTH_COMPONENT, GL11C.GL_FLOAT, null);

		GlStateManager._bindTexture(0);
	}

	public int getTextureId() {
		return getGlId();
	}

	@Override
	protected void destroyInternal() {
		GlStateManager._deleteTexture(getGlId());
	}
}
