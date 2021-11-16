package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformMatrix4f;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderFogComponent;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class IrisChunkProgram extends ChunkShaderInterface {
    // Uniform variable binding indexes
    private final GlUniformMatrix4f uniformNormalMatrix;
	private final GlUniformMatrix4f uniformModelViewProjectionMatrix;

    @Nullable
    private final ProgramUniforms irisProgramUniforms;

    @Nullable
    private final ProgramSamplers irisProgramSamplers;

    public IrisChunkProgram(RenderDevice owner, ShaderBindingContext context, ChunkShaderOptions options,
							@Nullable ProgramUniforms irisProgramUniforms, @Nullable ProgramSamplers irisProgramSamplers) {
        super(context, options);
		this.uniformModelViewProjectionMatrix = context.bindUniform("u_ModelViewProjectionMatrix", GlUniformMatrix4f::new);
		this.uniformNormalMatrix = context.bindUniform("u_NormalMatrix", GlUniformMatrix4f::new);
		this.irisProgramUniforms = irisProgramUniforms;
        this.irisProgramSamplers = irisProgramSamplers;
    }

    public void setup(PoseStack poseStack, ChunkVertexType vertexType) {
        super.setup(vertexType);

        if (irisProgramUniforms != null) {
            irisProgramUniforms.update();
        }

        if (irisProgramSamplers != null) {
            irisProgramSamplers.update();
        }

		Matrix4f normalMatrix = poseStack.last().pose().copy();
        normalMatrix.invert();
        normalMatrix.transpose();
    }

	public void setModelViewProjectionMatrix(Matrix4f matrix) {
		this.uniformModelViewProjectionMatrix.set(matrix);
	}

	public void setNormalMatrix(Matrix4f matrix) {
		this.uniformNormalMatrix.set(matrix);
	}
}
