package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.mixin.GameRendererAccessor;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.GameType;
import org.lwjgl.opengl.*;

public class HandRenderer {
	public static final HandRenderer INSTANCE = new HandRenderer();

	private static boolean ACTIVE;
	public boolean mainHandTranslucent;
	public boolean offHandTranslucent;
	public boolean isRenderingComposite;

	private void setupGlState(GameRenderer gameRenderer, PoseStack poseStack, float tickDelta, Camera camera) {
        final PoseStack.Pose pose = poseStack.last();

		// We have a inject in getProjectionMatrix to scale the matrix so the hand doesn't clip through blocks.
		gameRenderer.resetProjectionMatrix(gameRenderer.getProjectionMatrix(camera, tickDelta, false));

        pose.pose().setIdentity();
        pose.normal().setIdentity();

		if(Minecraft.getInstance().options.bobView) {
			((GameRendererAccessor)gameRenderer).invokeBobView(poseStack, tickDelta);
		}
	}

	private boolean canRender(Camera camera, GameRenderer gameRenderer) {
		return !(camera.isDetached() 
			|| !(camera.getEntity() instanceof Player) 
				|| ((GameRendererAccessor)gameRenderer).getPanoramicMode() 
					|| Minecraft.getInstance().options.hideGui 
						|| (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).isSleeping()) 
							|| Minecraft.getInstance().gameMode.getPlayerMode() == GameType.SPECTATOR);
	}

	public void render(RenderBuffers renderBuffers, PoseStack poseStack, float tickDelta, Camera camera, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		if(!canRender(camera, gameRenderer)) {
			return;
		}

		ACTIVE = true;

		if (Minecraft.getInstance().player.getMainHandItem().getItem() instanceof BlockItem) {
			if (ItemBlockRenderTypes.getChunkRenderType(((BlockItem) Minecraft.getInstance().player.getMainHandItem().getItem()).getBlock().defaultBlockState()) == RenderType.translucent() ) {
				mainHandTranslucent = true;
			}
		}

		if (Minecraft.getInstance().player.getOffhandItem().getItem() instanceof BlockItem) {
			if (ItemBlockRenderTypes.getChunkRenderType(((BlockItem) Minecraft.getInstance().player.getOffhandItem().getItem()).getBlock().defaultBlockState()) == RenderType.translucent() ) {
				offHandTranslucent = true;
			}
		}

		poseStack.pushPose();

		Minecraft.getInstance().getProfiler().push("iris_hand");

		setupGlState(gameRenderer, poseStack, tickDelta, camera);

		pipeline.pushProgram((mainHandTranslucent || offHandTranslucent) ? GbufferProgram.HAND_TRANSLUCENT : GbufferProgram.HAND);
		GlStateManager._disableBlend();


		if (mainHandTranslucent || offHandTranslucent) {
			poseStack.pushPose();
			int[] oldDrawBuffers = ((DeferredWorldRenderingPipeline) pipeline).getHandTranslucent().framebufferBeforeTranslucents.getDrawBuffers();
			GL21.glDrawBuffers(new int[]{});

			GL21.glDepthMask(true);
			GlStateManager._enableDepthTest();
			GlStateManager._depthFunc(GL15.GL_ALWAYS);

			//Minecraft.getInstance().getItemInHandRenderer().renderHandsWithItems(tickDelta, poseStack, renderBuffers.bufferSource(), Minecraft.getInstance().player, Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(camera.getEntity(), tickDelta));

			int[] newDrawBuffers = new int[oldDrawBuffers.length];
			int index = 0;
			for (int buffer : oldDrawBuffers) {
				if (buffer >= 8) {
					throw new IllegalArgumentException("Only 8 color attachments are supported, but an attempt was made to write to a color attachment with index " + buffer);
				}

				newDrawBuffers[index++] = GL30C.GL_COLOR_ATTACHMENT0 + buffer;
			}
			GL20.glDrawBuffers(newDrawBuffers);
			poseStack.popPose();

			pipeline.popProgram(GbufferProgram.HAND_TRANSLUCENT);

			Minecraft.getInstance().getProfiler().pop();

			gameRenderer.resetProjectionMatrix(CapturedRenderingState.INSTANCE.getGbufferProjection());

			poseStack.popPose();

			return;
		}

		Minecraft.getInstance().getItemInHandRenderer().renderHandsWithItems(tickDelta, poseStack, renderBuffers.bufferSource(), Minecraft.getInstance().player, Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(camera.getEntity(), tickDelta));

		pipeline.popProgram((mainHandTranslucent || offHandTranslucent) ? GbufferProgram.HAND_TRANSLUCENT : GbufferProgram.HAND);

		Minecraft.getInstance().getProfiler().pop();

		gameRenderer.resetProjectionMatrix(CapturedRenderingState.INSTANCE.getGbufferProjection());

		poseStack.popPose();

		ACTIVE = false;
	}

	public void renderTranslucent(RenderBuffers renderBuffers, PoseStack poseStack, float tickDelta, Camera camera, GameRenderer gameRenderer, WorldRenderingPipeline pipeline) {
		if (mainHandTranslucent || offHandTranslucent) {
			ACTIVE = true;
			isRenderingComposite = true;

			poseStack.pushPose();

			Minecraft.getInstance().getProfiler().push("iris_hand");

			setupGlState(gameRenderer, poseStack, tickDelta, camera);

			pipeline.pushProgram(GbufferProgram.HAND_TRANSLUCENT);
			GlStateManager._depthFunc(515);
			GlStateManager._enableBlend();

			GlStateManager._disableDepthTest();

			//Minecraft.getInstance().getItemInHandRenderer().renderHandsWithItems(tickDelta, poseStack, renderBuffers.bufferSource(), Minecraft.getInstance().player, Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(camera.getEntity(), tickDelta));
			//poseStack.popPose();

			GlStateManager._enableBlend();
			GlStateManager._depthFunc(GL21.GL_LEQUAL);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			Minecraft.getInstance().getItemInHandRenderer().renderHandsWithItems(tickDelta, poseStack, renderBuffers.bufferSource(), Minecraft.getInstance().player, Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(camera.getEntity(), tickDelta));
			pipeline.popProgram(GbufferProgram.HAND_TRANSLUCENT);
			poseStack.popPose();
			Minecraft.getInstance().getProfiler().pop();

			gameRenderer.resetProjectionMatrix(CapturedRenderingState.INSTANCE.getGbufferProjection());



			ACTIVE = false;
			isRenderingComposite = false;
		}

		mainHandTranslucent = false;
		offHandTranslucent = false;
	}

	public static boolean isActive() {
		return ACTIVE;
	}
}
