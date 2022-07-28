package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vendored.joml.Vector4f;

import java.util.function.Supplier;

public class Vector3Uniform extends Uniform<Vector3f> {
	private final Vector3f cachedValue;
	private final Supplier<Vector3f> value;

	Vector3Uniform(String name, int location, Supplier<Vector3f> value) {
		super(name, UniformType.VEC3, location);

		this.cachedValue = new Vector3f();
		this.value = value;
	}

	static Vector3Uniform converted(String name, int location, Supplier<Vector3d> value) {
		Vector3f held = new Vector3f();

		return new Vector3Uniform(name, location, () -> {
			Vector3d updated = value.get();

			held.set((float) updated.x, (float) updated.y, (float) updated.z);

			return held;
		});
	}

	static Vector3Uniform truncated(String name, int location, Supplier<Vector4f> value) {
		Vector3f held = new Vector3f();

		return new Vector3Uniform(name, location, () -> {
			Vector4f updated = value.get();

			held.set(updated.x(), updated.y(), updated.z());

			return held;
		});
	}

	@Override
	public Vector3f getValue() {
		return value.get();
	}

	@Override
	public void update() {
		Vector3f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue.set(newValue.x(), newValue.y(), newValue.z());
			IrisRenderSystem.uniform3f(location, cachedValue.x(), cachedValue.y(), cachedValue.z());
		}
	}

	@Override
	public int getByteSize() {
		return 12;
	}
}
