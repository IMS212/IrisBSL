package net.coderbot.iris.pipeline.patcher;

import net.coderbot.iris.gl.uniform.UBOCreator;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;

public class CompositeDepthTransformer {
	public static String patch(UBOCreator creator, String source) {
		if (source == null) {
			return null;
		}

		if (source.contains("iris_")) {
			throw new IllegalStateException("Shader is attempting to exploit internal Iris code!");
		}

		StringTransformations transformations = new StringTransformations(source);
		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, creator.getBufferStuff());

		// replace original declaration (fragile!!! we need glsl-transformer to do this robustly)
		// if centerDepthSmooth is not declared as a uniform, we don't make it available
		transformations.replaceRegex("uniform\\s+float\\s+centerDepthSmooth;", "uniform sampler2D iris_centerDepthSmooth;");
		if (transformations.contains("uniform sampler2D iris_centerDepthSmooth")) {
			transformations.define("centerDepthSmooth", "texture2D(iris_centerDepthSmooth, vec2(0.5)).r");
		}

		return transformations.toString();
	}
}
