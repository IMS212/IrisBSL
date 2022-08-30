package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;

// Order fixed
public class CompositeCoreTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root) {
		root.rename("vaPosition", "Position");
		root.rename("vaColor", "Color");
		root.rename("vaUV0", "UV0");
		root.rename("vaUV1", "UV1");
		root.rename("vaUV2", "UV2");
		root.rename("vaNormal", "Normal");
		root.rename("modelViewMatrix", "ModelViewMat");
		root.rename("modelViewMatrixInverse", "ModelViewMatInverse");
		root.replaceReferenceExpressions(t, "projectionMatrix",
			"mat4(vec4(2.0, 0.0, 0.0, 0.0), vec4(0.0, 2.0, 0.0, 0.0), vec4(0.0), vec4(-1.0, -1.0, 0.0, 1.0))");
		root.replaceReferenceExpressions(t, "projectionMatrixInverse",
			"inverse(mat4(vec4(2.0, 0.0, 0.0, 0.0), vec4(0.0, 2.0, 0.0, 0.0), vec4(0.0), vec4(-1.0, -1.0, 0.0, 1.0)))");
		root.rename("textureMatrix", "TextureMat");

		root.replaceReferenceExpressions(t, "normalMatrix",
			"NormalMat");
	}
}
