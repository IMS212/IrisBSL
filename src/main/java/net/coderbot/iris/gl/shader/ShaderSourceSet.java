package net.coderbot.iris.gl.shader;

import org.jetbrains.annotations.Nullable;

public class ShaderSourceSet {
	public String vertex;
	public String fragment;
	@Nullable
	public String geometry;

	public ShaderSourceSet(String vertex, String fragment, @Nullable String geometry) {
		this.vertex = vertex;
		this.fragment = fragment;
		this.geometry = geometry;
	}

	public String getVertex() {
		return vertex;
	}

	public String getFragment() {
		return fragment;
	}

	@Nullable
	public String getGeometry() {
		return geometry;
	}
}
