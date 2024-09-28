package net.irisshaders.iris.compat.sodium.mixin;

import net.caffeinemc.mods.sodium.api.vertex.format.common.ParticleVertex;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.QuadPositions;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.irisshaders.iris.vertices.sodium.IrisParticleVertex;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SingleQuadParticle.class)
public abstract class MixinSingleQuadParticle extends Particle {
	@Shadow
	public abstract float getQuadSize(float tickDelta);

	@Shadow
	protected abstract float getU0();

	@Shadow
	protected abstract float getU1();

	@Shadow
	protected abstract float getV0();

	@Shadow
	protected abstract float getV1();

	@Unique
	private Vector3f transferVector = new Vector3f();

	protected MixinSingleQuadParticle(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
	}

	/**
	 * @reason Optimize function
	 * @author JellySquid
	 */
	@Overwrite
	public void renderRotatedQuad(VertexConsumer vertexConsumer, Quaternionf quaternionf, float x, float y, float z, float tickDelta) {
		float size = this.getQuadSize(tickDelta);
		float minU = this.getU0();
		float maxU = this.getU1();
		float minV = this.getV0();
		float maxV = this.getV1();
		int light = this.getLightColor(tickDelta);

		var writer = VertexBufferWriter.of(vertexConsumer);

		int color = ColorABGR.pack(this.rCol, this.gCol, this.bCol, this.alpha);

		try (MemoryStack stack = MemoryStack.stackPush()) {
			if (Iris.isPackInUseQuick()) {
				long buffer = stack.nmalloc(4 * IrisParticleVertex.STRIDE);
				long ptr = buffer;

				this.writeVertexIris(ptr, 0, quaternionf, x, y, z, 1.0F, -1.0F, size, maxU, maxV, color, light);
				ptr += IrisParticleVertex.STRIDE;

				this.writeVertexIris(ptr, 1, quaternionf, x, y, z, 1.0F, 1.0F, size, maxU, minV, color, light);
				ptr += IrisParticleVertex.STRIDE;

				this.writeVertexIris(ptr, 2, quaternionf, x, y, z, -1.0F, 1.0F, size, minU, minV, color, light);
				ptr += IrisParticleVertex.STRIDE;

				this.writeVertexIris(ptr, 3, quaternionf, x, y, z, -1.0F, -1.0F, size, minU, maxV, color, light);
				ptr += IrisParticleVertex.STRIDE;

				writer.push(stack, buffer, 4, IrisParticleVertex.FORMAT);
			} else {
				long buffer = stack.nmalloc(4 * ParticleVertex.STRIDE);
				long ptr = buffer;

				this.writeVertex(ptr, quaternionf, x, y, z, 1.0F, -1.0F, size, maxU, maxV, color, light);
				ptr += ParticleVertex.STRIDE;

				this.writeVertex(ptr, quaternionf, x, y, z, 1.0F, 1.0F, size, maxU, minV, color, light);
				ptr += ParticleVertex.STRIDE;

				this.writeVertex(ptr, quaternionf, x, y, z, -1.0F, 1.0F, size, minU, minV, color, light);
				ptr += ParticleVertex.STRIDE;

				this.writeVertex(ptr, quaternionf, x, y, z, -1.0F, -1.0F, size, minU, maxV, color, light);
				ptr += ParticleVertex.STRIDE;

				writer.push(stack, buffer, 4, ParticleVertex.FORMAT);
			}
		}
	}

	@Unique
	private QuadPositions quadPositions = new QuadPositions();

	@Unique
	private void writeVertex(long ptr, Quaternionf quaternionf, float originX, float originY, float originZ, float posX, float posY, float size, float u, float v, int color, int light) {
		transferVector.set(posX, posY, 0.0f);
		transferVector.rotate(quaternionf);
		transferVector.mul(size);
		transferVector.add(originX, originY, originZ);

		ParticleVertex.put(ptr, transferVector.x(), transferVector.y(), transferVector.z(), u, v, color, light);
	}

	@Unique
	private void writeVertexIris(long ptr, int index, Quaternionf quaternionf, float originX, float originY, float originZ, float posX, float posY, float size, float u, float v, int color, int light) {
		transferVector.set(posX, posY, 0.0f);
		transferVector.rotate(quaternionf);
		transferVector.mul(size);
		transferVector.add(originX, originY, originZ);

		quadPositions.setAndUpdate(SystemTimeUniforms.COUNTER.getAsInt(), index, transferVector.x, transferVector.y, transferVector.z);

		IrisParticleVertex.put(ptr, transferVector.x(), transferVector.y(), transferVector.z(), quadPositions.velocityX[index], quadPositions.velocityY[index], quadPositions.velocityZ[index], u, v, color, light);
	}
}
