package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.GlObject;
import me.jellysquid.mods.sodium.client.gl.shader.*;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderBindingPoints;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisModelVertexFormats;
import net.coderbot.iris.gl.framebuffer.GlFramebuffer;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Optional;

public class IrisChunkProgramOverrides {
	private boolean shadersCreated = false;
    private final EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> programs = new EnumMap<>(IrisTerrainPass.class);

    private GlShader createVertexShader(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        Optional<String> irisVertexShader;

        if (pass == IrisTerrainPass.SHADOW || pass == IrisTerrainPass.SHADOW_CUTOUT) {
            irisVertexShader = pipeline.getShadowVertexShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_SOLID || pass == IrisTerrainPass.GBUFFER_CUTOUT) {
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


		return new GlShader(ShaderType.VERTEX, new ResourceLocation("iris", "sodium-terrain.vsh"), source);
    }

    private GlShader createGeometryShader(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        Optional<String> irisGeometryShader;

        if (pass == IrisTerrainPass.SHADOW || pass == IrisTerrainPass.SHADOW_CUTOUT) {
            irisGeometryShader = pipeline.getShadowGeometryShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_SOLID || pass == IrisTerrainPass.GBUFFER_CUTOUT) {
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

    private GlShader createFragmentShader(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        Optional<String> irisFragmentShader;

        if (pass == IrisTerrainPass.SHADOW) {
            irisFragmentShader = pipeline.getShadowFragmentShaderSource();
        } else if (pass == IrisTerrainPass.SHADOW_CUTOUT) {
        	irisFragmentShader = pipeline.getShadowCutoutFragmentShaderSource();
		} else if (pass == IrisTerrainPass.GBUFFER_SOLID) {
            irisFragmentShader = pipeline.getTerrainFragmentShaderSource();
        } else if (pass == IrisTerrainPass.GBUFFER_CUTOUT) {
        	irisFragmentShader = pipeline.getTerrainCutoutFragmentShaderSource();
		} else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
            irisFragmentShader = pipeline.getTranslucentFragmentShaderSource();
        } else {
            throw new IllegalArgumentException("Unknown pass type " + pass);
        }

        String source = irisFragmentShader.orElse(null);

        if (source == null) {
            return null;
        }

        return new GlShader(ShaderType.FRAGMENT, new ResourceLocation("iris", "sodium-terrain.fsh"), source);
    }

	private int[] getBlendModeOverride(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
		int[] blendModeOverride = null;

		if (pass == IrisTerrainPass.SHADOW || pass == IrisTerrainPass.SHADOW_CUTOUT) {
			blendModeOverride = pipeline.getShadowBlendOverride();
		} else if (pass == IrisTerrainPass.GBUFFER_SOLID || pass == IrisTerrainPass.GBUFFER_CUTOUT) {
			blendModeOverride = pipeline.getTerrainBlendOverride();
		} else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
			blendModeOverride = pipeline.getTranslucentBlendOverride();
		} else {
			throw new IllegalArgumentException("Unknown pass type " + pass);
		}

		return blendModeOverride;
	}

    @Nullable
    private GlProgram<IrisChunkShaderInterface> createShader(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        GlShader vertShader = createVertexShader(pass, pipeline);
        GlShader geomShader = createGeometryShader(pass, pipeline);
        GlShader fragShader = createFragmentShader(pass, pipeline);
		int[] blendModeOverride = getBlendModeOverride(pass, pipeline);

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
					.bindAttribute("a_PosId", ChunkShaderBindingPoints.ATTRIBUTE_POSITION_ID)
					.bindAttribute("a_Color", ChunkShaderBindingPoints.ATTRIBUTE_COLOR)
					.bindAttribute("a_TexCoord", ChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE)
					.bindAttribute("a_LightCoord", ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE)
                    .bindAttribute("mc_Entity", IrisChunkShaderBindingPoints.BLOCK_ID)
                    .bindAttribute("mc_midTexCoord", IrisChunkShaderBindingPoints.MID_TEX_COORD)
                    .bindAttribute("at_tangent", IrisChunkShaderBindingPoints.TANGENT)
                    .bindAttribute("a_Normal", IrisChunkShaderBindingPoints.NORMAL)
					.bindFragmentData("iris_FragData", ChunkShaderBindingPoints.FRAG_COLOR)
					.link((shader) -> {
						// TODO: Better way for this? It's a bit too much casting for me.
						int handle = ((GlObject) shader).handle();
						ShaderBindingContextExt contextExt = (ShaderBindingContextExt) shader;

						return new IrisChunkShaderInterface(handle, contextExt, pipeline,
								pass == IrisTerrainPass.SHADOW || pass == IrisTerrainPass.SHADOW_CUTOUT, blendModeOverride);
					});
        } finally {
            vertShader.delete();
            if (geomShader != null) {
                geomShader.delete();
            }
            fragShader.delete();
        }
    }

