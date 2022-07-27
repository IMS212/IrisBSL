package net.coderbot.iris.gl.uniform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

public class MatrixUniform extends Uniform<Matrix4f> {
	private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	private Matrix4f cachedValue;
	private final Supplier<Matrix4f> value;

	MatrixUniform(String name, int location, Supplier<Matrix4f> value) {
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
			cachedValue = newValue.copy();

			cachedValue.store(buffer);
			buffer.rewind();

			RenderSystem.glUniformMatrix4(location, false, buffer);
		}
	}

	@Override
	public int getByteSize() {
		return 64;
	}
}
