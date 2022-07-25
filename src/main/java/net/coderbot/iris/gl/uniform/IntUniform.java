package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;

import java.util.function.IntSupplier;

public class IntUniform extends Uniform<Integer> {
	private int cachedValue;
	private final IntSupplier value;

	IntUniform(String name, int location, IntSupplier value) {
		this(name, location, value, null);
	}

	IntUniform(String name, int location, IntSupplier value, ValueUpdateNotifier notifier) {
		super(name, UniformType.INT, location, notifier);

		this.cachedValue = 0;
		this.value = value;
	}

	@Override
	public Integer getValue() {
		return value.getAsInt();
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
		int newValue = value.getAsInt();

		if (cachedValue != newValue) {
			cachedValue = newValue;
			IrisRenderSystem.uniform1i(location, newValue);
		}
	}
}
