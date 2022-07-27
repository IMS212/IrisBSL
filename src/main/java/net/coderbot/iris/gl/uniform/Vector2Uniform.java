package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.vendored.joml.Vector2f;

import java.util.function.Supplier;

public class Vector2Uniform extends Uniform<Vector2f> {
	private Vector2f cachedValue;
	private final Supplier<Vector2f> value;

	Vector2Uniform(String name, int location, Supplier<Vector2f> value) {
		super(name, UniformType.VEC2, location);

		this.cachedValue = null;
		this.value = value;

	}

	@Override
	public Vector2f getValue() {
		return value.get();
	}

	@Override
	public void update() {
		Vector2f newValue = value.get();

		if (cachedValue == null || !newValue.equals(cachedValue)) {
			cachedValue = newValue;
			IrisRenderSystem.uniform2f(this.location, newValue.x, newValue.y);
		}
	}

	@Override
	public int getByteSize() {
		return 8;
	}
}
