package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.AlphaTests;

// Order fixed
public class VanillaTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			VanillaParameters parameters) {
		// this happens before common to make sure the renaming of attributes is done on
		// attribute inserted by this
		AttributeTransformer.patchOverlayColor(t, tree, root, parameters);

		CommonTransformer.transform(t, tree, root, parameters);

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// Alias of gl_MultiTexCoord1 on 1.15+ for OptiFine
			// See https://github.com/IrisShaders/Iris/issues/1149
			root.rename("gl_MultiTexCoord2", "gl_MultiTexCoord1");

				root.replaceReferenceExpressions(t, "gl_MultiTexCoord0",
						"(iris_hasTex ? vec4(iris_UV0, 0.0, 1.0) : vec4(0.5, 0.5, 0.0, 1.0))");
				tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
						"in vec2 iris_UV0;", "uniform bool iris_hasTex;");

			root.replaceReferenceExpressions(t, "gl_MultiTexCoord2",
				"(iris_hasLight ? vec4(iris_UV2, 0.0, 1.0) : vec4(240.0, 240.0, 0.0, 1.0))");
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"in vec2 iris_UV2;", "uniform bool iris_hasLight;");

			AttributeTransformer.patchMultiTexCoord3(t, tree, root, parameters);

			// gl_MultiTexCoord0 and gl_MultiTexCoord1 are the only valid inputs (with
			// gl_MultiTexCoord2 and gl_MultiTexCoord3 as aliases), other texture
			// coordinates are not valid inputs.
			CommonTransformer.replaceGlMultiTexCoordBounded(t, root, 4, 7);
		}

		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"uniform vec4 iris_ColorModulator;");

		if (parameters.inputs.hasColor()) {
			// TODO: Handle the fragment / geometry shader here
			if (parameters.alpha == AlphaTests.VERTEX_ALPHA) {
				root.replaceReferenceExpressions(t, "gl_Color",
						"vec4((iris_Color * iris_ColorModulator).rgb, iris_ColorModulator.a)");
			} else {
				root.replaceReferenceExpressions(t, "gl_Color",
						"(iris_Color * iris_ColorModulator)");
			}

			if (parameters.type.glShaderType == ShaderType.VERTEX) {
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
						"in vec4 iris_Color;");
			}
		} else {
			// iris_ColorModulator should be applied regardless of the alpha test state.
			root.rename("gl_Color", "iris_ColorModulator");
		}

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			root.replaceReferenceExpressions(t, "gl_Normal",
				"((iris_hasNormal && !iris_applyLines) ? iris_Normal : vec3(0.0, 0.0, 1.0))");

			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"in vec3 iris_Normal;", "uniform bool iris_hasNormal;");
		}

		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"uniform mat4 iris_LightmapTextureMatrix;",
				"uniform mat4 iris_TextureMat;",
				"uniform mat4 iris_ModelViewMat;");

		// TODO: More solid way to handle texture matrices
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix0, "iris_TextureMat");
		root.replaceExpressionMatches(t, CommonTransformer.glTextureMatrix1, "iris_LightmapTextureMatrix");

		// TODO: Should probably add the normal matrix as a proper uniform that's
		// computed on the CPU-side of things
		root.replaceReferenceExpressions(t, "gl_NormalMatrix",
				"mat3(transpose(inverse(gl_ModelViewMatrix)))");

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
					"in vec3 iris_Position;");
			if (root.identifierIndex.has("ftransform")) {
				tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
						"vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");
			}

			root.replaceReferenceExpressions(t, "gl_Vertex",
				"(iris_applyLines ? vec4(iris_Position + iris_vertex_offset, 1.0) : vec4(iris_Position, 1.0))");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS, "uniform bool iris_applyLines");
				// Create our own main function to wrap the existing main function, so that we
				// can do our line shenanigans.
				// TRANSFORM: this is fine since the AttributeTransformer has a different name
				// in the vertex shader
				root.rename("main", "irisMain");
				tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
						"vec3 iris_vertex_offset = vec3(0.0);");
				tree.parseAndInjectNodes(t, ASTInjectionPoint.END,
						"uniform vec2 iris_ScreenSize;",
						"uniform float iris_LineWidth;",
						"void iris_widen_lines(vec4 linePosStart, vec4 linePosEnd) {" +
								"vec3 ndc1 = linePosStart.xyz / linePosStart.w;" +
								"vec3 ndc2 = linePosEnd.xyz / linePosEnd.w;" +
								"vec2 lineScreenDirection = normalize((ndc2.xy - ndc1.xy) * iris_ScreenSize);" +
								"vec2 lineOffset = vec2(-lineScreenDirection.y, lineScreenDirection.x) * iris_LineWidth / iris_ScreenSize;"
								+
								"if (lineOffset.x < 0.0) {" +
								"    lineOffset *= -1.0;" +
								"}" +
								"if (gl_VertexID % 2 == 0) {" +
								"    gl_Position = vec4((ndc1 + vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);" +
								"} else {" +
								"    gl_Position = vec4((ndc1 - vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);" +
								"}}",
						"void main() {" +
							"if (iris_applyLines) {" +
								"iris_vertex_offset = iris_Normal;" +
								"irisMain();" +
								"vec4 linePosEnd = gl_Position;" +
								"gl_Position = vec4(0.0);" +
								"iris_vertex_offset = vec3(0.0);" +
								"irisMain();" +
								"vec4 linePosStart = gl_Position;" +
								"iris_widen_lines(linePosStart, linePosEnd);" +
					"} else {" +
					"irisMain();" +
					"} }");
		}

		// TODO: All of the transformed variants of the input matrices, preferably
		// computed on the CPU side...
		root.replaceReferenceExpressions(t, "gl_ModelViewProjectionMatrix",
				"(gl_ProjectionMatrix * gl_ModelViewMatrix)");

		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
			"uniform vec3 iris_ChunkOffset;",
			"mat4 _iris_internal_translate(vec3 offset) {" +
				"return mat4(1.0, 0.0, 0.0, 0.0," +
				"0.0, 1.0, 0.0, 0.0," +
				"0.0, 0.0, 1.0, 0.0," +
				"offset.x, offset.y, offset.z, 1.0); }");

		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
			"const float iris_VIEW_SHRINK = 1.0 - (1.0 / 256.0);",
			"const mat4 iris_VIEW_SCALE = mat4(" +
				"iris_VIEW_SHRINK, 0.0, 0.0, 0.0," +
				"0.0, iris_VIEW_SHRINK, 0.0, 0.0," +
				"0.0, 0.0, iris_VIEW_SHRINK, 0.0," +
				"0.0, 0.0, 0.0, 1.0);");
		root.replaceReferenceExpressions(t, "gl_ModelViewMatrix",
			"(iris_applyLines ? (iris_VIEW_SCALE * iris_ModelViewMat) : (iris_hasChunkOffset ? (iris_ModelViewMat * _iris_internal_translate(iris_ChunkOffset)) : iris_ModelViewMat)");

		root.rename("gl_ProjectionMatrix", "iris_ProjMat");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"uniform mat4 iris_ProjMat;");
	}
}
