package net.coderbot.iris.pipeline.newshader;

import net.minecraft.client.renderer.ShaderInstance;

import java.util.function.Function;

/**
 * A specialized map mapping {@link ShaderKey} to {@link ShaderInstance}.
 * Avoids much of the complexity / overhead of an EnumMap while ultimately
 * fulfilling the same function.
 */
public class ShaderMap {
	private final ShaderTemplate[] shaders;

	public ShaderMap(Function<ShaderKey, ShaderTemplate> factory) {
		ShaderKey[] ids = ShaderKey.values();

		this.shaders = new ShaderTemplate[ids.length];

		for (int i = 0; i < ids.length; i++) {
			this.shaders[i] = factory.apply(ids[i]);
		}
	}

	public ShaderTemplate getShader(ShaderKey id) {
		return shaders[id.ordinal()];
	}
}
