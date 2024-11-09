package net.irisshaders.iris.shaderpack;

import java.io.Serializable;

public class ShaderProperties implements Serializable {
	public String name;
	public String id;
	public String version;
	public GlobalPackOptions packOptions;
	public TextureOptions[] textures;
	public ProgramOptions[] programOptions;
}
