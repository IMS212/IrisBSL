package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.vendored.joml.Vector2i;

import java.util.function.Supplier;

public class Vector2IntegerJomlUniform extends Uniform<Vector2i> {
	private Vector2i cachedValue;
	private final Supplier<Vector2i> value;

	Vector2IntegerJomlUniform(String name, int location, Supplier<Vector2i> value) {
		this(name, location, value, null);
	}

	Vector2IntegerJomlUniform(String name, int location, Supplier<Vector2i> value, ValueUpdateNotifier notifier) {
		super(name, UniformType.IVEC2, location, notifier);

		this.cachedValue = null;
		this.value = value;
	}

	@Override
	public Vector2i getValue() {
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
		return 8;
	}

	private void updateValue() {
		Vector2i newValue = value.get();

		if (cachedValue == null || !newValue.equals(cachedValue)) {
			cachedValue = newValue;
			IrisRenderSystem.uniform2i(this.location, newValue.x, newValue.y);
		}
	}
}
