package net.irisshaders.iris.shaderpack;

import java.io.Serializable;

public class GlobalPackOptions implements Serializable {
	public boolean renderSun;
	public boolean renderMoon;
	public float sunPathRotation;
	public int shadowMapResolution;
	public float shadowMapDistance;
	public float shadowNearPlane;
	public float shadowFarPlane;
}
