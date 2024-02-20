package net.irisshaders.iris.forge.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.irisshaders.iris.block_rendering.BlockRenderingSettings;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ItemBlockRenderTypes.class)
public class MixinItemBlockRenderTypes {
	@Unique
	private static Object2ObjectOpenHashMap<RenderType, ChunkRenderTypeSet> setMap = new Object2ObjectOpenHashMap<>();

	@Inject(method = "getRenderLayers", at = @At("HEAD"), cancellable = true, remap = false)
	private static void iris$fixForgeRenderLayers(BlockState state, CallbackInfoReturnable<ChunkRenderTypeSet> cir) {
		Map<Block, RenderType> idMap = BlockRenderingSettings.INSTANCE.getBlockTypeIds();
		if (idMap != null) {
			RenderType type = idMap.get(state.getBlock());
			if (type != null) {
				cir.setReturnValue(setMap.computeIfAbsent(type, type2 -> ChunkRenderTypeSet.of(type)));
			}
		}
	}
}
