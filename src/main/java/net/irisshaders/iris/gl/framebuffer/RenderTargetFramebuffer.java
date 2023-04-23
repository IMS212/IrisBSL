package net.irisshaders.iris.gl.framebuffer;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.booleans.BooleanReferencePair;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.irisshaders.iris.gl.GlResource;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.texture.DepthBufferFormat;
import net.irisshaders.iris.rendertarget.RenderTarget;
import net.irisshaders.iris.texture.TextureInfoCache;
import org.lwjgl.opengl.GL30C;

public class RenderTargetFramebuffer extends GlFramebuffer {
	private Int2ObjectMap<BooleanReferencePair<RenderTarget>> targets = new Int2ObjectArrayMap<>();

	public void addColorAttachment(int index, RenderTarget target, boolean alt) {
		int fb = getGlId();

		IrisRenderSystem.framebufferTexture2D(fb, GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0 + index, GL30C.GL_TEXTURE_2D, alt ? target.getAltTexture() : target.getMainTexture(), 0);
		targets.put(index, BooleanReferencePair.of(alt, target));
	}

	public void updateTargets() {
		targets.forEach((index, pair) -> {
			int fb = getGlId();

			int tex = pair.leftBoolean() ? pair.right().getAltTexture() : pair.right().getMainTexture();
			attachments.replace((int) index, tex);
			IrisRenderSystem.framebufferTexture2D(fb, GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0 + index, GL30C.GL_TEXTURE_2D, tex, 0);
		});
	}
}
