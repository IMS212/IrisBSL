package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.vendored.joml.Vector4i;

import java.util.function.Supplier;

public class Vector4IntegerJomlUniform extends Uniform<Vector4i> {
	private Vector4i cachedValue;
	private final Supplier<Vector4i> value;

	Vector4IntegerJomlUniform(String name, int location, Supplier<Vector4i> value) {
		this(name, location, value, null);
	}

	Vector4IntegerJomlUniform(String name, int location, Supplier<Vector4i> value, ValueUpdateNotifier notifier) {
		super(name, UniformType.IVEC4, location, notifier);

		this.cachedValue = null;
		this.value = value;
	}

	@Override
	public Vector4i getValue() {
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
		Vector4i newValue = value.get();

		if (cachedValue == null || !newValue.equals(cachedValue)) {
			cachedValue = newValue;
			IrisRenderSystem.uniform4i(this.location, newValue.x, newValue.y, newValue.z, newValue.w);
		}
	}
}
