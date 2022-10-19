package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.basic.ASTNode;
import io.github.douira.glsl_transformer.ast.node.external_declaration.ExternalDeclaration;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.shaderpack.PackRenderTargetDirectives;

import java.util.stream.Stream;

/**
 * Implements AttributeShaderTransformer using glsl-transformer AST
 * transformation methods.
 */
class TextureFlipTransformer {
	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		for (int i : parameters.flipped) {
			root.rename("colortex" + i, "colortex" + i + "Alt");
			if (i < PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.size()) {
				String legacyName = PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.get(i);
				root.rename(legacyName, "colortex" + i + "Alt");
			}

			//tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
			//	"uniform sampler2D colortex" + i + "Alt;");
			//tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
			//	"uniform bool isBeforeTranslucent;");

			//root.replaceReferenceExpressions(t, "colortex" + i,
			//	"isBeforeTranslucent ? colorTex" + i + " : colortex" + i + "Alt");
			//root.replaceReferenceExpressions(t, "colortex" + i,
			//"colortex" + i + "Alt");
		}
	}
}
