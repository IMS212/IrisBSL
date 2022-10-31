package net.coderbot.iris.pipeline.newshader;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.blending.BufferBlendOverride;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.image.ImageHolder;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.sampler.SamplerHolder;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ShaderTemplate implements AutoCloseable {
	private final ShaderKey key;
	private final Program program;
	private final Int2ObjectMap<PersonalizedShader> personalizedShaders = new Int2ObjectOpenHashMap<>();
	public final List<BufferBlendOverride> bufferBlendOverrides;
	private final GlFramebuffer beforeTranslucent;
	private final GlFramebuffer afterTranslucent;
	private final AbstractTexture whitePixel;
	public BlendModeOverride blendModeOverride;
	private static ShaderTemplate lastApplied;
	private NewWorldRenderingPipeline parent;
	private MutableShaderAttributeInputs inputs = new MutableShaderAttributeInputs(true, true, true, true, true, false);

	public ShaderTemplate(String name, String vertex, String geometry, String fragment,
						  GlFramebuffer writingToBeforeTranslucent, GlFramebuffer writingToAfterTranslucent,
						  GlFramebuffer baseline, BlendModeOverride blendModeOverride, ShaderKey key, AbstractTexture whitePixel,
						  Consumer<DynamicUniformHolder> uniformCreator, BiConsumer<SamplerHolder, ImageHolder> samplerCreator,
						  NewWorldRenderingPipeline parent, @Nullable List<BufferBlendOverride> bufferBlendOverrides) {
		this.key = key;
		ProgramBuilder builder = ProgramBuilder.begin(name, vertex, geometry, fragment, ImmutableSet.of(0));
		uniformCreator.accept(builder);
		samplerCreator.accept(builder, builder);
		builder.bindAttributeLocation(0, "iris_Position");
		builder.bindAttributeLocation(1, "iris_Color");
		builder.bindAttributeLocation(2, "iris_UV0");
		builder.bindAttributeLocation(3, "iris_UV1");
		builder.bindAttributeLocation(4, "iris_UV2");
		builder.bindAttributeLocation(5, "iris_Normal");
		builder.bindAttributeLocation(6, "mc_Entity");
		builder.bindAttributeLocation(7, "mc_midTexCoord");
		builder.bindAttributeLocation(8, "at_tangent");
		builder.bindAttributeLocation(9, "at_midBlock");

		// Vanilla replacement uniforms
		builder.uniform1b("iris_hasColor", inputs::hasColor, listener -> {});
		builder.uniform1b("iris_hasTex", inputs::hasTex, listener -> {});
		builder.uniform1b("iris_hasLight", inputs::hasLight, listener -> {});
		builder.uniform1b("iris_hasOverlay", inputs::hasOverlay, listener -> {});
		builder.uniform1b("iris_hasNormal", inputs::hasNormal, listener -> {});
		builder.uniform1b("iris_applyLines", inputs::isNewLines, listener -> {});
		this.program = builder.build();
		this.blendModeOverride = blendModeOverride;
		this.bufferBlendOverrides = bufferBlendOverrides;
		this.parent = parent;
		this.beforeTranslucent = writingToBeforeTranslucent;
		this.afterTranslucent = writingToAfterTranslucent;
		this.whitePixel = whitePixel;
	}

	public void apply() {
		if (lastApplied != this) {
			lastApplied = this;
			program.use();
		} else {
			program.updateUniforms();
		}

		IrisRenderSystem.bindTextureToUnit(IrisSamplers.ALBEDO_TEXTURE_UNIT, inputs.hasTex() ? RenderSystem.getShaderTexture(0) : whitePixel.getId());
		IrisRenderSystem.bindTextureToUnit(IrisSamplers.OVERLAY_TEXTURE_UNIT, inputs.hasOverlay() ? RenderSystem.getShaderTexture(1) : whitePixel.getId());
		IrisRenderSystem.bindTextureToUnit(IrisSamplers.LIGHTMAP_TEXTURE_UNIT, inputs.hasLight() ? RenderSystem.getShaderTexture(2) : whitePixel.getId());

		if (this.blendModeOverride != null) {
			this.blendModeOverride.apply();
		}

		if (!bufferBlendOverrides.isEmpty()) {
			bufferBlendOverrides.forEach(BufferBlendOverride::apply);
		}

		if (parent.isBeforeTranslucent) {
			beforeTranslucent.bind();
		} else {
			afterTranslucent.bind();
		}
	}

	public int getProgramId() {
		return program.getProgramId();
	}

	public void close() {
		program.destroy();
	}

	public void setUniforms(ShaderAttributeInputs inputs) {
		this.inputs.setInputs(inputs);
	}

	public boolean hasActiveImages() {
		return program.getActiveImages() > 0;
	}
}