    private SodiumTerrainPipeline getSodiumTerrainPipeline() {
		WorldRenderingPipeline worldRenderingPipeline = Iris.getPipelineManager().getPipeline();

		if (worldRenderingPipeline != null) {
			return worldRenderingPipeline.getSodiumTerrainPipeline();
		} else {
			return null;
		}
	}

    private void createShaders(ChunkVertexType vertexType) {
    	SodiumTerrainPipeline pipeline = getSodiumTerrainPipeline();
        Iris.getPipelineManager().clearSodiumShaderReloadNeeded();

        if (pipeline != null) {
			pipeline.patchShaders(vertexType);
			for (IrisTerrainPass pass : IrisTerrainPass.values()) {
                this.programs.put(pass, createShader(pass, pipeline));
            }
        } else {
            this.programs.clear();
        }

        shadersCreated = true;
    }

    @Nullable
    public GlProgram<IrisChunkShaderInterface> getProgramOverride(BlockRenderPass pass, ChunkVertexType vertexType) {
        if (Iris.getPipelineManager().isSodiumShaderReloadNeeded()) {
            deleteShaders();
        }

        if (!shadersCreated) {
			createShaders(vertexType);
		}

        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
        	if (pass == BlockRenderPass.CUTOUT || pass == BlockRenderPass.CUTOUT_MIPPED) {
				return this.programs.get(IrisTerrainPass.SHADOW_CUTOUT);
			} else {
				return this.programs.get(IrisTerrainPass.SHADOW);
			}
        } else {
			if (pass == BlockRenderPass.CUTOUT || pass == BlockRenderPass.CUTOUT_MIPPED) {
				return this.programs.get(IrisTerrainPass.GBUFFER_CUTOUT);
			} else if (pass.isTranslucent()) {
				return this.programs.get(IrisTerrainPass.GBUFFER_TRANSLUCENT);
			} else {
				return this.programs.get(IrisTerrainPass.GBUFFER_SOLID);
			}
        }
    }

    public void bindFramebuffer(BlockRenderPass pass) {
		SodiumTerrainPipeline pipeline = getSodiumTerrainPipeline();
		boolean isShadowPass = ShadowRenderingState.areShadowsCurrentlyBeingRendered();

		if (pipeline != null) {
			GlFramebuffer framebuffer;

			if (isShadowPass) {
				framebuffer = pipeline.getShadowFramebuffer();
			} else if (pass.isTranslucent()) {
				framebuffer = pipeline.getTranslucentFramebuffer();
			} else {
				framebuffer = pipeline.getTerrainFramebuffer();
			}

			if (framebuffer != null) {
				framebuffer.bind();
			}
		}
	}

	public void unbindFramebuffer() {
		// TODO: Bind the framebuffer to whatever fallback is specified by SodiumTerrainPipeline.
		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
	}

    public void deleteShaders() {
        for (GlProgram<?> program : this.programs.values()) {
            if (program != null) {
                program.delete();
            }
        }

        this.programs.clear();
        shadersCreated = false;
    }
}
