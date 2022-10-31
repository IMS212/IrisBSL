package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.state.ValueUpdateNotifier;

import java.util.function.BooleanSupplier;

public class BooleanUniform extends IntUniform {
	BooleanUniform(int location, BooleanSupplier value) {
		super(location, () -> value.getAsBoolean() ? 1 : 0);
	}

	BooleanUniform(int location, BooleanSupplier value, ValueUpdateNotifier notifier) {
		super(location, () -> value.getAsBoolean() ? 1 : 0, notifier);
	}
}
