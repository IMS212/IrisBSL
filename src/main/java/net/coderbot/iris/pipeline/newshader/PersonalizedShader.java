package net.coderbot.iris.pipeline.newshader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;

public class PersonalizedShader extends ShaderInstance {
	private final ShaderTemplate template;
	private final ShaderAttributeInputs inputs;

	public PersonalizedShader(ShaderTemplate template, ShaderAttributeInputs inputs) throws IOException {
		// Create a fake program...
		super(Minecraft.getInstance().getResourceManager(), "rendertype_entity_alpha", DefaultVertexFormat.NEW_ENTITY); // Fake shader...
		// aaand immediately close it.
		close();

		this.template = template;
		this.inputs = inputs;
	}

	@Override
	public void clear() {
		ProgramUniforms.clearActiveUniforms();
		ProgramSamplers.clearActiveSamplers();

		if (this.template.blendModeOverride != null || !template.bufferBlendOverrides.isEmpty()) {
			BlendModeOverride.restore();
		}

		Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
	}

	@Override
	public void apply() {
		template.setUniforms(inputs);
		template.apply();
	}

	@Override
	public int getId() {
		return template.getProgramId();
	}
}
