package net.coderbot.iris.mixin;

import net.coderbot.iris.layer.EntityColorRenderPhase;
import net.coderbot.iris.layer.InnerWrappedRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.TntMinecartEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TntMinecartEntityRenderer.class)
public abstract class MixinTntMinecartEntityRenderer {
	//@ModifyVariable(method = "renderBlock", at = @At("HEAD"))
	private VertexConsumerProvider iris$wrapProvider(TntMinecartEntity tntMinecartEntity, float f, BlockState blockState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
		if (f > 0) {
			EntityColorRenderPhase phase = new EntityColorRenderPhase(false, 1.0F);
			return layer -> vertexConsumerProvider.getBuffer(new InnerWrappedRenderLayer("iris_entity_color", layer, phase));
		} else {
			return vertexConsumerProvider;
		}
	}
}
