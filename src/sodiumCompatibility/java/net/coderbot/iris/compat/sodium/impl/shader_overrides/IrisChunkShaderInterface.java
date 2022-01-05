package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import me.jellysquid.mods.sodium.opengl.buffer.Buffer;
import me.jellysquid.mods.sodium.opengl.shader.ShaderBindingContext;
import me.jellysquid.mods.sodium.opengl.shader.uniform.UniformBlock;
import me.jellysquid.mods.sodium.opengl.shader.uniform.UniformFloat;
import me.jellysquid.mods.sodium.opengl.shader.uniform.UniformFloatArray;
import me.jellysquid.mods.sodium.opengl.shader.uniform.UniformInt;
import me.jellysquid.mods.sodium.opengl.shader.uniform.UniformMatrix4;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.texunits.TextureUnit;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class IrisChunkShaderInterface {
	public final UniformMatrix4 uniformModelViewMatrix;
	public final UniformMatrix4 uniformProjectionMatrix;
	public final UniformMatrix4 uniformNormalMatrix;
	public final UniformFloatArray uniformRegionOffset;
	public final UniformBlock uniformBlockDrawParameters;
	private final UniformFloatArray uFogColor;
	private final UniformFloat uFogStart;
	private final UniformFloat uFogEnd;

	private final BlendModeOverride blendModeOverride;
	private final ProgramUniforms irisProgramUniforms;
	private final ProgramSamplers irisProgramSamplers;
	private final ProgramImages irisProgramImages;

	public IrisChunkShaderInterface(int handle, ShaderBindingContext contextExt, SodiumTerrainPipeline pipeline,
									boolean isShadowPass, BlendModeOverride blendModeOverride) {
		this.uniformModelViewMatrix = contextExt.bindUniform("u_ModelViewMatrix", UniformMatrix4.of());
		this.uniformProjectionMatrix = contextExt.bindUniform("u_ProjectionMatrix", UniformMatrix4.of());
		this.uniformNormalMatrix = contextExt.bindUniform("u_NormalMatrix", UniformMatrix4.of());
		this.uniformRegionOffset = contextExt.bindUniform("u_RegionOffset", UniformFloatArray.ofSize(3));
		UniformInt uniformBlockTex = contextExt.bindUniform("u_BlockTex", UniformInt.of());
		uniformBlockTex.setInt(0);
		UniformInt uniformLightTex = contextExt.bindUniform("u_LightTex", UniformInt.of());
		uniformLightTex.setInt(1);
		this.uniformBlockDrawParameters = ((ShaderBindingContextExt) contextExt).bindUniformBlockIfPresent("ubo_DrawParameters", 0);
		this.uFogColor = contextExt.bindUniform("u_FogColor", UniformFloatArray.ofSize(4));
		this.uFogStart = contextExt.bindUniform("u_FogStart", UniformFloat.of());
		this.uFogEnd = contextExt.bindUniform("u_FogEnd", UniformFloat.of());

		this.blendModeOverride = blendModeOverride;

		this.irisProgramUniforms = pipeline.initUniforms(handle);
		this.irisProgramSamplers
				= isShadowPass? pipeline.initShadowSamplers(handle) : pipeline.initTerrainSamplers(handle);
		this.irisProgramImages = isShadowPass ? pipeline.initShadowImages(handle) : pipeline.initTerrainImages(handle);
	}

	public void setup() {
		// See IrisSamplers#addLevelSamplers
		RenderSystem.activeTexture(TextureUnit.TERRAIN.getUnitId());
		RenderSystem.bindTexture(RenderSystem.getShaderTexture(0));
		RenderSystem.activeTexture(TextureUnit.LIGHTMAP.getUnitId());
		RenderSystem.bindTexture(RenderSystem.getShaderTexture(2));

		if (blendModeOverride != null) {
			blendModeOverride.apply();
		}

		irisProgramUniforms.update();
		irisProgramSamplers.update();
		irisProgramImages.update();
	}

	public void restore() {
		if (blendModeOverride != null) {
			BlendModeOverride.restore();
		}
	}

	public void setProjectionMatrix(Matrix4f matrix) {
		if (this.uniformProjectionMatrix != null) {
			this.uniformProjectionMatrix.set(matrix);
		}
	}

	public void setModelViewMatrix(Matrix4f modelView) {
		if (this.uniformModelViewMatrix != null) {
			this.uniformModelViewMatrix.set(modelView);
		}

		if (this.uniformNormalMatrix != null) {
			Matrix4f normalMatrix = new Matrix4f(modelView);
			normalMatrix.invert();
			normalMatrix.transpose();
			this.uniformNormalMatrix.set(normalMatrix);
		}
	}

	public void setDrawUniforms(Buffer buffer) {
		this.uniformBlockDrawParameters.bindBuffer(buffer);
	}

	public void setRegionOffset(float x, float y, float z) {
		this.uniformRegionOffset.setFloats(x, y, z);
	}
}
