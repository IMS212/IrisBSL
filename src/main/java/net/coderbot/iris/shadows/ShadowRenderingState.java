package net.coderbot.iris.shadows;

import net.coderbot.iris.uniforms.CelestialUniforms;
import org.joml.Matrix4f;
import net.coderbot.iris.pipeline.ShadowRenderer;

public class ShadowRenderingState {
	public static boolean areShadowsCurrentlyBeingRendered() {
		return ShadowRenderer.ACTIVE;
	}

    public static Matrix4f getShadowOrthoMatrix() {
		return ShadowRenderer.PROJECTION;
    }

	public static CelestialUniforms getCelestialUniforms() {
		return ShadowRenderer.celestialUniforms;
	}
}
