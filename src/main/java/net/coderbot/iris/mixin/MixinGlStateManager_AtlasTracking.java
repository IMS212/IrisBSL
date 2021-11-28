package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.samplers.TextureAtlasTracker;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

@Mixin(GlStateManager.class)
public class MixinGlStateManager_AtlasTracking {
	@Shadow(remap = false)
	private static int activeTexture;

	@Shadow(remap = false)
	@Final
	private static GlStateManager.TextureState[] TEXTURES;

	@Shadow
	@Final
	private static GlStateManager.BlendState BLEND;

	@Inject(method = "_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", at = @At("HEAD"), remap = false)
	private static void iris$onTexImage2D(int target, int level, int internalformat, int width, int height, int border,
										  int format, int type, @Nullable IntBuffer pixels, CallbackInfo ci) {
		TextureAtlasTracker.INSTANCE.trackTexImage2D(TEXTURES[activeTexture].binding, level, width, height);
	}

	@Inject(method = "_deleteTexture(I)V", at = @At("HEAD"), remap = false)
	private static void iris$onDeleteTexture(int id, CallbackInfo ci) {
		TextureAtlasTracker.INSTANCE.trackDeleteTextures(id);
	}

	@Inject(method = "_deleteTextures([I)V", at = @At("HEAD"), remap = false)
	private static void iris$onDeleteTextures(int[] ids, CallbackInfo ci) {
		for (int id : ids) {
			TextureAtlasTracker.INSTANCE.trackDeleteTextures(id);
		}
	}

	@Inject(method = "_blendFuncSeparate", at = @At("HEAD"), remap = false, cancellable = true)
	private static void _fdsfdsblendFuncSeparate(int i, int j, int k, int l, CallbackInfo ci) {
		if (BlockRenderingSettings.INSTANCE.getCurrentBlendFunc() != null) {
			ci.cancel();
		} else {
			return;
		}
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		int[] func = BlockRenderingSettings.INSTANCE.getCurrentBlendFunc();
		if (func[0] != BLEND.srcRgb || func[1] != BLEND.dstRgb || func[2] != BLEND.srcAlpha || func [3] != BLEND.dstAlpha) {
			BLEND.srcRgb = func[0];
			BLEND.dstRgb = func[1];
			BLEND.srcAlpha = func[2];
			BLEND.dstAlpha = func[3];
			GL30.glBlendFuncSeparate(func[0], func[1], func[2], func[3]);
		}
	}

	@Inject(method = "_blendFunc", at = @At("HEAD"), remap = false, cancellable = true)
	private static void _fdsfdsblendFunc(int i, int j, CallbackInfo ci) {
		if (BlockRenderingSettings.INSTANCE.getCurrentBlendFunc() != null) {
			ci.cancel();
		} else {
			return;
		}
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		int[] func = BlockRenderingSettings.INSTANCE.getCurrentBlendFunc();
		if (func[0] != BLEND.srcRgb || func[1] != BLEND.dstRgb || func[2] != BLEND.srcAlpha || func [3] != BLEND.dstAlpha) {
			BLEND.srcRgb = func[0];
			BLEND.dstRgb = func[1];
			BLEND.srcAlpha = func[2];
			BLEND.dstAlpha = func[3];
			GL30.glBlendFuncSeparate(func[0], func[1], func[2], func[3]);
		}
	}
}
