package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;

public class FloatUniform extends Uniform<Float> {
	private float cachedValue;
	private final FloatSupplier value;

	FloatUniform(String name, int location, FloatSupplier value) {
		this(name, location, value, null);
	}

	FloatUniform(String name, int location, FloatSupplier value, ValueUpdateNotifier notifier) {
		super(name, UniformType.FLOAT, location, notifier);

		this.cachedValue = 0;
		this.value = value;
	}

	@Override
	public Float getValue() {
		return value.getAsFloat();
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
		return 4;
	}

	private void updateValue() {
		float newValue = value.getAsFloat();

		if (cachedValue != newValue) {
			cachedValue = newValue;
			IrisRenderSystem.uniform1f(location, newValue);
		}
	}
}
