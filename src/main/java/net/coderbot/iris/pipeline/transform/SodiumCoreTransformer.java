package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.coderbot.iris.gl.shader.ShaderType;

// Order fixed
public class SodiumCoreTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			SodiumParameters parameters) {
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
			"uniform mat4 iris_LightmapTextureMatrix;");
		root.replaceReferenceExpressions(t, "TEXTURE_MATRIX_2", "iris_LightmapTextureMatrix");

		root.replaceReferenceExpressions(t, "vaPosition", "getVertexPosition()");
		root.replaceReferenceExpressions(t, "vaColor", "_vert_color");
		root.replaceReferenceExpressions(t, "vaUV0", "_vert_tex_diffuse_coord");
		root.replaceReferenceExpressions(t, "vaUV2", "_vert_tex_light_coord");
		root.rename("vaNormal", "iris_Normal");
		root.rename("modelViewMatrix", "iris_ModelViewMatrix");
		root.replaceReferenceExpressions(t, "modelViewMatrixInverse", "inverse(iris_ModelViewMatrix)");
		root.rename("projectionMatrix", "iris_ProjectionMatrix");
		root.replaceReferenceExpressions(t, "modelViewMatrixInverse", "inverse(iris_ProjectionMatrix)");
		root.replaceReferenceExpressions(t, "textureMatrix", "mat4(1.0)");
		root.rename("chunkOffset", "u_RegionOffset");
		root.rename("alphaTestRef", "iris_currentAlphaTest");

		root.rename("normalMatrix",
			"iris_NormalMatrix");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// TODO: Vaporwave-Shaderpack expects that vertex positions will be aligned to
			// chunks.

			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
				// translated from sodium's chunk_vertex.glsl
				"vec3 _vert_position;",
				"vec2 _vert_tex_diffuse_coord;",
				"ivec2 _vert_tex_light_coord;",
				"vec4 _vert_color;",
				"uint _draw_id;",
				"in vec4 a_PosId;",
				"in vec4 a_Color;",
				"in vec2 a_TexCoord;",
				"in ivec2 a_LightCoord;",
				"void _vert_init() {" +
					"_vert_position = (a_PosId.xyz * " + String.valueOf(parameters.positionScale) + " + "
					+ String.valueOf(parameters.positionOffset) + ");" +
					"_vert_tex_diffuse_coord = (a_TexCoord * " + String.valueOf(parameters.textureScale) + ");" +
					"_vert_tex_light_coord = a_LightCoord;" +
					"_vert_color = a_Color;" +
					"_draw_id = uint(a_PosId.w); }",

				// translated from sodium's chunk_parameters.glsl
				// Comment on the struct:
				// Older AMD drivers can't handle vec3 in std140 layouts correctly The alignment
				// requirement is 16 bytes (4 float components) anyways, so we're not wasting
				// extra memory with this, only fixing broken drivers.
				"struct DrawParameters { vec4 offset; };",
				"layout(std140) uniform ubo_DrawParameters {DrawParameters Chunks[256]; };",

				// _draw_translation replaced with Chunks[_draw_id].offset.xyz
				"vec3 getVertexPosition() { return Chunks[_draw_id].offset.xyz + _vert_position; }");

			tree.prependMain(t, "_vert_init();");
		}
	}
}
