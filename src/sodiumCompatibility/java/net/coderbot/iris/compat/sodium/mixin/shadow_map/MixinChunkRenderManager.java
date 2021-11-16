package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPassManager;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.compat.sodium.impl.shadow_map.SwappableChunkRenderManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Modifies {@link RenderSectionManager} to support maintaining a separate visibility list for the shadow camera, as well
 * as disabling chunk rebuilds when computing visibility for the shadow camera.
 */
@Mixin(RenderSectionManager.class)
public abstract class MixinChunkRenderManager implements SwappableChunkRenderManager {
    @Shadow(remap = false)
    @Final
    @Mutable
    private ChunkRenderList chunkRenderList;

    @Shadow(remap = false)
    @Final
    @Mutable
    private ObjectList<RenderSection> tickableChunks;

    @Shadow(remap = false)
    @Final
    @Mutable
    private ObjectList<BlockEntity> visibleBlockEntities;

	@Shadow
	public abstract int getVisibleChunkCount();

	@Shadow
	private boolean needsUpdate;
	@Unique
    private ChunkRenderList chunkRenderListSwap;

    @Unique
    private ObjectList<RenderSection> tickableChunksSwap;

    @Unique
    private ObjectList<BlockEntity> visibleBlockEntitiesSwap;

    @Unique
    private boolean needsUpdateSwap;

    @Unique
    private static final ObjectArrayFIFOQueue<?> EMPTY_QUEUE = new ObjectArrayFIFOQueue<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void iris$onInit(SodiumWorldRenderer worldRenderer, BlockRenderPassManager renderPassManager, ClientLevel world, int renderDistance, CommandList commandList,
							 CallbackInfo ci) {
        this.chunkRenderListSwap = new ChunkRenderList();
        this.tickableChunksSwap = new ObjectArrayList<>();
        this.visibleBlockEntitiesSwap = new ObjectArrayList<>();

        this.needsUpdateSwap = true;
    }

    @Override
    public void iris$swapVisibilityState() {
		ChunkRenderList chunkRenderListTmp = chunkRenderList;
		chunkRenderList = chunkRenderListSwap;
		chunkRenderListSwap = chunkRenderListTmp;

		ObjectList<RenderSection> tickableChunksTmp = tickableChunks;
		tickableChunks = tickableChunksSwap;
		tickableChunksSwap = tickableChunksTmp;

		ObjectList<BlockEntity> visibleBlockEntitiesTmp = visibleBlockEntities;
		visibleBlockEntities = visibleBlockEntitiesSwap;
		visibleBlockEntitiesSwap = visibleBlockEntitiesTmp;

		boolean needsUpdateTmp = needsUpdate;
		needsUpdate = needsUpdateSwap;
		needsUpdateSwap = needsUpdateTmp;
    }

    @Inject(method = "schedulePendingUpdates", at = @At("HEAD"), cancellable = true, remap = false)
    private void iris$noRebuildEnqueueingInShadowPass(RenderSection section, CallbackInfo ci) {
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            ci.cancel();
        }
    }

	@Inject(method = "updateChunks", at = @At("RETURN"), remap = false)
	private void iris$copyBlockEntities(CallbackInfo ci) {
		if(ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			ShadowRenderer.visibleBlockEntities = visibleBlockEntities;
		}
	}

    @Redirect(method = "resetLists", remap = false,
            at = @At(value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/PriorityQueue;clear()V",
                    remap = false))
    private void iris$noQueueClearingInShadowPass$rebuildQueue(PriorityQueue instance) {
        if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            instance.clear();
        }
    }


    @Inject(method = "updateChunks()V", at = @At("HEAD"), cancellable = true, remap = false)
    private void iris$preventChunkRebuildsInShadowPass(CallbackInfo ci) {
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            ci.cancel();
        }
    }
}
