package net.coderbot.iris.pipeline.newshader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.shaderpack.loading.ProgramId;
import net.coderbot.iris.vertices.IrisVertexFormats;

import java.util.Locale;

public enum ShaderKey {
	// if you auto-format this and destroy all the manual indentation, I'll steal your kneecaps

	BASIC(ProgramId.Basic, false),
	TEXTURED(ProgramId.Textured, false),
	SKY_BASIC(ProgramId.SkyBasic, false),
	SKY_TEXTURED(ProgramId.SkyTextured, true),
	CLOUDS(ProgramId.Clouds, true),
	TERRAIN(ProgramId.Terrain, false),
	TERRAIN_CUTOUT(ProgramId.TerrainCutout, true),
	TRANSLUCENT(ProgramId.Water, false),
	BLOCK_ENTITIES(ProgramId.Block, true),
	ENTITIES(ProgramId.Entities, false),
	ENTITIES_TRANSLUCENT(ProgramId.EntitiesTrans, true),
	ENTITIES_CUTOUT(ProgramId.Entities, true),
	HAND_CUTOUT(ProgramId.Hand, true),
	HAND_TRANSLUCENT(ProgramId.HandWater, false),
	WEATHER(ProgramId.Weather, true),
	CRUMBLING(ProgramId.Basic, true),
	TEXT(ProgramId.Entities, true),
	BEACON(ProgramId.BeaconBeam, false),
	GLINT(ProgramId.ArmorGlint, true),
	SHADOW(ProgramId.ShadowSolid, false),
	SHADOW_CUTOUT(ProgramId.ShadowCutout, true);
	private final ProgramId program;
	private final boolean hasAlphaTest;

	ShaderKey(ProgramId program, boolean hasAlphaTest) {
		this.program = program;
		this.hasAlphaTest = hasAlphaTest;
	}

	public ProgramId getProgram() {
		return program;
	}

	public boolean hasAlphaTest() {
		return hasAlphaTest;
	}

	public boolean isShadow() {
		return this == SHADOW || this == SHADOW_CUTOUT;
	}
}
