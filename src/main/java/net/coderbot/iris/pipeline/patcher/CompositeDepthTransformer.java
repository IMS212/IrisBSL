package net.coderbot.iris.pipeline.patcher;

import net.coderbot.iris.gl.uniform.UBOCreator;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;

public class CompositeDepthTransformer {
	public static StringTransformations patch(UBOCreator creator, StringTransformations source) {
		if (source == null) {
			return null;
		}

		source.injectLine(Transformations.InjectionPoint.BEFORE_CODE, creator.getBufferStuff());

		// replace original declaration (fragile!!! we need glsl-transformer to do this robustly)
		// if centerDepthSmooth is not declared as a uniform, we don't make it available
		source.replaceRegex("uniform\\s+float\\s+centerDepthSmooth;", "uniform sampler2D iris_centerDepthSmooth;");
		if (source.contains("uniform sampler2D iris_centerDepthSmooth")) {
			source.define("centerDepthSmooth", "texture(iris_centerDepthSmooth, vec2(0.5)).r");
		}

		return source;
	}
}
