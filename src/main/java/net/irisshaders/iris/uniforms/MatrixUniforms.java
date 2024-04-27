package net.irisshaders.iris.uniforms;

import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shadows.ShadowCascade;
import net.irisshaders.iris.shadows.ShadowMatrices;
import net.irisshaders.iris.shadows.ShadowRenderer;
import org.joml.Matrix4f;

import java.util.function.Supplier;

import static net.irisshaders.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

public final class MatrixUniforms {
	private MatrixUniforms() {
	}

	private static ShadowCascade[] matrices;

	public static void addMatrixUniforms(UniformHolder uniforms, PackDirectives directives, FrameUpdateNotifier updateNotifier) {
		addMatrix(uniforms, "ModelView", CapturedRenderingState.INSTANCE::getGbufferModelView);
		addMatrix(uniforms, "Projection", CapturedRenderingState.INSTANCE::getGbufferProjection);
		addDHMatrix(uniforms, "Projection", DHCompat::getProjection);
		addShadowMatrix(uniforms, "ModelView", () ->
			new Matrix4f(ShadowRenderer.createShadowModelView(directives.getSunPathRotation(), directives.getShadowDirectives().getIntervalSize()).last().pose()));
		addShadowMatrices(uniforms, "Projection", new CelestialUniforms(directives.getSunPathRotation()), () -> matrices, updateNotifier);
	}

	private static void addShadowMatrices(UniformHolder uniforms, String name, CelestialUniforms celestial, Supplier<ShadowCascade[]> supplier, FrameUpdateNotifier updateNotifier) {
		updateNotifier.addListener(() -> {
			matrices = ShadowMatrices.updateCascadeShadows(celestial.getShadowLightPositionInWorldSpace());
		});

		for (int i = 0; i < IrisRenderingPipeline.CASCADE_COUNT; i++) {
			int finalI = i;
			uniforms
				.uniformMatrix(PER_FRAME, "shadow" + name + "[" + i + "]", () -> matrices[finalI].projection())
				.uniform1f(PER_FRAME, "shadow" + name + "SplitDistance" + "[" + i + "]", () -> matrices[finalI].splitDistance())
				.uniformMatrix(PER_FRAME, "shadow" + name + "Inverse" + "[" + i + "]", new Inverted(() -> matrices[finalI].projection()));
		}
	}

	private static void addMatrix(UniformHolder uniforms, String name, Supplier<Matrix4f> supplier) {
		uniforms
			.uniformMatrix(PER_FRAME, "gbuffer" + name, supplier)
			.uniformMatrix(PER_FRAME, "gbuffer" + name + "Inverse", new Inverted(supplier))
			.uniformMatrix(PER_FRAME, "gbufferPrevious" + name, new Previous(supplier));
	}

	private static void addDHMatrix(UniformHolder uniforms, String name, Supplier<Matrix4f> supplier) {
		uniforms
			.uniformMatrix(PER_FRAME, "dh" + name, supplier)
			.uniformMatrix(PER_FRAME, "dh" + name + "Inverse", new Inverted(supplier))
			.uniformMatrix(PER_FRAME, "dhPrevious" + name, new Previous(supplier));
	}

	private static void addShadowMatrix(UniformHolder uniforms, String name, Supplier<Matrix4f> supplier) {
		uniforms
			.uniformMatrix(PER_FRAME, "shadow" + name, supplier)
			.uniformMatrix(PER_FRAME, "shadow" + name + "Inverse", new Inverted(supplier));
	}

	private static class Inverted implements Supplier<Matrix4f> {
		private final Supplier<Matrix4f> parent;

		Inverted(Supplier<Matrix4f> parent) {
			this.parent = parent;
		}

		@Override
		public Matrix4f get() {
			// PERF: Don't copy + allocate this matrix every time?
			Matrix4f copy = new Matrix4f(parent.get());

			copy.invert();

			return copy;
		}
	}

	private static class Previous implements Supplier<Matrix4f> {
		private final Supplier<Matrix4f> parent;
		private Matrix4f previous;

		Previous(Supplier<Matrix4f> parent) {
			this.parent = parent;
			this.previous = new Matrix4f();
		}

		@Override
		public Matrix4f get() {
			// PERF: Don't copy + allocate these matrices every time?
			Matrix4f copy = new Matrix4f(parent.get());
			Matrix4f previous = new Matrix4f(this.previous);

			this.previous = copy;

			return previous;
		}
	}
}
