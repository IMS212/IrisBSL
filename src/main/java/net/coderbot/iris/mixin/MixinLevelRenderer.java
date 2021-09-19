package net.coderbot.iris.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.coderbot.iris.HorizonRenderer;
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.WorldRenderingPhase;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(LevelRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinLevelRenderer {
	@Shadow
	@Final
	private Minecraft minecraft;
	private static final String PROFILER_SWAP = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V";
	private static final String RENDER = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V";
	private static final String CLEAR = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V";
	private static final String RENDER_SKY = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/math/Matrix4f;FLjava/lang/Runnable;)V";
	private static final String RENDER_LAYER = "renderLayer(Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/util/math/MatrixStack;DDD)V";
	private static final String RENDER_CLOUDS = "Lnet/minecraft/client/render/WorldRenderer;renderClouds(Lnet/minecraft/client/util/math/MatrixStack;FDDD)V";
	private static final String RENDER_WEATHER = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V";
	private static final String RENDER_WORLD_BORDER = "Lnet/minecraft/client/render/WorldRenderer;renderWorldBorder(Lnet/minecraft/client/render/Camera;)V";

	@Unique
	private HorizonRenderer horizonRenderer = new HorizonRenderer();

	@Unique
	private boolean skyTextureEnabled;

	@Unique
	private int previousViewDistance;

	@Unique
	private WorldRenderingPipeline pipeline;

	@Inject(method = RENDER, at = @At("HEAD"))
	private void iris$beginLevelRender(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo callback) {
		if (Iris.isSodiumInvalid()) {
			throw new IllegalStateException("An invalid version of Sodium is installed, and the warning screen somehow" +
					" didn't work. This is a bug! Please report it to the Iris developers.");
		}

		CapturedRenderingState.INSTANCE.setGbufferModelView(poseStack.last().pose());
		CapturedRenderingState.INSTANCE.setTickDelta(tickDelta);
		if(previousViewDistance != minecraft.options.renderDistance) {
			horizonRenderer.close();
			horizonRenderer = new HorizonRenderer();
			previousViewDistance = minecraft.options.renderDistance;
		}
		pipeline = Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());
		pipeline.beginLevelRendering();
		pipeline.setPhase(WorldRenderingPhase.OTHER);
	}

	// Inject a bit early so that we can end our rendering before mods like VoxelMap (which inject at RETURN)
	// render their waypoint beams.
	@Inject(method = RENDER, at = @At(value = "RETURN", shift = At.Shift.BEFORE))
	private void iris$endLevelRender(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo callback) {
		Minecraft.getInstance().getProfiler().popPush("iris_final");
		pipeline.finalizeLevelRendering();
		pipeline.setPhase(WorldRenderingPhase.NOT_RENDERING_WORLD);
		pipeline = null;
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZIZ)V", shift = At.Shift.AFTER))
	private void iris$renderTerrainShadows(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.renderShadows((LevelRendererAccessor) this, camera);
	}

	// TODO(21w10a): Deal with render hooks
	/*@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = CLEAR))
	private void iris$beforeClear(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.CLEAR);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = CLEAR, shift = At.Shift.AFTER))
	private void iris$afterClear(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.CLEAR);
	}*/

	@Inject(method = RENDER, at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=sky"))
	private void iris$beginSky(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.setPhase(WorldRenderingPhase.SKY);
	}

	// TODO(21w10a): Restore sky render hooks
	/*
	@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableTexture()V"))
	private void iris$renderSky$disableTexture(PoseStack poseStack, float tickDelta, CallbackInfo callback) {
		if (skyTextureEnabled) {
			skyTextureEnabled = false;
			pipeline.pushProgram(GbufferProgram.SKY_BASIC);
		}
	}*/

	@Inject(method = RENDER_SKY,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogRenderer;levelFogColor()V"))
	private void iris$renderSky$drawHorizon(PoseStack poseStack, Matrix4f projectionMatrix, float f, Runnable runnable, CallbackInfo callback) {
		RenderSystem.depthMask(false);

		Vec3 fogColor = CapturedRenderingState.INSTANCE.getFogColor();
		RenderSystem.setShaderColor((float) fogColor.x, (float) fogColor.y, (float) fogColor.z, 1.0F);

		horizonRenderer.renderHorizon(poseStack.last().pose().copy(), projectionMatrix.copy(), GameRenderer.getPositionShader());

		RenderSystem.depthMask(true);
	}

	@Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getTimeOfDay(F)F"),
			slice = @Slice(from = @At(value = "FIELD", target = "Lcom/mojang/math/Vector3f;YP:Lcom/mojang/math/Vector3f;")))
	private void iris$renderSky$tiltSun(PoseStack poseStack, Matrix4f projectionMatrix, float f, Runnable runnable, CallbackInfo callback) {
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(pipeline.getSunPathRotation()));
	}

	/*@Inject(method = RENDER_SKY, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableTexture()V"))
	private void iris$renderSky$enableTexture(PoseStack poseStack, float tickDelta, CallbackInfo callback) {
		if (!skyTextureEnabled) {
			skyTextureEnabled = true;
			pipeline.popProgram(GbufferProgram.SKY_BASIC);
		}
	}*/

	// TODO(21w10a): Deal with render hooks
	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_SKY, shift = At.Shift.AFTER))
	private void iris$endSky(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo callback) {
		// After the sky has rendered, there isn't a clear phase.
		pipeline.setPhase(WorldRenderingPhase.OTHER);
	}

	/*@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_CLOUDS))
	private void iris$beginClouds(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.CLOUDS);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_CLOUDS, shift = At.Shift.AFTER))
	private void iris$endClouds(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.CLOUDS);
	}*/

	@Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
	private void iris$maybeRemoveClouds(PoseStack poseStack, Matrix4f matrix4f, float f, double d, double e, double g, CallbackInfo ci) {
		if (!pipeline.shouldRenderClouds()) {
			ci.cancel();
		}
	}


	// TODO(21w10a): Restore render layer hooks
	/*
	@Inject(method = RENDER_LAYER, at = @At("HEAD"))
	private void iris$beginTerrainLayer(RenderType renderType, PoseStack poseStack, double cameraX, double cameraY, double cameraZ, CallbackInfo callback) {
		if (renderType == RenderType.solid() || renderType == RenderType.cutout() || renderType == RenderType.cutoutMipped()) {
			pipeline.pushProgram(GbufferProgram.TERRAIN);
		} else if (renderType == RenderType.translucent() || renderType == RenderType.tripwire()) {
			pipeline.pushProgram(GbufferProgram.TRANSLUCENT_TERRAIN);
		} else {
			throw new IllegalStateException("[Iris] Unexpected terrain layer: " + renderType);
		}
	}

	@Inject(method = RENDER_LAYER, at = @At("RETURN"))
	private void iris$endTerrainLayer(RenderType renderType, PoseStack poseStack, double cameraX, double cameraY, double cameraZ, CallbackInfo callback) {
		if (renderType == RenderType.solid() || renderType == RenderType.cutout() || renderType == RenderType.cutoutMipped()) {
			pipeline.popProgram(GbufferProgram.TERRAIN);
		} else if (renderType == RenderType.translucent() || renderType == RenderType.tripwire()) {
			pipeline.popProgram(GbufferProgram.TRANSLUCENT_TERRAIN);
		} else {
			throw new IllegalStateException("[Iris] Unexpected terrain layer: " + renderType);
		}
	}*/

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_WEATHER))
	private void iris$beginWeather(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.setPhase(WorldRenderingPhase.WEATHER);
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = RENDER_WEATHER, shift = At.Shift.AFTER))
	private void iris$endWeather(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo callback) {
		pipeline.setPhase(WorldRenderingPhase.OTHER);
	}

	// TODO(21w10a): Deal with render hooks
	/*@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_WORLD_BOUNDS))
	private void iris$beginWorldBorder(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo callback) {
		pipeline.pushProgram(GbufferProgram.TEXTURED_LIT);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = RENDER_WORLD_BOUNDS, shift = At.Shift.AFTER))
	private void iris$endWorldBorder(PoseStack poseStack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection, CallbackInfo callback) {
		pipeline.popProgram(GbufferProgram.TEXTURED_LIT);
	}*/

	@Inject(method = RENDER, at = @At(value = "CONSTANT", args = "stringValue=translucent"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$beginTranslucents(PoseStack poseStack, float tickDelta, long limitTime,
										boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
										LightTexture lightTexture, Matrix4f matrix4f,
										CallbackInfo ci, ProfilerFiller profiler, Vec3 vec3d, double d, double e, double f,
										Matrix4f matrix4f2, boolean bl, Frustum frustum2, boolean bl3,
										MultiBufferSource.BufferSource bufferSource) {
		profiler.popPush("iris_entity_draws");
		bufferSource.endBatch();

		profiler.popPush("iris_pre_translucent");
		pipeline.beginTranslucents();
	}
}
