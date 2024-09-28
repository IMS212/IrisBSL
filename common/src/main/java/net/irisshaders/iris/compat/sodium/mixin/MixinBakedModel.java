package net.irisshaders.iris.compat.sodium.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.render.frapi.helper.ColorHelper;
import net.caffeinemc.mods.sodium.client.render.immediate.model.BakedModelEncoder;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.QuadPositionAccess;
import net.irisshaders.iris.pipeline.QuadPositions;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import net.irisshaders.iris.vertices.sodium.IrisEntityVertex;
import net.irisshaders.iris.vertices.sodium.ModelVertexStorage;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BakedModelEncoder.class)
public abstract class MixinBakedModel {
	private static final Vector4f TANGENT_HOLDER = new Vector4f();

	@Shadow
	private static int mergeLighting(int stored, int calculated) {
		return 0;
	}

	@Inject(method = "writeQuadVertices(Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadView;IIIZ)V", at = @At("HEAD"), cancellable = true)
	private static void redirectToIris(VertexBufferWriter writer, PoseStack.Pose matrices, ModelQuadView quad, int color, int light, int overlay, boolean colorize, CallbackInfo ci) {
		if (Iris.isPackInUseQuick()) {
			ci.cancel();
			writeIris(writer, matrices, quad, color, light, overlay, colorize);
		}
	}

	private static final ModelVertexStorage[] VERTICES = new ModelVertexStorage[4];

	static {
		for (int i = 0; i < 4; i++) {
			VERTICES[i] = new ModelVertexStorage();
		}
	}

	private static void writeIris(VertexBufferWriter writer, PoseStack.Pose matrices, ModelQuadView quad, int color, int light, int overlay, boolean colorize) {
		Matrix3f matNormal = matrices.normal();
		Matrix4f matPosition = matrices.pose();

		IrisEntityVertex vertexWriter = WorldRenderingSettings.INSTANCE.getSodiumEntityWriter();

		QuadPositions quadPositions = ((QuadPositionAccess) quad).getQuadPosition(CapturedRenderingState.INSTANCE.getEntityRollingId());

		try (MemoryStack stack = MemoryStack.stackPush()) {
			long buffer = stack.nmalloc(4 * WorldRenderingSettings.INSTANCE.getEntityFormat().getVertexSize());
			long ptr = buffer;

			float midU = (quad.getTexU(0) + quad.getTexU(1) + quad.getTexU(2) + quad.getTexU(3)) * 0.25f;
			float midV = (quad.getTexV(0) + quad.getTexV(1) + quad.getTexV(2) + quad.getTexV(3)) * 0.25f;

			for (int i = 0; i < 4; i++) {
				// The position vector
				float x = quad.getX(i);
				float y = quad.getY(i);
				float z = quad.getZ(i);

				VERTICES[i].light = mergeLighting(quad.getLight(i), light);

				VERTICES[i].color = color;

				if (colorize) {
					VERTICES[i].color = ColorHelper.multiplyColor(VERTICES[i].color, quad.getColor(i));
				}

				// The packed transformed normal vector
				VERTICES[i].normal = MatrixHelper.transformNormal(matNormal, matrices.trustedNormals, quad.getAccurateNormal(i));
				VERTICES[i].u = quad.getTexU(i);
				VERTICES[i].v = quad.getTexV(i);
				// The transformed position vector
				VERTICES[i].x = MatrixHelper.transformPositionX(matPosition, x, y, z);
				VERTICES[i].y = MatrixHelper.transformPositionY(matPosition, x, y, z);
				VERTICES[i].z = MatrixHelper.transformPositionZ(matPosition, x, y, z);
			}

			// Actually draw now

			int tangent = NormalHelper.computeTangent(TANGENT_HOLDER, NormI8.unpackX(quad.getFaceNormal()), NormI8.unpackY(quad.getFaceNormal()), NormI8.unpackZ(quad.getFaceNormal()),
				VERTICES[0].x, VERTICES[0].y, VERTICES[0].z, VERTICES[0].u, VERTICES[0].v,
				VERTICES[1].x, VERTICES[1].y, VERTICES[1].z, VERTICES[1].u, VERTICES[1].v,
				VERTICES[2].x, VERTICES[2].y, VERTICES[2].z, VERTICES[2].u, VERTICES[2].v
				);

			for (int i = 0; i < 4; i++) {

				quadPositions.setAndUpdate(SystemTimeUniforms.COUNTER.getAsInt(), i, VERTICES[i].x, VERTICES[i].y, VERTICES[i].z);

				// TODO TANGENT
				vertexWriter.write(ptr, VERTICES[i].x, VERTICES[i].y, VERTICES[i].z, quadPositions.velocityX[i], quadPositions.velocityY[i], quadPositions.velocityZ[i], VERTICES[i].color, VERTICES[i].u, VERTICES[i].v, overlay, VERTICES[i].light, VERTICES[i].normal, tangent, midU, midV);
				ptr += WorldRenderingSettings.INSTANCE.getEntityFormat().getVertexSize();
			}

			writer.push(stack, buffer, 4, WorldRenderingSettings.INSTANCE.getEntityFormat());
		}
	}
}
