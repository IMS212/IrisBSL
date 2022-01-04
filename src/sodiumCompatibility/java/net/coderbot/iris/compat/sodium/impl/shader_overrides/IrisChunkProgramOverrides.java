package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import me.jellysquid.mods.sodium.opengl.ManagedObject;
import me.jellysquid.mods.sodium.opengl.device.RenderDevice;
import me.jellysquid.mods.sodium.opengl.shader.Program;
import me.jellysquid.mods.sodium.opengl.shader.ShaderDescription;
import me.jellysquid.mods.sodium.opengl.shader.ShaderType;
import me.jellysquid.mods.sodium.render.chunk.passes.ChunkRenderPass;
import me.jellysquid.mods.sodium.render.chunk.shader.ChunkShaderBindingPoints;
import me.jellysquid.mods.sodium.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.render.terrain.format.TerrainVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.BlendModeOverride;
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
    private final EnumMap<IrisTerrainPass, Program<IrisChunkShaderInterface>> programs = new EnumMap<>(IrisTerrainPass.class);

    private String createVertexShader(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
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

		return irisVertexShader.orElse(null);
    }

    private String createGeometryShader(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
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

        return irisGeometryShader.orElse(null);
    }

    private String createFragmentShader(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
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

        return irisFragmentShader.orElse(null);
	}

	private BlendModeOverride getBlendOverride(IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
		if (pass == IrisTerrainPass.SHADOW || pass == IrisTerrainPass.SHADOW_CUTOUT) {
			return pipeline.getShadowBlendOverride();
		} else if (pass == IrisTerrainPass.GBUFFER_SOLID || pass == IrisTerrainPass.GBUFFER_CUTOUT) {
			return pipeline.getTerrainBlendOverride();
		} else if (pass == IrisTerrainPass.GBUFFER_TRANSLUCENT) {
			return pipeline.getTranslucentBlendOverride();
		} else {
			throw new IllegalArgumentException("Unknown pass type " + pass);
		}
	}

    @Nullable
    private Program<IrisChunkShaderInterface> createShader(RenderDevice device, IrisTerrainPass pass, SodiumTerrainPipeline pipeline) {
        String vertShader = createVertexShader(pass, pipeline);
		String geomShader = createGeometryShader(pass, pipeline);
		String fragShader = createFragmentShader(pass, pipeline);
		BlendModeOverride blendOverride = getBlendOverride(pass, pipeline);

            ShaderDescription.Builder builder = ShaderDescription.builder();

            if (geomShader != null) {
                builder.addShaderSource(IrisShaderTypes.GEOMETRY, geomShader);
            }

            ShaderDescription description = builder.addShaderSource(ShaderType.VERTEX, vertShader)
                    .addShaderSource(ShaderType.FRAGMENT, fragShader)
					.addAttributeBinding("a_PosId", ChunkShaderBindingPoints.ATTRIBUTE_POSITION_ID)
					.addAttributeBinding("a_Color", ChunkShaderBindingPoints.ATTRIBUTE_COLOR)
					.addAttributeBinding("a_TexCoord", ChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE)
					.addAttributeBinding("a_LightCoord", ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE)
                    .addAttributeBinding("mc_Entity", IrisChunkShaderBindingPoints.BLOCK_ID)
                    .addAttributeBinding("mc_midTexCoord", IrisChunkShaderBindingPoints.MID_TEX_COORD)
                    .addAttributeBinding("at_tangent", IrisChunkShaderBindingPoints.TANGENT)
                    .addAttributeBinding("a_Normal", IrisChunkShaderBindingPoints.NORMAL)
					.addFragmentBinding("iris_FragData", ChunkShaderBindingPoints.FRAG_COLOR)
					.build();
			return device.createProgram(description, (binder) -> {
				// TODO: Better way for this? It's a bit too much casting for me.
				int handle = ((ManagedObject) binder).handle();
				ShaderBindingContextExt contextExt = (ShaderBindingContextExt) binder;

				return new IrisChunkShaderInterface(handle, contextExt, pipeline,
						pass == IrisTerrainPass.SHADOW || pass == IrisTerrainPass.SHADOW_CUTOUT, blendOverride);

			});
    }

    private SodiumTerrainPipeline getSodiumTerrainPipeline() {
		WorldRenderingPipeline worldRenderingPipeline = Iris.getPipelineManager().getPipelineNullable();

		if (worldRenderingPipeline != null) {
			return worldRenderingPipeline.getSodiumTerrainPipeline();
		} else {
			return null;
		}
	}

    private void createShaders(RenderDevice device, TerrainVertexType vertexType) {
    	SodiumTerrainPipeline pipeline = getSodiumTerrainPipeline();
        Iris.getPipelineManager().clearSodiumShaderReloadNeeded();

        if (pipeline != null) {
			pipeline.patchShaders(vertexType);
			for (IrisTerrainPass pass : IrisTerrainPass.values()) {
                this.programs.put(pass, createShader(device, pass, pipeline));
            }
        } else {
            this.programs.clear();
        }

        shadersCreated = true;
    }

    @Nullable
    public Program<IrisChunkShaderInterface> getProgramOverride(RenderDevice device, ChunkRenderPass pass, TerrainVertexType vertexType) {
        if (Iris.getPipelineManager().isSodiumShaderReloadNeeded()) {
            deleteShaders(device);
        }

        if (!shadersCreated) {
			createShaders(device, vertexType);
		}

        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
        	if (pass == ChunkRenderPass.CUTOUT || pass == ChunkRenderPass.CUTOUT_MIPPED) {
				return this.programs.get(IrisTerrainPass.SHADOW_CUTOUT);
			} else {
				return this.programs.get(IrisTerrainPass.SHADOW);
			}
        } else {
			if (pass == ChunkRenderPass.CUTOUT || pass == ChunkRenderPass.CUTOUT_MIPPED) {
				return this.programs.get(IrisTerrainPass.GBUFFER_CUTOUT);
			} else if (pass.isTranslucent()) {
				return this.programs.get(IrisTerrainPass.GBUFFER_TRANSLUCENT);
			} else {
				return this.programs.get(IrisTerrainPass.GBUFFER_SOLID);
			}
        }
    }

    public void bindFramebuffer(ChunkRenderPass pass) {
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
		SodiumTerrainPipeline pipeline = getSodiumTerrainPipeline();

		if (pipeline != null) {
			// TODO: Bind the framebuffer to whatever fallback is specified by SodiumTerrainPipeline.
			Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
		}
	}

    public void deleteShaders(RenderDevice device) {
        for (Program<?> program : this.programs.values()) {
            if (program != null) {
                device.deleteProgram(program);
            }
        }

        this.programs.clear();
        shadersCreated = false;
    }
}
