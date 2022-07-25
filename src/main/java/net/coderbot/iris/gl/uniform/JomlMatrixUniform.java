package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.vendored.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

public class JomlMatrixUniform extends Uniform<Matrix4f> {
	private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	private Matrix4f cachedValue;
	private final Supplier<Matrix4f> value;

	JomlMatrixUniform(String name, int location, Supplier<Matrix4f> value) {
		super(name, UniformType.MAT4, location);

		this.cachedValue = null;
		this.value = value;
	}

	@Override
	public Matrix4f getValue() {
		return value.get();
	}

	@Override
	public void update() {
		Matrix4f newValue = value.get();

		if (!newValue.equals(cachedValue)) {
			cachedValue = new Matrix4f(newValue);

			cachedValue.get(buffer);
			buffer.rewind();

			IrisRenderSystem.uniformMatrix4fv(location, false, buffer);
		}
	}

	@Override
	public int getByteSize() {
		return 64;
	}
}
