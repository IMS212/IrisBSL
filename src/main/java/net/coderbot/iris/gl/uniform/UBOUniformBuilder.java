package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.Iris;
import net.coderbot.iris.vendored.joml.Vector3d;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vendored.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

public class UBOUniformBuilder implements DynamicLocationalUniformHolder {
	private final List<Uniform> uniforms = new ArrayList<>();
	@Override
	public DynamicLocationalUniformHolder addDynamicUniform(Uniform uniform, ValueUpdateNotifier notifier) {
		uniforms.add(uniform);
		return this;
	}

	@Override
	public LocationalUniformHolder addUniform(UniformUpdateFrequency updateFrequency, Uniform uniform) {
		uniforms.add(uniform);
		return this;
	}

	public UBOCreator build() {
		UBOCreator creator = new UBOCreator();
		int byteSize = 0;
		for (Uniform uniform : uniforms) {
			if (uniform instanceof Vector4Uniform || uniform instanceof Vector3Uniform || uniform instanceof Vector4IntegerJomlUniform || uniform.getType() == UniformType.MAT4) {
				byteSize = align(byteSize, 16);
			} else if (uniform instanceof Vector2Uniform || uniform instanceof Vector2IntegerJomlUniform) {
				byteSize = align(byteSize, 8);
			}
			byteSize += uniform.getByteSize();
		}
		creator.addUniforms(uniforms);

		creator.reset(byteSize);


		return creator;
	}

	/**
	 * Returns {@param position} aligned to the next multiple of {@param alignment}.
	 * @param position The position in bytes
	 * @param alignment The alignment in bytes (must be a power-of-two)
	 * @return The aligned position, either equal to or greater than {@param position}
	 */
	public static int align(int position, int alignment) {
		return ((position - 1) + alignment) & -alignment;
	}

	@Override
	public OptionalInt location(String name, UniformType type) {
		return OptionalInt.of(0);
	}

	@Override
	public UniformHolder externallyManagedUniform(String name, UniformType type) {
		return this;
	}
}
