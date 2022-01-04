package net.coderbot.iris.compat.sodium.mixin.block_id;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.jellysquid.mods.sodium.render.chunk.compile.tasks.TerrainBuildBuffers;
import me.jellysquid.mods.sodium.render.chunk.passes.ChunkRenderPassManager;
import me.jellysquid.mods.sodium.render.terrain.format.TerrainVertexType;
import me.jellysquid.mods.sodium.render.vertex.VertexSink;
import me.jellysquid.mods.sodium.render.vertex.buffer.VertexBufferView;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.sodium.impl.block_id.ChunkBuildBuffersExt;
import net.coderbot.iris.compat.sodium.impl.block_id.MaterialIdAwareVertexWriter;
import net.coderbot.iris.block_rendering.MaterialIdHolder;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Associates the material ID holder with the chunk build buffers, allowing {@link MixinChunkRenderRebuildTask} to pass
 * data to {@link MaterialIdAwareVertexWriter}.
 */
@Mixin(TerrainBuildBuffers.class)
public class MixinTerrainBuildBuffers implements ChunkBuildBuffersExt {
    @Unique
    private MaterialIdHolder idHolder;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void iris$onConstruct(TerrainVertexType indexBuffers, ChunkRenderPassManager pass, CallbackInfo ci) {
        Object2IntMap<BlockState> blockStateIds = BlockRenderingSettings.INSTANCE.getBlockStateIds();

        if (blockStateIds != null) {
            this.idHolder = new MaterialIdHolder(blockStateIds);
        } else {
            this.idHolder = new MaterialIdHolder();
        }
    }

    @Redirect(method = "init", remap = false, at = @At(value = "INVOKE",
            target = "Lme/jellysquid/mods/sodium/render/terrain/format/TerrainVertexType;createBufferWriter" +
					"(Lme/jellysquid/mods/sodium/render/vertex/buffer/VertexBufferView;)" +
					"Lme/jellysquid/mods/sodium/render/vertex/VertexSink;", remap = false))
    private VertexSink iris$redirectWriterCreation(TerrainVertexType instance, VertexBufferView vertexBufferView) {
        VertexSink sink = instance.createBufferWriter(vertexBufferView);

        if (sink instanceof MaterialIdAwareVertexWriter) {
            ((MaterialIdAwareVertexWriter) sink).iris$setIdHolder(idHolder);
        }

        return sink;
    }

    @Override
    public void iris$setMaterialId(BlockState state, short renderType) {
        this.idHolder.set(state, renderType);
    }

    @Override
    public void iris$resetMaterialId() {
        this.idHolder.reset();
    }
}
