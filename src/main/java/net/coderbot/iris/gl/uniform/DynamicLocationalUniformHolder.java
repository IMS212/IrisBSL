package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.vendored.joml.Vector2i;
import net.coderbot.iris.vendored.joml.Vector4f;
import net.coderbot.iris.vendored.joml.Vector4i;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface DynamicLocationalUniformHolder extends LocationalUniformHolder, DynamicUniformHolder {
	DynamicLocationalUniformHolder addDynamicUniform(Uniform uniform, ValueUpdateNotifier notifier);

	default DynamicLocationalUniformHolder uniform1f(String name, FloatSupplier value, ValueUpdateNotifier notifier) {
		location(name, UniformType.FLOAT).ifPresent(id -> addDynamicUniform(new FloatUniform(name, id, value, notifier), notifier));

		return this;
	}

	default DynamicLocationalUniformHolder uniform1f(String name, IntSupplier value, ValueUpdateNotifier notifier) {
		location(name, UniformType.FLOAT).ifPresent(id -> addDynamicUniform(new FloatUniform(name, id, () -> (float) value.getAsInt(), notifier), notifier));

		return this;
	}

	default DynamicLocationalUniformHolder uniform1f(String name, DoubleSupplier value, ValueUpdateNotifier notifier) {
		location(name, UniformType.FLOAT).ifPresent(id -> addDynamicUniform(new FloatUniform(name, id, () -> (float) value.getAsDouble(), notifier), notifier));

		return this;
	}

	default DynamicLocationalUniformHolder uniform1i(String name, IntSupplier value, ValueUpdateNotifier notifier) {
		location(name, UniformType.INT).ifPresent(id -> addDynamicUniform(new IntUniform(name, id, value, notifier), notifier));

		return this;
	}

	default DynamicLocationalUniformHolder uniform2i(String name, Supplier<Vector2i> value, ValueUpdateNotifier notifier) {
		location(name, UniformType.IVEC2).ifPresent(id -> addDynamicUniform(new Vector2IntegerJomlUniform(name, id, value, notifier), notifier));

		return this;
	}

	default DynamicUniformHolder uniform4f(String name, Supplier<Vector4f> value, ValueUpdateNotifier notifier) {
		location(name, UniformType.VEC4).ifPresent(id -> addDynamicUniform(new Vector4Uniform(name, id, value, notifier), notifier));

		return this;
	}

	default DynamicUniformHolder uniform4i(String name, Supplier<Vector4i> value, ValueUpdateNotifier notifier) {
		location(name, UniformType.IVEC4).ifPresent(id -> addDynamicUniform(new Vector4IntegerJomlUniform(name, id, value, notifier), notifier));

		return this;
	}
}
