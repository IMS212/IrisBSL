package net.irisshaders.iris.shaderpack;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class ProgramOptions implements Serializable {
	public String source;
	public String type = "geometry";
	public String[] usages;
	public int order = -1;
	public String[] targets;
	public String[] targetsToMipmap = new String[0];
	public String depthTexture = "mainDepth";
}
