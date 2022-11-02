package net.coderbot.iris.pipeline.newshader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.blending.BufferBlendOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.image.ImageHolder;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.gl.sampler.SamplerHolder;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.pipeline.newshader.uniforms.RedirectingUniform3F;
import net.coderbot.iris.pipeline.newshader.uniforms.RedirectingUniform4F;
import net.coderbot.iris.pipeline.newshader.uniforms.RedirectingUniformMatrix;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FakeShader extends ShaderInstance {
	private final Program program;
	private final NewWorldRenderingPipeline parent;
	private final GlFramebuffer beforeTranslucent;
	private final GlFramebuffer afterTranslucent;
	private final BlendModeOverride blendOverride;
	private final List<BufferBlendOverride> bufferOverrides;
	private final AlphaTest alphaTest;
	private int programId;
	private static FakeShader lastApplied;
	private final boolean intensitySwizzle;

	private static final ImmutableSet<String> ATTRIBUTE_LIST = ImmutableSet.of("Position", "Color", "Normal", "UV0", "UV1", "UV2");

	public FakeShader(String name, String vertex, @Nullable String geometry, String fragment, VertexFormat vertexFormat,
					  GlFramebuffer writingToBeforeTranslucent, GlFramebuffer writingToAfterTranslucent,
					  GlFramebuffer baseline, BlendModeOverride blendModeOverride, AlphaTest alphaTest,
					  Consumer<DynamicUniformHolder> uniformCreator, BiConsumer<SamplerHolder, ImageHolder> samplerCreator, boolean isIntensity,
					  NewWorldRenderingPipeline parent, ShaderAttributeInputs inputs, @Nullable List<BufferBlendOverride> bufferBlendOverrides) throws IOException {
		// Run our own constructor.
		super(Minecraft.getInstance().getResourceManager(), "blit_screen", vertexFormat);
		super.close();

		ProgramBuilder builder = ProgramBuilder.begin(name, false, vertex, geometry, fragment, ImmutableSet.of(0, 1, 2));

		ImmutableList<String> elementAttributeNames = vertexFormat.getElementAttributeNames();
		Iris.logger.warn("Vertex format " + vertexFormat.toString() + " value " + elementAttributeNames.toString());
		for (int i = 0, elementAttributeNamesSize = elementAttributeNames.size(); i < elementAttributeNamesSize; i++) {
			String attributeName = elementAttributeNames.get(i);
			if (ATTRIBUTE_LIST.contains(attributeName)) {
				attributeName = "iris_" + attributeName;
			}
			builder.bindAttributeLocation(i, attributeName);
			Iris.logger.warn("Bound " + i + " to " + attributeName);
		}

		samplerCreator.accept(builder, builder);
		uniformCreator.accept(builder);

		this.program = builder.build();
		this.programId = program.getProgramId();

		this.parent = parent;
		this.beforeTranslucent = writingToBeforeTranslucent;
		this.afterTranslucent = writingToAfterTranslucent;
		this.alphaTest = alphaTest;
		this.blendOverride = blendModeOverride;
		this.bufferOverrides = bufferBlendOverrides;

		this.intensitySwizzle = isIntensity;

		//Iris.logger.warn(String.valueOf(GlStateManager._glGetUniformLocation(programId, "iris_ProjMat")));
		this.PROJECTION_MATRIX = new RedirectingUniformMatrix(programId, GlStateManager._glGetUniformLocation(programId, "iris_ProjMat"));
		this.MODEL_VIEW_MATRIX = new RedirectingUniformMatrix(programId, GlStateManager._glGetUniformLocation(programId, "iris_ModelViewMat"));
		this.TEXTURE_MATRIX = new RedirectingUniformMatrix(programId, GlStateManager._glGetUniformLocation(programId, "iris_TextureMat"));
		this.COLOR_MODULATOR = new RedirectingUniform4F(programId, GlStateManager._glGetUniformLocation(programId, "iris_ColorModulator"));
		this.CHUNK_OFFSET = new RedirectingUniform3F(programId, GlStateManager._glGetUniformLocation(programId, "iris_ChunkOffset"));
	}

	@Override
	public void close() {
		PROJECTION_MATRIX.close();
		MODEL_VIEW_MATRIX.close();
		TEXTURE_MATRIX.close();
		COLOR_MODULATOR.close();
		CHUNK_OFFSET.close();
		program.destroy();
	}

	@Override
	public void clear() {
		ProgramUniforms.clearActiveUniforms();
		ProgramSamplers.clearActiveSamplers();
		lastApplied = null;

		if (this.blendOverride != null || !bufferOverrides.isEmpty()) {
			BlendModeOverride.restore();
		}

		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
	}

	@Override
	public void apply() {
		CapturedRenderingState.INSTANCE.setCurrentAlphaTest(alphaTest.getReference());

		IrisRenderSystem.bindTextureToUnit(IrisSamplers.ALBEDO_TEXTURE_UNIT, RenderSystem.getShaderTexture(0));
		IrisRenderSystem.bindTextureToUnit(IrisSamplers.OVERLAY_TEXTURE_UNIT, RenderSystem.getShaderTexture(1));
		IrisRenderSystem.bindTextureToUnit(IrisSamplers.LIGHTMAP_TEXTURE_UNIT, RenderSystem.getShaderTexture(2));

		program.use();

		PROJECTION_MATRIX.upload();
		MODEL_VIEW_MATRIX.upload();
		TEXTURE_MATRIX.upload();
		COLOR_MODULATOR.upload();
		CHUNK_OFFSET.upload();

		if (this.blendOverride != null) {
			this.blendOverride.apply();
		}

		if (!bufferOverrides.isEmpty()) {
			bufferOverrides.forEach(BufferBlendOverride::apply);
		}

		if (parent.isBeforeTranslucent) {
			beforeTranslucent.bind();
		} else {
			afterTranslucent.bind();
		}
	}

	@Override
	public int getId() {
		return programId;
	}

	public boolean hasActiveImages() {
		return program.getActiveImages() > 0;
	}

	public boolean isIntensitySwizzle() {
		return intensitySwizzle;
	}
}
