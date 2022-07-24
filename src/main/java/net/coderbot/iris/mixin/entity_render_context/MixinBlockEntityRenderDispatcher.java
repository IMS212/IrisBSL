package net.coderbot.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.fantastic.WrappingMultiBufferSource;
import net.coderbot.iris.layer.BlockEntityRenderStateShard;
import net.coderbot.iris.layer.OuterWrappedRenderType;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wraps block entity rendering functions in order to create additional render layers
 * that provide context to shaders about what block entity is currently being
 * rendered.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
	private static final String RENDER =
			"render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V";

	private static final String RUN_REPORTED =
			"Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;tryRender(Lnet/minecraft/world/level/block/entity/BlockEntity;Ljava/lang/Runnable;)V";

	// I inject here in the method so that:
	//
	// 1. we can know that some checks we need have already been done
	// 2. if someone cancels this method hopefully it gets cancelled before this point
	@Inject(method = "render", at = @At(value = "INVOKE", target = RUN_REPORTED))
	private void iris$beforeRender(BlockEntity blockEntity, float tickDelta, PoseStack poseStack,
								   MultiBufferSource bufferSource, CallbackInfo ci) {
		Object2IntMap<BlockState> blockStateIds = BlockRenderingSettings.INSTANCE.getBlockStateIds();

		if (blockStateIds == null) {
			return;
		}

		// At this point, based on where we are in BlockEntityRenderDispatcher:
		// - The block entity is non-null
		// - The block entity has a world
		// - The block entity thinks that it's supported by a valid block

		int intId = blockStateIds.getOrDefault(blockEntity.getBlockState(), 0);

		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(intId);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = RUN_REPORTED, shift = At.Shift.AFTER))
	private void iris$afterRender(BlockEntity blockEntity, float tickDelta, PoseStack matrix,
								  MultiBufferSource bufferSource, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(0);
	}
}
