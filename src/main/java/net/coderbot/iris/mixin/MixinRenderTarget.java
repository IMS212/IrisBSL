package net.coderbot.iris.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.coderbot.iris.rendertarget.Blaze3dRenderTargetExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.texture.DepthBufferFormat;
import net.coderbot.iris.rendertarget.Blaze3dRenderTargetExt;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
/**
 * Allows Iris to detect when the depth texture was re-created, so we can re-attach it
 * to the shader framebuffers. See DeferredWorldRenderingPipeline and RenderTargets.
 */
@Mixin(RenderTarget.class)
public class MixinRenderTarget implements Blaze3dRenderTargetExt {
	@Shadow
	private int depthBufferId;

	private int iris$depthBufferVersion;
	private int iris$colorBufferVersion;

	@Inject(method = "destroyBuffers()V", at = @At("HEAD"))
	private void iris$onDestroyBuffers(CallbackInfo ci) {
		iris$depthBufferVersion++;
		iris$colorBufferVersion++;
	}

	@Redirect(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;glGenRenderbuffers()I"))
	private int genTexture() {
		return GlStateManager._genTexture();
	}

	@Redirect(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glBindRenderbuffer(II)V"))
	private void bind(int i, int j) {
		GlStateManager._bindTexture(j);
	}

	@Redirect(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glRenderbufferStorage(IIII)V"))
	private void renderbufferStorage(int i, int j, int width, int height) {
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
		RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);
		DepthBufferFormat format = DepthBufferFormat.DEPTH;
		GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, format.getGlInternalFormat(), width, height, 0,
			format.getGlType(), format.getGlFormat(), null);
	}

	@Redirect(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferRenderbuffer(IIII)V"))
	private void frameBuffer(int i, int j, int k, int l) {
		GlStateManager._glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL30C.GL_TEXTURE_2D, l, 0);
	}


	@Override
	public int iris$getDepthBufferVersion() {
		return iris$depthBufferVersion;
	}

	@Override
	public int iris$getColorBufferVersion() {
		return iris$colorBufferVersion;
	}
}
