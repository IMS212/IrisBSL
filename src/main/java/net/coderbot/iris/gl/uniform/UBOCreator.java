package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.shadows.Matrix4fAccess;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;

public class UBOCreator extends GlResource {
	private ByteBuffer buffer;
	private long currentAddress;
	private int size;
	private List<Uniform> uniformTypes;

	public UBOCreator() {
		super(GL30C.glGenBuffers());
	}

	public void reset(int size) {
		Iris.logger.warn("size: " + size);
		this.size = size;
	}

	private void reset() {
		if (this.buffer != null) {
			MemoryUtil.memFree(this.buffer);
		}
	}

	public void update() {
		GL20C.glBindBuffer(GL32C.GL_UNIFORM_BUFFER, getGlId());
		GL45C.glBufferData(GL32C.GL_UNIFORM_BUFFER, size, GL43C.GL_DYNAMIC_DRAW);
		this.buffer = MemoryUtil.memAlloc(size);
		this.currentAddress = MemoryUtil.memAddress(buffer);
		for (Uniform uniform : uniformTypes) {
			if (uniform instanceof IntUniform) {
				putIntUniform((IntUniform) uniform);
			} else if (uniform instanceof FloatUniform) {
				putFloatUniform((FloatUniform) uniform);
			} else if (uniform instanceof JomlMatrixUniform) {
				putJomlMatrixUniform((JomlMatrixUniform) uniform);
			} else if (uniform instanceof MatrixFromFloatArrayUniform) {
				putMatrixFromFloatArrayUniform((MatrixFromFloatArrayUniform) uniform);
			} else if (uniform instanceof MatrixUniform) {
				putMatrixUniform((MatrixUniform) uniform);
			} else if (uniform instanceof Vector2IntegerJomlUniform) {
				putVector2IntegerJomlUniform((Vector2IntegerJomlUniform) uniform);
			} else if (uniform instanceof Vector2Uniform) {
				putVector2Uniform((Vector2Uniform) uniform);
			} else if (uniform instanceof Vector3Uniform) {
				putVector3Uniform((Vector3Uniform) uniform);
			} else if (uniform instanceof Vector4IntegerJomlUniform) {
				putVector4IntegerJomlUniform((Vector4IntegerJomlUniform) uniform);
			} else if (uniform instanceof Vector4Uniform) {
				putVector4Uniform((Vector4Uniform) uniform);
			} else {
				throw new IllegalArgumentException("Unknown uniform type: " + uniform.getClass().getName());
			}
		}

		sendBufferToGPU();

	}

	public void putIntUniform(IntUniform uniform) {
		PutInt(currentAddress, uniform.getValue());
		currentAddress += 4;
	}

	public void putFloatUniform(FloatUniform uniform) {
		PutFloat(currentAddress, uniform.getValue());
		currentAddress += 4;
	}

	public void putJomlMatrixUniform(JomlMatrixUniform uniform) {
		align(16);
		for (float value : uniform.getValue().get(new float[16])) {
			PutFloat(currentAddress, value);
			currentAddress += 4;
		}
	}

	public void putMatrixFromFloatArrayUniform(MatrixFromFloatArrayUniform uniform) {
		align(16);
		for (float value : uniform.getValue()) {
			PutFloat(currentAddress, value);
			currentAddress += 4;
		}
	}

	public void putMatrixUniform(MatrixUniform uniform) {
		align(16);
		for (float value : ((Matrix4fAccess) (Object) uniform.getValue()).copyIntoArray()) {
			PutFloat(currentAddress, value);
			currentAddress += 4;
		}
	}

	public void putVector2IntegerJomlUniform(Vector2IntegerJomlUniform uniform) {
		align(8);
		PutInt(currentAddress, uniform.getValue().x);
		PutInt(currentAddress + 4, uniform.getValue().y);
		currentAddress += 8;
	}

	public void putVector2Uniform(Vector2Uniform uniform) {
		align(8);
		PutFloat(currentAddress, uniform.getValue().x);
		PutFloat(currentAddress + 4, uniform.getValue().y);
		currentAddress += 8;
	}

	public void putVector3Uniform(Vector3Uniform uniform) {
		align(16);
		PutFloat(currentAddress, uniform.getValue().x);
		PutFloat(currentAddress + 4, uniform.getValue().y);
		PutFloat(currentAddress + 8, uniform.getValue().z);
		currentAddress += 12;
	}

	public void putVector4IntegerJomlUniform(Vector4IntegerJomlUniform uniform) {
		align(16);
		PutInt(currentAddress, uniform.getValue().x);
		PutInt(currentAddress + 4, uniform.getValue().y);
		PutInt(currentAddress + 8, uniform.getValue().z);
		PutInt(currentAddress + 12, uniform.getValue().w);
		currentAddress += 16;
	}

	public void putVector4Uniform(Vector4Uniform uniform) {
		align(16);
		PutFloat(currentAddress, uniform.getValue().x);
		PutFloat(currentAddress + 4, uniform.getValue().y);
		PutFloat(currentAddress + 8, uniform.getValue().z);
		PutFloat(currentAddress + 12, uniform.getValue().w);
		currentAddress += 16;
	}

	private void PutInt(long l, int w) {
		MemoryUtil.memPutInt(l, w);
	}

	private void PutFloat(long l, float y) {
		MemoryUtil.memPutFloat(l, y);
	}

	public String getBufferStuff() {
		StringBuilder sb = new StringBuilder();
		sb.append("layout (std140, binding = 1) uniform CommonUniforms { \n");
		for (Uniform uniform : uniformTypes) {
			if (uniform instanceof BooleanUniform) {
				sb.append("bool").append(" ").append(uniform.getName()).append(";\n");
			} else {
				sb.append(uniform.getType().name().toLowerCase()).append(" ").append(uniform.getName()).append(";\n");
			}
		}
		sb.append("} uniformValues;");
		return sb.toString();
	}

	private void align(int alignment) {
		currentAddress = (((currentAddress - 1) + alignment) & -alignment);
	}

	public void sendBufferToGPU() {
		GL30C.glBufferSubData(GL32C.GL_UNIFORM_BUFFER, 0, buffer);
		GL20C.glBindBuffer(GL32C.GL_UNIFORM_BUFFER, 0);
		GL30C.glBindBufferBase(GL32C.GL_UNIFORM_BUFFER, 1, getGlId());
		reset();
	}

	public void addUniforms(List<Uniform> uniforms) {
		this.uniformTypes = uniforms;
	}

	@Override
	protected void destroyInternal() {
		GL30C.glDeleteBuffers(getGlId());
	}
}
