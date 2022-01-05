package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import me.jellysquid.mods.sodium.opengl.device.RenderDevice;
import me.jellysquid.mods.sodium.opengl.pipeline.PipelineCommandList;
import me.jellysquid.mods.sodium.opengl.pipeline.PipelineState;
import me.jellysquid.mods.sodium.opengl.shader.Program;
import me.jellysquid.mods.sodium.opengl.shader.ProgramCommandList;
import me.jellysquid.mods.sodium.opengl.shader.uniform.UniformBlock;
import me.jellysquid.mods.sodium.opengl.shader.uniform.UniformFloatArray;
import me.jellysquid.mods.sodium.opengl.shader.uniform.UniformMatrix4;
import me.jellysquid.mods.sodium.render.chunk.draw.ChunkCameraContext;
import me.jellysquid.mods.sodium.render.chunk.draw.ChunkRenderList;
import me.jellysquid.mods.sodium.render.chunk.draw.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.render.chunk.draw.DefaultChunkRenderer;
import me.jellysquid.mods.sodium.render.chunk.draw.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.render.chunk.passes.ChunkRenderPass;
import me.jellysquid.mods.sodium.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.render.terrain.format.TerrainVertexType;
import me.jellysquid.mods.sodium.render.vertex.type.VertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderChunkRendererExt;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.shadows.ShadowRenderingState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultChunkRenderer.class)
public abstract class MixinRegionChunkRenderer extends ShaderChunkRenderer implements ShaderChunkRendererExt {
	@Unique
	private IrisChunkProgramOverrides irisChunkProgramOverrides;

	@Unique
	private Program<IrisChunkShaderInterface> override;

	public MixinRegionChunkRenderer(RenderDevice device, TerrainVertexType vertexType) {
		super(device, vertexType);
	}

	@Inject(method = "<init>", at = @At("RETURN"), remap = false)
	private void iris$onInit(RenderDevice device, TerrainVertexType vertexType, CallbackInfo ci) {
		irisChunkProgramOverrides = new IrisChunkProgramOverrides();
	}

	@Redirect(method = "render", remap = false,
			at = @At(value = "INVOKE",
					target = "Lme/jellysquid/mods/sodium/render/chunk/draw/DefaultChunkRenderer;compileProgram(Lme/jellysquid/mods/sodium/render/chunk/shader/ChunkShaderOptions;)Lme/jellysquid/mods/sodium/opengl/shader/Program;"))
	private Program iris$getInterface(DefaultChunkRenderer instance, ChunkShaderOptions chunkShaderOptions) {
		if (Iris.isPackActive()) {
			// Iris sentinel null
			return irisChunkProgramOverrides.getProgramOverride(device, chunkShaderOptions.pass(), this.vertexType);
		} else {
			return this.compileProgram(chunkShaderOptions);
		}
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
	private void iris$begin(ChunkRenderMatrices matrices, RenderDevice device, ChunkRenderList list, ChunkRenderPass renderPass, ChunkCameraContext camera, CallbackInfo ci) {
		irisChunkProgramOverrides.bindFramebuffer(renderPass);

		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			// No back face culling during the shadow pass
			// TODO: Hopefully this won't be necessary in the future...
			RenderSystem.disableCull();
		}
	}

	@Inject(method = "lambda$render$2", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/opengl/shader/uniform/UniformBlock;bindBuffer(Lme/jellysquid/mods/sodium/opengl/buffer/Buffer;)V"), cancellable = true, remap = false)
	private void iris$setNormalMatrix(ChunkRenderMatrices matrices, ChunkRenderList list, ChunkRenderPass renderPass, ChunkCameraContext camera, ProgramCommandList programCommands, ChunkShaderInterface programInterface, CallbackInfo ci) {
		if (Iris.isPackActive()) {
			iris$getOverride().getInterface().uniformNormalMatrix.set(new Matrix4f(matrices.modelView()).invert().transpose());
		}
	}

	@Inject(method = "render", at = @At("TAIL"), remap = false, cancellable = true)
	private void iris$onEnd(CallbackInfo ci) {
		ProgramUniforms.clearActiveUniforms();
		irisChunkProgramOverrides.unbindFramebuffer();
	}

	@Redirect(method = "lambda$render$2",
			at = @At(value = "FIELD",
					target = "Lme/jellysquid/mods/sodium/render/chunk/shader/ChunkShaderInterface;uniformProjectionMatrix:Lme/jellysquid/mods/sodium/opengl/shader/uniform/UniformMatrix4;"), remap = false)
	private UniformMatrix4 iris$setProjectionMatrix(ChunkShaderInterface instance) {
		if (instance != null) {
			return instance.uniformProjectionMatrix;
		} else {
			return iris$getOverride().getInterface().uniformProjectionMatrix;
		}
	}

	@Redirect(method = "lambda$render$2",
			at = @At(value = "FIELD",
					target = "Lme/jellysquid/mods/sodium/render/chunk/shader/ChunkShaderInterface;uniformModelViewMatrix:Lme/jellysquid/mods/sodium/opengl/shader/uniform/UniformMatrix4;"), remap = false)
	private UniformMatrix4 iris$setModelViewMatrix(ChunkShaderInterface instance) {
		if (instance != null) {
			return instance.uniformModelViewMatrix;
		} else {
			return iris$getOverride().getInterface().uniformModelViewMatrix;
		}
	}

	@Inject(method = "delete", at = @At("HEAD"), remap = false)
	private void iris$onDelete(CallbackInfo ci) {
		irisChunkProgramOverrides.deleteShaders(this.device);
	}

	@Redirect(method = "lambda$render$2", remap = false,
			at = @At(value = "FIELD",
					target = "Lme/jellysquid/mods/sodium/render/chunk/shader/ChunkShaderInterface;uniformBlockDrawParameters:Lme/jellysquid/mods/sodium/opengl/shader/uniform/UniformBlock;"))
	private UniformBlock iris$setDrawUniforms(ChunkShaderInterface instance) {
		if (instance != null) {
			return instance.uniformBlockDrawParameters;
		} else {
			return iris$getOverride().getInterface().uniformBlockDrawParameters;
		}
	}

	@Redirect(method = "setModelMatrixUniforms",
			at = @At(value = "FIELD",
					target = "Lme/jellysquid/mods/sodium/render/chunk/shader/ChunkShaderInterface;uniformRegionOffset:Lme/jellysquid/mods/sodium/opengl/shader/uniform/UniformFloatArray;"), remap = false)
	private UniformFloatArray iris$setRegionOffset(ChunkShaderInterface instance) {
		if (instance != null) {
			return instance.uniformRegionOffset;
		} else {
			return iris$getOverride().getInterface().uniformRegionOffset;
		}
	}

	@Override
	public Program<IrisChunkShaderInterface> iris$getOverride() {
		return override;
	}
}
