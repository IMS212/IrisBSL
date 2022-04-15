package net.coderbot.iris.pipeline;

import net.coderbot.iris.IrisLogging;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.gl.shader.ShaderSourceSet;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.transform.BuiltinUniformReplacementTransformer;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.builtin.BuiltinReplacementUniforms;

import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;

public class SodiumTerrainPipeline {
	ShaderSourceSet terrain;
	ShaderSourceSet translucent;
	ShaderSourceSet shadowTerrain;
	ShaderSourceSet shadowTranslucent;
	//GlFramebuffer framebuffer;
	ProgramSet programSet;

	private final WorldRenderingPipeline parent;

	private final IntFunction<ProgramSamplers> createTerrainSamplers;
	private final IntFunction<ProgramSamplers> createShadowSamplers;

	private final IntFunction<ProgramImages> createTerrainImages;
	private final IntFunction<ProgramImages> createShadowImages;

	public SodiumTerrainPipeline(WorldRenderingPipeline parent,
								 ProgramSet programSet, IntFunction<ProgramSamplers> createTerrainSamplers,
								 IntFunction<ProgramSamplers> createShadowSamplers,
								 IntFunction<ProgramImages> createTerrainImages,
								 IntFunction<ProgramImages> createShadowImages) {
		this.parent = Objects.requireNonNull(parent);

		Optional<ProgramSource> terrainSource = first(programSet.getGbuffersTerrain(), programSet.getGbuffersTexturedLit(), programSet.getGbuffersTextured(), programSet.getGbuffersBasic());
		Optional<ProgramSource> translucentSource = first(programSet.getGbuffersWater(), terrainSource);
		ProgramSource shadowSource = programSet.getShadow().orElse(null);
		ProgramSource shadowTerrainSource = programSet.getShadowTerrain().orElse(shadowSource);
		ProgramSource shadowTranslucentSource = programSet.getShadowTranslucent().orElse(shadowSource);

		this.programSet = programSet;

		terrainSource.ifPresent(sources -> {
			terrain = new ShaderSourceSet(sources.getVertexSource().orElse(null), sources.getFragmentSource().orElse(null), sources.getGeometrySource().orElse(null));
		});

		translucentSource.ifPresent(sources -> {
			translucent = new ShaderSourceSet(sources.getVertexSource().orElse(null), sources.getFragmentSource().orElse(null), sources.getGeometrySource().orElse(null));
		});

		if (shadowTerrainSource != null && shadowTerrainSource.isValid()) {
			shadowTerrain = new ShaderSourceSet(shadowTerrainSource.getVertexSource().orElse(null), shadowTerrainSource.getFragmentSource().orElse(null), shadowTerrainSource.getGeometrySource().orElse(null));
		}

		if (shadowTranslucentSource != null && shadowTranslucentSource.isValid()) {
			shadowTranslucent = new ShaderSourceSet(shadowTranslucentSource.getVertexSource().orElse(null), shadowTranslucentSource.getFragmentSource().orElse(null), shadowTranslucentSource.getGeometrySource().orElse(null));
		}

		if (terrain.vertex != null) {
			terrain.vertex = transformVertexShader(terrain.vertex);
		}

		if (translucent.vertex != null) {
			translucent.vertex = transformVertexShader(translucent.vertex);
		}

		if (shadowTerrain.vertex != null) {
			shadowTerrain.vertex = transformVertexShader(shadowTerrain.vertex);
		}

		if (shadowTranslucent.vertex != null) {
			shadowTranslucent.vertex = transformVertexShader(shadowTranslucent.vertex);
		}

		if (terrain.fragment != null) {
			terrain.fragment = transformFragmentShader(terrain.fragment);
		}

		if (translucent.fragment != null) {
			translucent.fragment = transformFragmentShader(translucent.fragment);
		}

		if (shadowTerrain.fragment != null) {
			shadowTerrain.fragment = transformFragmentShader(shadowTerrain.fragment);
		}

		if (shadowTranslucent.fragment != null) {
			shadowTranslucent.fragment = transformFragmentShader(shadowTranslucent.fragment);
		}

		this.createTerrainSamplers = createTerrainSamplers;
		this.createShadowSamplers = createShadowSamplers;
		this.createTerrainImages = createTerrainImages;
		this.createShadowImages = createShadowImages;
	}

