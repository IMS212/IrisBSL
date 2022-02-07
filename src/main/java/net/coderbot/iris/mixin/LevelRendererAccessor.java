package net.coderbot.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	@Accessor("entityRenderDispatcher")
	EntityRenderDispatcher getEntityRenderDispatcher();

	@Invoker("renderChunkLayer")
	void invokeRenderChunkLayer(RenderType terrainLayer, PoseStack modelView, double cameraX, double cameraY, double cameraZ, Matrix4f matrix4f);

	@Invoker("updateRenderChunks")
	void invokeUpdateRenderChunks(Frustum arg, int i, boolean bl, Vec3 arg22, BlockPos arg3, ChunkRenderDispatcher.RenderChunk arg4, int j, BlockPos arg5);

	@Invoker("renderEntity")
	void invokeRenderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource);

	@Accessor("level")
	ClientLevel getLevel();

	@Accessor
	ViewArea getViewArea();

	@Accessor("frameId")
	int getFrameId();

	@Accessor("frameId")
	void setFrameId(int frame);
}
