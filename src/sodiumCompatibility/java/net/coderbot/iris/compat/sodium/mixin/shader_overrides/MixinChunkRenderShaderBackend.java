package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgram;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ChunkRenderBackendExt;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the Iris shader program overrides to Sodium's chunk rendering pipeline.
 */
@Mixin(ShaderChunkRenderer.class)
public class MixinChunkRenderShaderBackend implements ChunkRenderBackendExt {
    @Unique
    private IrisChunkProgramOverrides irisChunkProgramOverrides;

    @Unique
    private RenderDevice device;

    @Unique
    private GlProgram<ChunkShaderInterface> override;

    @Shadow(remap = false)
    private ChunkShaderInterface activeProgram;

    @Shadow
    private void begin(BlockRenderPass pass) {
        throw new AssertionError();
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void iris$onInit(RenderDevice device, ChunkVertexType vertexType, CallbackInfo ci) {
        irisChunkProgramOverrides = new IrisChunkProgramOverrides();
    }

    @Overwrite(remap = false)
	private GlProgram<ChunkShaderInterface> createShader(String path, ChunkShaderOptions options) {
		this.device = device;
        irisChunkProgramOverrides.createShaders(device, path, options);
		return irisChunkProgramOverrides.getProgramOverride(device, options.pass());
    }

    @Override
    public void iris$begin(PoseStack poseStack, BlockRenderPass pass) {
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            // No back face culling during the shadow pass
            // TODO: Hopefully this won't be necessary in the future...
            RenderSystem.disableCull();
        }

        this.override = irisChunkProgramOverrides.getProgramOverride(device, pass);

        begin(pass);

		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			// No back face culling during the shadow pass
			// TODO: Hopefully this won't be necessary in the future...
			RenderSystem.disableCull();
		}

		WorldRenderingPipeline worldRenderingPipeline = Iris.getPipelineManager().getPipeline();
		SodiumTerrainPipeline sodiumTerrainPipeline = null;

		if (worldRenderingPipeline != null) {
			sodiumTerrainPipeline = worldRenderingPipeline.getSodiumTerrainPipeline();
		}

		if (sodiumTerrainPipeline != null) {
			GlFramebuffer framebuffer = null;

			if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
				framebuffer = sodiumTerrainPipeline.getShadowFramebuffer();
			} else if (pass.isTranslucent()) {
				framebuffer = sodiumTerrainPipeline.getTranslucentFramebuffer();
			} else {
				framebuffer = sodiumTerrainPipeline.getTerrainFramebuffer();
			}

			if (framebuffer != null) {
				framebuffer.bind();
			}
		}
    }

    @Inject(method = "begin",
            at = @At(value = "FIELD",
                    target = "Lme/jellysquid/mods/sodium/client/render/chunk/ShaderChunkRenderer;activeProgram:Lme/jellysquid/mods/sodium/client/gl/shader/GlProgram;",
                    args = "opcode=PUTFIELD",
                    remap = false,
                    shift = At.Shift.AFTER), remap = false)
    private void iris$applyOverride(BlockRenderPass pass, CallbackInfo ci) {
        if (override != null) {
            this.activeProgram = override.getInterface();
        }
    }

    @Inject(method = "end", at = @At("RETURN"), remap = false)
    private void iris$onEnd(CallbackInfo ci) {
        ProgramUniforms.clearActiveUniforms();
    }

    @Inject(method = "delete", at = @At("HEAD"), remap = false)
    private void iris$onDelete(CallbackInfo ci) {
        irisChunkProgramOverrides.deleteShaders();
    }
}
