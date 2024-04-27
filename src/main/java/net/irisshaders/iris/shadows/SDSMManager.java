package net.irisshaders.iris.shadows;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.program.ComputeProgram;
import net.irisshaders.iris.gl.shader.ShaderCompileException;
import net.irisshaders.iris.gl.shader.ShaderWorkarounds;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.gl.texture.PixelFormat;
import net.irisshaders.iris.gl.texture.PixelType;
import net.irisshaders.iris.targets.RenderTarget;
import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL42C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL46C;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SDSMManager {
	static final int ReductionTGSize = 16;
	static final int CullTGSize = 128;
	static final int BatchTGSize = 256;
	// Computes a compute shader dispatch size given a thread group size, and number of elements to process
	private static int DispatchSize(int tgSize, int numElements)
	{
		int dispatchSize = numElements / tgSize;
		dispatchSize += numElements % tgSize > 0 ? 1 : 0;
		return dispatchSize;
	}

	private BasicTexture[] reductions;

	public void setupTextures(int width, int height) {
		if (reductions != null) {
			for (BasicTexture reduction : reductions) {
				reduction.destroy();
			}
		}

		List<BasicTexture> list = new ArrayList<>();
		while(width > 1 || height > 1) {
			width = DispatchSize(ReductionTGSize, width);
			height = DispatchSize(ReductionTGSize, height);

			BasicTexture rt = BasicTexture.builder().setDimensions(width, height)
				.setName("reduction " + width + " " + height)
				.setPixelFormat(PixelFormat.RG)
				.setPixelType(PixelType.SHORT)
				.setInternalFormat(InternalTextureFormat.RG16)
				.build();
			list.add(rt);
		}

		this.reductions = list.toArray(BasicTexture[]::new);
	}

	private static int initialReduction;
	private static int furtherReduction;

	static {
		initialReduction = GL46C.glCreateProgram();
		furtherReduction = GL46C.glCreateProgram();

		int initialS = GL46C.glCreateShader(GL46C.GL_COMPUTE_SHADER);
		int finalS = GL46C.glCreateShader(GL46C.GL_COMPUTE_SHADER);
		String source;
		try {
			source = new String(IOUtils.toByteArray(Objects.requireNonNull(SDSMManager.class.getResourceAsStream("/initialReduction.csh"))), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ShaderWorkarounds.safeShaderSource(initialS, source);

		GL46C.glCompileShader(initialS);
		String log = IrisRenderSystem.getShaderInfoLog(initialS);

		if (!log.isEmpty()) {
			Iris.logger.warn("Shader compilation log for " + "initial" + ": " + log);
		}

		int result = GlStateManager.glGetShaderi(initialS, GL20C.GL_COMPILE_STATUS);

		if (result != GL20C.GL_TRUE) {
			throw new ShaderCompileException("initialR", log);
		}

		GL46C.glAttachShader(initialReduction, initialS);
		GL46C.glLinkProgram(initialReduction);
		GL46C.glDeleteShader(initialS);

		try {
			source = new String(IOUtils.toByteArray(Objects.requireNonNull(SDSMManager.class.getResourceAsStream("/furtherReduction.csh"))), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ShaderWorkarounds.safeShaderSource(finalS, source);
		GL46C.glCompileShader(finalS);
		GL46C.glAttachShader(furtherReduction, finalS);
		GL46C.glLinkProgram(furtherReduction);
		GL46C.glDeleteShader(finalS);
	}

	public void reduceDepth(int depthTex, Matrix4f projection, float nearClip, float farClip) {
		GlStateManager._glUseProgram(initialReduction);
		IrisRenderSystem.bindImageTexture(0, reductions[0].getTexture(), 0, true, 0, GL42C.GL_READ_WRITE, InternalTextureFormat.RG16.getGlFormat());
		IrisRenderSystem.bindTextureToUnit(GL43C.GL_TEXTURE_2D, 0, depthTex);

		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer buffer = stack.mallocFloat(16);
			projection.get(buffer);
			GL46C.glUniformMatrix4fv(1, false, buffer);
		}
		GL46C.glUniform1f(2, nearClip);
		GL46C.glUniform1f(3, farClip);
		IrisRenderSystem.dispatchCompute(reductions[0].getWidth(), reductions[0].getHeight(), 1);

		GlStateManager._glUseProgram(furtherReduction);

		for (int i = 1; i < reductions.length; i++) {
			IrisRenderSystem.memoryBarrier(GL46C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL46C.GL_TEXTURE_FETCH_BARRIER_BIT);

			IrisRenderSystem.bindImageTexture(0, reductions[i].getTexture(), 0, true, 0, GL42C.GL_READ_WRITE, InternalTextureFormat.RG16.getGlFormat());
			IrisRenderSystem.bindTextureToUnit(GL43C.GL_TEXTURE_2D, 0, reductions[i - 1].getTexture());
			IrisRenderSystem.dispatchCompute(reductions[i].getWidth(), reductions[i].getHeight(), 1);
		}


		IrisRenderSystem.memoryBarrier(GL46C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL46C.GL_TEXTURE_FETCH_BARRIER_BIT);

	}
}
