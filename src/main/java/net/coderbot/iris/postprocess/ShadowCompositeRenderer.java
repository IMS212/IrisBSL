package net.coderbot.iris.postprocess;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.sampler.SamplerLimits;
import net.coderbot.iris.rendertarget.RenderTargets;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.shaderpack.ProgramDirectives;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shadows.ShadowMapRenderer;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.FrameUpdateNotifier;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.util.Objects;

public class ShadowCompositeRenderer {
	private final ShadowRenderTargets renderTargets;
	private final RenderTargets gbufferRenderTargets;

	private final ImmutableList<Pass> passes;
	private final AbstractTexture noiseTexture;

	public ShadowCompositeRenderer(ProgramSet pack, ShadowRenderTargets renderTargets, RenderTargets gbufferRenderTargets, AbstractTexture noiseTexture, 
								   ShadowMapRenderer shadowMapRenderer) {

		this.noiseTexture = noiseTexture;
		this.gbufferRenderTargets = gbufferRenderTargets;
		this.renderTargets = renderTargets;

		final ImmutableList.Builder<Pass> passes = ImmutableList.builder();

		for (ProgramSource source : pack.getShadowComposite()) {
			if (source == null || !source.isValid()) {
				continue;
			}

			Pass pass = new Pass();
			ProgramDirectives directives = source.getDirectives();

			pass.program = createProgram(source, shadowMapRenderer);
			pass.mipmappedBuffers = directives.getMipmappedBuffers();

			passes.add(pass);
		}

		this.passes = passes.build();

		GL30C.glBindFramebuffer(GL30C.GL_READ_FRAMEBUFFER, 0);
	}

	private static final class Pass {
		Program program;
		ImmutableSet<Integer> mipmappedBuffers;

		private void destroy() {
			this.program.destroy();
		}
	}

	public void renderShadowComposites() {
		RenderSystem.disableBlend();
		RenderSystem.disableAlphaTest();

		FullScreenQuadRenderer.INSTANCE.begin();

		for (Pass renderPass : passes) {
			if (!renderPass.mipmappedBuffers.isEmpty()) {
				RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

				for (int index : renderPass.mipmappedBuffers) {
					setupMipmapping(renderTargets.getColorTextureId(index));
				}
			}

			renderTargets.getFramebuffer().bind();
			renderPass.program.use();

			FullScreenQuadRenderer.INSTANCE.renderQuad();
		}

		FullScreenQuadRenderer.end();

		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);

		resetRenderTarget(renderTargets.getColorTextureId(0));
		resetRenderTarget(renderTargets.getColorTextureId(1));

		// Make sure to reset the viewport to how it was before... Otherwise weird issues could occur.
		GlStateManager._glUseProgram(0);

		for (int i = 0; i < SamplerLimits.get().getMaxTextureUnits(); i++) {
			// Unbind all textures that we may have used.
			// NB: This is necessary for shader pack reloading to work propely
			RenderSystem.activeTexture(GL15C.GL_TEXTURE0 + i);
			RenderSystem.bindTexture(0);
		}

		RenderSystem.activeTexture(GL15C.GL_TEXTURE0);
	}

	private static void setupMipmapping(int target) {
		RenderSystem.bindTexture(target);

		// TODO: Only generate the mipmap if a valid mipmap hasn't been generated or if we've written to the buffer
		// (since the last mipmap was generated)
		//
		// NB: We leave mipmapping enabled even if the buffer is written to again, this appears to match the
		// behavior of ShadersMod/OptiFine, however I'm not sure if it's desired behavior. It's possible that a
		// program could use mipmapped sampling with a stale mipmap, which probably isn't great. However, the
		// sampling mode is always reset between frames, so this only persists after the first program to use
		// mipmapping on this buffer.
		//
		// Also note that this only applies to one of the two buffers in a render target buffer pair - making it
		// unlikely that this issue occurs in practice with most shader packs.
		GL30C.glGenerateMipmap(GL20C.GL_TEXTURE_2D);
		GL30C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR_MIPMAP_LINEAR);
	}

	private static void resetRenderTarget(int target) {
		RenderSystem.bindTexture(target);
		GL30C.glTexParameteri(GL20C.GL_TEXTURE_2D, GL20C.GL_TEXTURE_MIN_FILTER, GL20C.GL_LINEAR);
		RenderSystem.bindTexture(0);
	}

	// TODO: Don't just copy this from DeferredWorldRenderingPipeline
	private Program createProgram(ProgramSource source,
								  ShadowMapRenderer shadowMapRenderer) {
		// TODO: Properly handle empty shaders
		Objects.requireNonNull(source.getVertexSource());
		Objects.requireNonNull(source.getFragmentSource());
		ProgramBuilder builder;

		try {
			builder = ProgramBuilder.begin(source.getName(), source.getVertexSource().orElse(null), source.getGeometrySource().orElse(null),
				source.getFragmentSource().orElse(null), IrisSamplers.COMPOSITE_RESERVED_TEXTURE_UNITS);
		} catch (RuntimeException e) {
			// TODO: Better error handling
			throw new RuntimeException("Shader compilation failed!", e);
		}

		CommonUniforms.addCommonUniforms(builder, source.getParent().getPack().getIdMap(), source.getParent().getPackDirectives(), new FrameUpdateNotifier());
		IrisSamplers.addRenderTargetSamplers(builder, () -> ImmutableSet.<Integer>builder().build(), gbufferRenderTargets, false);
		IrisSamplers.addNoiseSampler(builder, noiseTexture);
		IrisSamplers.addShadowSamplers(builder, shadowMapRenderer);
		
		return builder.build();
	}

	public void destroy() {
		for (Pass renderPass : passes) {
			renderPass.destroy();
		}
	}
}
