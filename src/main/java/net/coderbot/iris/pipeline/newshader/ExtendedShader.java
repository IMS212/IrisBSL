package net.coderbot.iris.pipeline.newshader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.IrisLogging;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.gl.sampler.SamplerHolder;
import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20C;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class ExtendedShader extends ShaderInstance implements SamplerHolder {
	NewWorldRenderingPipeline parent;
	ProgramUniforms uniforms;
	GlFramebuffer writingToBeforeTranslucent;
	GlFramebuffer writingToAfterTranslucent;
	GlFramebuffer baseline;
	HashMap<String, IntSupplier> dynamicSamplers;
	private final boolean intensitySwizzle;
	private final int[] blendModeOverride;
	private final boolean disableBlend;

	public ExtendedShader(ResourceProvider resourceFactory, String string, VertexFormat vertexFormat, GlFramebuffer writingToBeforeTranslucent, GlFramebuffer writingToAfterTranslucent, GlFramebuffer baseline, int[] blendModeOverride, boolean disableBlend, Consumer<DynamicUniformHolder> uniformCreator, NewWorldRenderingPipeline parent) throws IOException {
		super(resourceFactory, string, vertexFormat);

		int programId = this.getId();

		ProgramUniforms.Builder uniformBuilder = ProgramUniforms.builder(string, programId);
		uniformCreator.accept(uniformBuilder);

		uniforms = uniformBuilder.buildUniforms();
		this.writingToBeforeTranslucent = writingToBeforeTranslucent;
		this.writingToAfterTranslucent = writingToAfterTranslucent;
		this.baseline = baseline;
		this.dynamicSamplers = new HashMap<>();
		this.parent = parent;
		this.blendModeOverride = blendModeOverride;
		this.disableBlend = disableBlend;

		// TODO(coderbot): consider a way of doing this that doesn't rely on checking the shader name.
		this.intensitySwizzle = getName().contains("intensity");
	}

	public boolean isIntensitySwizzle() {
		return intensitySwizzle;
	}

	@Override
	public void clear() {
		ProgramUniforms.clearActiveUniforms();
		super.clear();

		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
		BlockRenderingSettings.INSTANCE.setCurrentBlendFunc(null);
	}

	@Override
	public void apply() {
		dynamicSamplers.forEach((name, supplier) -> this.addIrisSampler(name, supplier.getAsInt()));

		if (disableBlend) {
			GlStateManager._disableBlend();
		} else {
			GlStateManager._enableBlend();
		}

		BlockRenderingSettings.INSTANCE.setCurrentBlendFunc(blendModeOverride);

		super.apply();

		RenderSystem.defaultBlendFunc();

		uniforms.update();

		if (parent.isBeforeTranslucent) {
			writingToBeforeTranslucent.bind();
		} else {
			writingToAfterTranslucent.bind();
		}
	}

	public void addIrisSampler(String name, int id) {
		super.setSampler(name, id);
	}

	public void addIrisSampler(String name, IntSupplier supplier) {
		dynamicSamplers.put(name, supplier);
	}

	public int[] getBlendModeOverride() {
		return blendModeOverride;
	}

	@Override
	public void setSampler(String name, Object sampler) {
		// Translate vanilla sampler names to Iris / ShadersMod sampler names
		if (name.equals("Sampler0")) {
			name = "gtexture";

			// "tex" and "texture" are also valid sampler names.
			super.setSampler("texture", sampler);
			super.setSampler("tex", sampler);
		} else if (name.equals("Sampler2")) {
			name = "lightmap";
		} else if (name.startsWith("Sampler")) {
			// We only care about the texture and the lightmap for now from vanilla.
			// All other samplers will be coming from Iris.
			return;
		} else {
			Iris.logger.warn("Iris: didn't recognize the sampler name " + name + " in addSampler, please use addIrisSampler for custom Iris-specific samplers instead.");
			return;
		}

		// TODO: Expose Sampler1 (the mob overlay flash)

		super.setSampler(name, sampler);
	}

	@Nullable
	@Override
	public Uniform getUniform(String name) {
		// Prefix all uniforms with Iris to help avoid conflicts with existing names within the shader.
		return super.getUniform("iris_" + name);
	}

	// TODO: This is kind of a mess. The interface might need some cleanup here.
	@Override
	public void addExternalSampler(int textureUnit, String... names) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public boolean hasSampler(String name) {
		return GL20C.glGetUniformLocation(this.getId(), name) != -1;
	}

	@Override
	public boolean addDefaultSampler(IntSupplier sampler, Runnable postBind, String... names) {
		throw new UnsupportedOperationException("addDefaultSampler is not yet implemented");
	}

	@Override
	public boolean addDynamicSampler(IntSupplier sampler, Runnable postBind, String... names) {
		if (IrisLogging.ENABLE_SPAM) {
			Iris.logger.warn("postBind isn't supported on 1.17.");
		}
		return addDynamicSampler(sampler, names);
	}

	@Override
	public boolean addDynamicSampler(IntSupplier sampler, String... names) {
		boolean used = false;

		for (String name : names) {
			if (hasSampler(name)) {
				used = true;
			}

			addIrisSampler(name, sampler);
		}

		return used;
	}
}