	private static String transformVertexShader(String base) {
		StringTransformations transformations = new StringTransformations(base);

		String injections = "attribute vec3 a_Pos; // The position of the vertex\n" +
			"attribute vec4 a_Color; // The color of the vertex\n" +
			"attribute vec2 a_TexCoord; // The block texture coordinate of the vertex\n" +
			"attribute vec2 a_LightCoord; // The light map texture coordinate of the vertex\n" +
			"attribute vec3 a_Normal; // The vertex normal\n" +
			"uniform mat4 u_ModelViewMatrix;\n" +
			"uniform mat4 u_ModelViewProjectionMatrix;\n" +
			"uniform mat4 u_NormalMatrix;\n" +
			"uniform vec3 u_ModelScale;\n" +
			"uniform vec2 u_TextureScale;\n" +
			"\n" +
			"// The model translation for this draw call.\n" +
			"attribute vec4 d_ModelOffset;\n" +
			"\n" +
			"vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }";

		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, injections);

		transformations.define("gl_Vertex", "vec4((a_Pos * u_ModelScale) + d_ModelOffset.xyz, 1.0)");
		// transformations.replaceExact("gl_MultiTexCoord1.xy/255.0", "a_LightCoord");
		transformations.define("gl_MultiTexCoord0", "vec4(a_TexCoord * u_TextureScale, 0.0, 1.0)");
		//transformations.replaceExact("gl_MultiTexCoord1", "vec4(a_LightCoord * 255.0, 0.0, 1.0)");
		transformations.define("gl_Color", "a_Color");
		transformations.define("gl_ModelViewMatrix", "u_ModelViewMatrix");
		transformations.define("gl_ModelViewProjectionMatrix", "u_ModelViewProjectionMatrix");
		transformations.replaceExact("gl_TextureMatrix[0]", "mat4(1.0)");
		// transformations.replaceExact("gl_TextureMatrix[1]", "mat4(1.0 / 255.0)");
		transformations.define("gl_NormalMatrix", "mat3(u_NormalMatrix)");
		transformations.define("gl_Normal", "a_Normal");
		// Just being careful
		transformations.define("ftransform", "iris_ftransform");

		new BuiltinUniformReplacementTransformer("a_LightCoord").apply(transformations);

		if (IrisLogging.ENABLE_SPAM) {
			System.out.println("Final patched vertex source:");
			System.out.println(transformations);
		}

		return transformations.toString();
	}

	private static String transformFragmentShader(String base) {
		StringTransformations transformations = new StringTransformations(base);

		String injections =
				"uniform mat4 u_ModelViewMatrix;\n" +
				"uniform mat4 u_ModelViewProjectionMatrix;\n" +
				"uniform mat4 u_NormalMatrix;\n";

		transformations.define("gl_ModelViewMatrix", "u_ModelViewMatrix");
		transformations.define("gl_ModelViewProjectionMatrix", "u_ModelViewProjectionMatrix");
		transformations.replaceExact("gl_TextureMatrix[0]", "mat4(1.0)");
		transformations.define("gl_NormalMatrix", "mat3(u_NormalMatrix)");

		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, injections);

		if (IrisLogging.ENABLE_SPAM) {
			System.out.println("Final patched fragment source:");
			System.out.println(transformations);
		}

		return transformations.toString();
	}

	public ShaderSourceSet getTerrainShaderSet() {
		return terrain;
	}

	public ShaderSourceSet getTranslucentShaderSet() {
		return translucent;
	}

	public ShaderSourceSet getShadowTerrainSourceSet() {
		return shadowTerrain;
	}

	public ShaderSourceSet getShadowTranslucentSourceSet() {
		return shadowTranslucent;
	}

	public ProgramUniforms initUniforms(int programId) {
		ProgramUniforms.Builder uniforms = ProgramUniforms.builder("<sodium shaders>", programId);

		CommonUniforms.addCommonUniforms(uniforms, programSet.getPack().getIdMap(), programSet.getPackDirectives(), parent.getFrameUpdateNotifier());
		BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);

		return uniforms.buildUniforms();
	}

	public boolean hasShadowPass() {
		return createShadowSamplers != null;
	}

	public ProgramSamplers initTerrainSamplers(int programId) {
		return createTerrainSamplers.apply(programId);
	}

	public ProgramSamplers initShadowSamplers(int programId) {
		return createShadowSamplers.apply(programId);
	}

	public ProgramImages initTerrainImages(int programId) {
		return createTerrainImages.apply(programId);
	}

	public ProgramImages initShadowImages(int programId) {
		return createShadowImages.apply(programId);
	}

	/*public void bindFramebuffer() {
		this.framebuffer.bind();
	}

	public void unbindFramebuffer() {
		GlStateManager.bindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
	}*/

	@SafeVarargs
	private static <T> Optional<T> first(Optional<T>... candidates) {
		for (Optional<T> candidate : candidates) {
			if (candidate.isPresent()) {
				return candidate;
			}
		}

		return Optional.empty();
	}
}
