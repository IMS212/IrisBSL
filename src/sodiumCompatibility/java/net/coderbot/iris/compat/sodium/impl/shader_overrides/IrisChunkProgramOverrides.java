package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.GlShader;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderConstants;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderType;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkFogMode;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.Optional;

public class IrisChunkProgramOverrides {
    private static final ShaderConstants EMPTY_CONSTANTS = ShaderConstants.builder().build();

    private final EnumMap<IrisTerrainPass, GlProgram<ChunkShaderInterface>> programs = new EnumMap<>(IrisTerrainPass.class);

    private GlShader createVertexShader(RenderDevice device, IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        Optional<String> irisVertexShader;

        if (pass == IrisTerrainPass.SHADOW) {
            irisVertexShader = pipeline.getShadowVertexShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_SOLID) {
            irisVertexShader = pipeline.getTerrainVertexShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
            irisVertexShader = pipeline.getTranslucentVertexShaderSource();
        } else {
            throw new IllegalArgumentException("Unknown pass type " + pass);
        }

        String source = irisVertexShader.orElse(null);

        if (source == null) {
            return null;
        }

        return new GlShader(ShaderType.VERTEX, new ResourceLocation("iris", "sodium-terrain.vsh"),
                source);
    }

    private GlShader createGeometryShader(RenderDevice device, IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        Optional<String> irisGeometryShader;

        if (pass == IrisTerrainPass.SHADOW) {
            irisGeometryShader = pipeline.getShadowGeometryShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_SOLID) {
            irisGeometryShader = pipeline.getTerrainGeometryShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
            irisGeometryShader = pipeline.getTranslucentGeometryShaderSource();
        } else {
            throw new IllegalArgumentException("Unknown pass type " + pass);
        }

        String source = irisGeometryShader.orElse(null);

        if (source == null) {
            return null;
        }

        return new GlShader(IrisShaderTypes.GEOMETRY, new ResourceLocation("iris", "sodium-terrain.gsh"),
                source);
    }

    private GlShader createFragmentShader(RenderDevice device, IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        Optional<String> irisFragmentShader;

        if (pass == IrisTerrainPass.SHADOW) {
            irisFragmentShader = pipeline.getShadowFragmentShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_SOLID) {
            irisFragmentShader = pipeline.getTerrainFragmentShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
            irisFragmentShader = pipeline.getTranslucentFragmentShaderSource();
        } else {
            throw new IllegalArgumentException("Unknown pass type " + pass);
        }

        String source = irisFragmentShader.orElse(null);

        if (source == null) {
            return null;
        }

        return new GlShader(ShaderType.FRAGMENT, new ResourceLocation("iris", "sodium-terrain.fsh"),
                source);
    }

    private GlProgram<ChunkShaderInterface> createShader(RenderDevice device, IrisTerrainPass pass, SodiumTerrainPipeline pipeline, ChunkShaderOptions options) {
        GlShader vertShader = createVertexShader(device, pass, pipeline);
        GlShader geomShader = createGeometryShader(device, pass, pipeline);
        GlShader fragShader = createFragmentShader(device, pass, pipeline);

        if (vertShader == null || fragShader == null) {
            if (vertShader != null) {
                vertShader.delete();
            }

            if (geomShader != null) {
                geomShader.delete();
            }

            if (fragShader != null) {
                fragShader.delete();
            }

            // TODO: Partial shader programs?
            return null;
        }

        try {
            GlProgram.Builder builder = GlProgram.builder(new ResourceLocation("sodium", "chunk_shader_for_"
                    + pass.getName()));

            if (geomShader != null) {
                builder.attachShader(geomShader);
            }

            return builder.attachShader(vertShader)
                    .attachShader(fragShader)
                    .bindAttribute("a_Pos", ChunkShaderBindingPoints.ATTRIBUTE_POSITION_ID)
                    .bindAttribute("a_Color", ChunkShaderBindingPoints.ATTRIBUTE_COLOR)
                    .bindAttribute("a_TexCoord", ChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE)
                    .bindAttribute("a_LightCoord", ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE)
                    .bindAttribute("mc_Entity", IrisChunkShaderBindingPoints.BLOCK_ID)
                    .bindAttribute("mc_midTexCoord", IrisChunkShaderBindingPoints.MID_TEX_COORD)
                    .bindAttribute("at_tangent", IrisChunkShaderBindingPoints.TANGENT)
                    .bindAttribute("a_Normal", IrisChunkShaderBindingPoints.NORMAL)
					.bindFragmentData("fragColor", ChunkShaderBindingPoints.FRAG_COLOR)
					.bindFragmentData("iris_FragData", ChunkShaderBindingPoints.FRAG_COLOR)
					.link((shader) -> {
                        ProgramUniforms uniforms = pipeline.initUniforms(((GlProgram) shader).handle());
                        ProgramSamplers samplers;

                        if (pass == IrisTerrainPass.SHADOW) {
                            samplers = pipeline.initShadowSamplers(((GlProgram) shader).handle());
                        } else {
                            samplers = pipeline.initTerrainSamplers(((GlProgram) shader).handle());
                        }

                        return new IrisChunkProgram(device, shader, options, uniforms, samplers);
                    });
        } finally {
            vertShader.delete();
            if (geomShader != null) {
                geomShader.delete();
            }
            fragShader.delete();
        }
    }

    public void createShaders(RenderDevice device, String path, ChunkShaderOptions options) {
        WorldRenderingPipeline worldRenderingPipeline = Iris.getPipelineManager().getPipeline();
        SodiumTerrainPipeline sodiumTerrainPipeline = null;

        if (worldRenderingPipeline != null) {
            sodiumTerrainPipeline = worldRenderingPipeline.getSodiumTerrainPipeline();
        }

        Iris.getPipelineManager().clearSodiumShaderReloadNeeded();

		GlProgram<ChunkShaderInterface> program = programs.get(options);

		if (program == null) {
			programs.put(getPass(options.pass()), program = this.createShader(device, getPass(options.pass()), sodiumTerrainPipeline, options));
		}
    }

    public GlProgram<ChunkShaderInterface> getProgramOverride(RenderDevice device, BlockRenderPass pass) {
        if (Iris.getPipelineManager().isSodiumShaderReloadNeeded()) {
            deleteShaders();
        }

		return this.programs.get(getPass(pass));
    }

	public IrisTerrainPass getPass(BlockRenderPass pass) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return IrisTerrainPass.SHADOW;
		} else {
			return pass.isTranslucent() ? IrisTerrainPass.GBUFFER_TRANSLUCENT : IrisTerrainPass.GBUFFER_SOLID;
		}
	}

    public void deleteShaders() {
        for (GlProgram<ChunkShaderInterface> program : this.programs.values()) {
            if (program != null) {
                program.delete();
            }
        }

        this.programs.clear();
    }
}
