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
			byteSize += uniform.getByteSize();
		}
		creator.addUniforms(uniforms);

		creator.reset(byteSize);


		return creator;
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
