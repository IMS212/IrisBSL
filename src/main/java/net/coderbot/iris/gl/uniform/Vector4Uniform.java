package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vendored.joml.Vector4f;

import java.util.function.Supplier;

public class Vector4Uniform extends Uniform<Vector4f> {
	private final Vector4f cachedValue;
	private final Supplier<Vector4f> value;
	private Supplier<Vector3f> threeF;
	private Supplier<Vector3d> threeD;

	Vector4Uniform(String name, int location, Supplier<Vector4f> value) {
		this(name, location, value, null);
	}

	Vector4Uniform(String name, Supplier<Vector3f> value) {
		this(name, 0, () -> null, null);
		threeF = value;
	}

	Vector4Uniform(String name, String a, Supplier<Vector3d> value) {
		this(name, 0, () -> null, null);
		threeD = value;
	}

	Vector4Uniform(String name, int location, Supplier<Vector4f> value, ValueUpdateNotifier notifier) {
		super(name, UniformType.VEC4, location, notifier);

		this.cachedValue = new Vector4f();
		this.value = value;
	}

	@Override
	public Vector4f getValue() {
		if (threeF != null) {
			return new Vector4f(threeF.get(), 1);
		}
		if (threeD != null) {
			return new Vector4f(threeD.get().get(new Vector3f()), 1);
		}
		return value.get();
	}

	@Override
	public void update() {
		updateValue();

		if (notifier != null) {
			notifier.setListener(this::updateValue);
		}
	}

	@Override
	public int getByteSize() {
		return 16;
	}

	private void updateValue() {
		Vector4f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue.set(newValue.x(), newValue.y(), newValue.z(), newValue.w());
			IrisRenderSystem.uniform4f(location, cachedValue.x(), cachedValue.y(), cachedValue.z(), cachedValue.w());
		}
	}
}
