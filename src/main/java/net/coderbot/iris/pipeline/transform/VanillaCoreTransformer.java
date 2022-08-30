package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.AlphaTests;

// Order fixed
public class VanillaCoreTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			VanillaParameters parameters) {
		// this happens before common to make sure the renaming of attributes is done on
		// attribute inserted by this
		if (parameters.inputs.hasOverlay()) {
			AttributeTransformer.patchOverlayColor(t, tree, root, parameters);
		}

		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
			"const mat4 TEXTURE_MATRIX_2 = mat4(vec4(0.00390625, 0.0, 0.0, 0.0), vec4(0.0, 0.00390625, 0.0, 0.0), vec4(0.0, 0.0, 0.00390625, 0.0), vec4(0.03125, 0.03125, 0.03125, 1.0));");

		root.rename("vaPosition", "iris_Position");
		root.rename("vaColor", "iris_Color");
		root.rename("vaUV0", "iris_UV0");
		root.rename("vaUV1", "iris_UV1");
		root.rename("vaUV2", "iris_UV2");
		root.rename("vaNormal", "iris_Normal");
		root.rename("modelViewMatrix", "iris_ModelViewMat");
		root.rename("modelViewMatrixInverse", "iris_ModelViewMatInverse");
		root.rename("projectionMatrix", "iris_ProjMat");
		root.rename("projectionMatrixInverse", "iris_ProjMatInverse");
		root.rename("textureMatrix", "iris_TextureMat");
		root.rename("chunkOffset", "iris_ChunkOffset");
		root.rename("alphaTestRef", "iris_currentAlphaTest");

		root.replaceReferenceExpressions(t, "normalMatrix",
			"iris_NormalMat");
	}
}
