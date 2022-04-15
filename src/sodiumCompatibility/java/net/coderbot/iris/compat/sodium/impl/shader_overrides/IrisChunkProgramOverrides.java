package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.GlShader;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderConstants;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderType;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkProgram;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Locale;

public class IrisChunkProgramOverrides {
    private static final ShaderConstants EMPTY_CONSTANTS = ShaderConstants.builder().build();

    private final EnumMap<IrisTerrainPass, ChunkProgram> programs = new EnumMap<>(IrisTerrainPass.class);

    private GlShader createVertexShader(RenderDevice device, IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        String source;

        if (pass == IrisTerrainPass.SHADOW_SOLID) {
			source = pipeline.getShadowTerrainSourceSet().vertex;
        } else if (pass == IrisTerrainPass.SHADOW_TRANSLUCENT) {
			source = pipeline.getShadowTranslucentSourceSet().vertex;
        } else if (pass == IrisTerrainPass.GBUFFER_SOLID) {
			source = pipeline.getTerrainShaderSet().vertex;
        } else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
			source = pipeline.getTranslucentShaderSet().vertex;
        } else {
            throw new IllegalArgumentException("Unknown pass type " + pass);
        }

        if (source == null) {
            return null;
        }

        return new GlShader(device, ShaderType.VERTEX, new ResourceLocation("iris",
			"sodium-terrain-" + pass.toString().toLowerCase(Locale.ROOT) + ".vsh"), source, EMPTY_CONSTANTS);
    }

    private GlShader createGeometryShader(RenderDevice device, IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        String source;

		if (pass == IrisTerrainPass.SHADOW_SOLID) {
			source = pipeline.getShadowTerrainSourceSet().geometry;
		} else if (pass == IrisTerrainPass.SHADOW_TRANSLUCENT) {
			source = pipeline.getShadowTranslucentSourceSet().geometry;
		} else if (pass == IrisTerrainPass.GBUFFER_SOLID) {
			source = pipeline.getTerrainShaderSet().geometry;
		} else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
			source = pipeline.getTranslucentShaderSet().geometry;
		} else {
            throw new IllegalArgumentException("Unknown pass type " + pass);
        }

        if (source == null) {
            return null;
        }

        return new GlShader(device, IrisShaderTypes.GEOMETRY, new ResourceLocation("iris",
			"sodium-terrain-" + pass.toString().toLowerCase(Locale.ROOT) + ".gsh"), source, EMPTY_CONSTANTS);
    }

    private GlShader createFragmentShader(RenderDevice device, IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        String source;

        if (pass == IrisTerrainPass.SHADOW_SOLID) {
            source = pipeline.getShadowTerrainSourceSet().fragment;
		} else if (pass == IrisTerrainPass.SHADOW_TRANSLUCENT) {
			source = pipeline.getShadowTranslucentSourceSet().fragment;
		} else if (pass == IrisTerrainPass.GBUFFER_SOLID) {
			source = pipeline.getTerrainShaderSet().fragment;;
        } else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
			source = pipeline.getTranslucentShaderSet().fragment;
        } else {
            throw new IllegalArgumentException("Unknown pass type " + pass);
        }

        if (source == null) {
            return null;
        }

        return new GlShader(device, ShaderType.FRAGMENT, new ResourceLocation("iris",
			"sodium-terrain-" + pass.toString().toLowerCase(Locale.ROOT) + ".fsh"), source, EMPTY_CONSTANTS);
    }

    @Nullable
    private ChunkProgram createShader(RenderDevice device, IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
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
                    .bindAttribute("a_Pos", ChunkShaderBindingPoints.POSITION)
                    .bindAttribute("a_Color", ChunkShaderBindingPoints.COLOR)
                    .bindAttribute("a_TexCoord", ChunkShaderBindingPoints.TEX_COORD)
                    .bindAttribute("a_LightCoord", ChunkShaderBindingPoints.LIGHT_COORD)
                    .bindAttribute("mc_Entity", IrisChunkShaderBindingPoints.BLOCK_ID)
                    .bindAttribute("mc_midTexCoord", IrisChunkShaderBindingPoints.MID_TEX_COORD)
                    .bindAttribute("at_tangent", IrisChunkShaderBindingPoints.TANGENT)
                    .bindAttribute("a_Normal", IrisChunkShaderBindingPoints.NORMAL)
                    .bindAttribute("d_ModelOffset", ChunkShaderBindingPoints.MODEL_OFFSET)
                    .build((program, name) -> {
                        ProgramUniforms uniforms = pipeline.initUniforms(name);
                        ProgramSamplers samplers;
						ProgramImages images;

                        if (pass.isShadow()) {
                            samplers = pipeline.initShadowSamplers(name);
							images = pipeline.initShadowImages(name);
                        } else {
                            samplers = pipeline.initTerrainSamplers(name);
							images = pipeline.initTerrainImages(name);
                        }

                        return new IrisChunkProgram(device, program, name, uniforms, samplers, images);
                    });
        } finally {
            vertShader.delete();
            if (geomShader != null) {
                geomShader.delete();
            }
            fragShader.delete();
        }
    }

    public void createShaders(RenderDevice device) {
        WorldRenderingPipeline worldRenderingPipeline = Iris.getPipelineManager().getPipelineNullable();
        SodiumTerrainPipeline sodiumTerrainPipeline = null;

        if (worldRenderingPipeline != null) {
            sodiumTerrainPipeline = worldRenderingPipeline.getSodiumTerrainPipeline();
        }

        Iris.getPipelineManager().clearSodiumShaderReloadNeeded();

        if (sodiumTerrainPipeline != null) {
            for (IrisTerrainPass pass : IrisTerrainPass.values()) {
				if (pass.isShadow() && !sodiumTerrainPipeline.hasShadowPass()) {
					this.programs.put(pass, null);
					continue;
				}

                this.programs.put(pass, createShader(device, pass, sodiumTerrainPipeline));
            }
        } else {
            this.programs.clear();
        }
    }

    @Nullable
    public ChunkProgram getProgramOverride(RenderDevice device, BlockRenderPass pass) {
        if (Iris.getPipelineManager().isSodiumShaderReloadNeeded()) {
            deleteShaders();
            createShaders(device);
        }

        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			ChunkProgram shadowProgram = this.programs.get(pass.isTranslucent() ? IrisTerrainPass.SHADOW_TRANSLUCENT : IrisTerrainPass.SHADOW_SOLID);

			if (shadowProgram == null) {
				throw new IllegalStateException("Shadow program requested, but the pack does not have a shadow pass?");
			}

			return shadowProgram;
        } else {
            return this.programs.get(pass.isTranslucent() ? IrisTerrainPass.GBUFFER_TRANSLUCENT : IrisTerrainPass.GBUFFER_SOLID);
        }
    }

    public void deleteShaders() {
        for (ChunkProgram program : this.programs.values()) {
            if (program != null) {
                program.delete();
            }
        }

        this.programs.clear();
    }
}
