package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.rendertarget.Blaze3dRenderTargetExt;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows Iris to detect when the depth texture was re-created, so we can re-attach it
 * to the shader framebuffers. See DeferredWorldRenderingPipeline and RenderTargets.
 */
@Mixin(RenderTarget.class)
public abstract class MixinRenderTarget implements Blaze3dRenderTargetExt {
	@Shadow
	private int depthBufferId;

	@Shadow
	public int viewWidth;
	@Shadow
	public int viewHeight;
	@Shadow
	public int width;
	@Shadow
	public int height;
	@Shadow
	public int frameBufferId;
	@Shadow
	protected int colorTextureId;
	@Shadow
	@Final
	public boolean useDepth;

	@Shadow
	public abstract void setFilterMode(int pRenderTarget0);

	@Shadow
	public abstract void checkStatus();

	@Shadow
	public abstract void clear(boolean pRenderTarget0);

	@Shadow
	public abstract void unbindRead();

	private int iris$depthBufferVersion;
	private int iris$colorBufferVersion;

	@Inject(method = "destroyBuffers()V", at = @At("HEAD"))
	private void iris$onDestroyBuffers(CallbackInfo ci) {
		iris$depthBufferVersion++;
		iris$colorBufferVersion++;
	}

	@Override
	public int iris$getDepthBufferVersion() {
		return iris$depthBufferVersion;
	}

	@Override
	public int iris$getColorBufferVersion() {
		return iris$colorBufferVersion;
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public void createBuffers(int pRenderTarget0, int pInt1, boolean pBoolean2) {
		RenderSystem.assertOnRenderThreadOrInit();
		int lvInt4 = RenderSystem.maxSupportedTextureSize();
		if (pRenderTarget0 <= 0 || pRenderTarget0 > lvInt4 || pInt1 <= 0 || pInt1 > lvInt4) {
			throw new IllegalArgumentException("Window " + pRenderTarget0 + "x" + pInt1 + " size out of bounds (max. size: " + lvInt4 + ")");
		}
		this.viewWidth = pRenderTarget0;
		this.viewHeight = pInt1;
		this.width = pRenderTarget0;
		this.height = pInt1;
		this.frameBufferId = GlStateManager.glGenFramebuffers();
		this.colorTextureId = IrisRenderSystem.createTexture(GL30C.GL_TEXTURE_2D);
		if (this.useDepth) {
			this.depthBufferId = IrisRenderSystem.createTexture(GL30C.GL_TEXTURE_2D);
			GlStateManager._bindTexture(this.depthBufferId);
			GlStateManager._texParameter(3553, 10241, 9728);
			GlStateManager._texParameter(3553, 10240, 9728);
			GlStateManager._texParameter(3553, 34892, 0);
			GlStateManager._texParameter(3553, 10242, 33071);
			GlStateManager._texParameter(3553, 10243, 33071);
			GL45C.glTextureStorage2D(depthBufferId, 1, GL45C.GL_DEPTH_COMPONENT16, width, height);
		}
		this.setFilterMode(9728);
		GlStateManager._bindTexture(this.colorTextureId);
		GlStateManager._texParameter(3553, 10242, 33071);
		GlStateManager._texParameter(3553, 10243, 33071);
		GL45C.glTextureStorage2D(colorTextureId, 1, GL45C.GL_RGBA8, width, height);
		GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
		GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, this.colorTextureId, 0);
		if (this.useDepth) {
			GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, this.depthBufferId, 0);
		}
		this.checkStatus();
		this.clear(pBoolean2);
		this.unbindRead();
	}
}
