package net.coderbot.iris.compat.sodium.mixin.block_id;

import me.jellysquid.mods.sodium.render.chunk.compile.tasks.TerrainBuildResult;
import me.jellysquid.mods.sodium.render.chunk.compile.tasks.TerrainBuildTask;
import me.jellysquid.mods.sodium.render.terrain.TerrainBuildContext;
import me.jellysquid.mods.sodium.util.tasks.CancellationSource;
import net.coderbot.iris.compat.sodium.impl.block_id.ChunkBuildBuffersExt;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Passes material ID information indirectly to the vertex writer to support the mc_Entity part of the vertex format.
 */
@Mixin(TerrainBuildTask.class)
public class MixinChunkRenderRebuildTask {
    @Redirect(method = "performBuild", at = @At(value = "INVOKE",
            target = "net/minecraft/client/renderer/ItemBlockRenderTypes.getChunkRenderType (" +
                        "Lnet/minecraft/world/level/block/state/BlockState;" +
                    ")Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType iris$wrapGetBlockLayer(BlockState blockState, TerrainBuildContext buildContext, CancellationSource cancellationSource) {
        if (buildContext.buffers instanceof ChunkBuildBuffersExt) {
            ((ChunkBuildBuffersExt) buildContext.buffers).iris$setMaterialId(blockState, (short) -1);
        }

        return ItemBlockRenderTypes.getChunkRenderType(blockState);
    }

    @Redirect(method = "performBuild", at = @At(value = "INVOKE",
            target = "net/minecraft/client/renderer/ItemBlockRenderTypes.getRenderLayer (" +
                        "Lnet/minecraft/world/level/material/FluidState;" +
                    ")Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType iris$wrapGetFluidLayer(FluidState fluidState, TerrainBuildContext buildContext, CancellationSource cancellationSource) {
        if (buildContext.buffers instanceof ChunkBuildBuffersExt) {
            // All fluids have a ShadersMod render type of 1, to match behavior of Minecraft 1.7 and earlier.
            ((ChunkBuildBuffersExt) buildContext.buffers).iris$setMaterialId(fluidState.createLegacyBlock(), (short) 1);
        }

        return ItemBlockRenderTypes.getRenderLayer(fluidState);
    }

    @Inject(method = "performBuild",
            at = @At(value = "INVOKE", target = "net/minecraft/world/level/block/state/BlockState.hasBlockEntity ()Z"))
    private void iris$resetId(TerrainBuildContext buildContext, CancellationSource source, CallbackInfoReturnable<TerrainBuildResult> cir) {
        if (buildContext.buffers instanceof ChunkBuildBuffersExt) {
            ((ChunkBuildBuffersExt) buildContext.buffers).iris$resetMaterialId();
        }
    }
}
