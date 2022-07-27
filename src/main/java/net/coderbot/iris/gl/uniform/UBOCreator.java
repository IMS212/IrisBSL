package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.shadows.Matrix4fAccess;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;
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
		GL20C.glBindBuffer(GL32C.GL_UNIFORM_BUFFER, getGlId());
		GL45C.glBufferStorage(GL32C.GL_UNIFORM_BUFFER, size, 0);
		GL20C.glBindBuffer(GL32C.GL_UNIFORM_BUFFER, 0);
		GL30C.glBindBufferBase(GL32C.GL_UNIFORM_BUFFER, 1, getGlId());
		reset();
	}

	private void reset() {
		if (this.buffer != null) {
			MemoryUtil.memFree(this.buffer);
		}
		this.buffer = MemoryUtil.memAlloc(size);
		this.currentAddress = MemoryUtil.memAddress(this.buffer);
	}

	public void update() {
		reset();
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
		MemoryUtil.memPutInt(currentAddress, uniform.getValue());
		currentAddress += 4;
	}

	public void putFloatUniform(FloatUniform uniform) {
		MemoryUtil.memPutFloat(currentAddress, uniform.getValue());
		currentAddress += 4;
	}

	public void putJomlMatrixUniform(JomlMatrixUniform uniform) {
		for (float value : uniform.getValue().get(new float[16])) {
			MemoryUtil.memPutFloat(currentAddress, value);
			currentAddress += 4;
		}
	}

	public void putMatrixFromFloatArrayUniform(MatrixFromFloatArrayUniform uniform) {
		for (float value : uniform.getValue()) {
			MemoryUtil.memPutFloat(currentAddress, value);
			currentAddress += 4;
		}
	}

	public void putMatrixUniform(MatrixUniform uniform) {
		for (float value : ((Matrix4fAccess) (Object) uniform.getValue()).copyIntoArray()) {
			MemoryUtil.memPutFloat(currentAddress, value);
			currentAddress += 4;
		}
	}

	public void putVector2IntegerJomlUniform(Vector2IntegerJomlUniform uniform) {
		MemoryUtil.memPutInt(currentAddress, uniform.getValue().x);
		MemoryUtil.memPutInt(currentAddress + 4, uniform.getValue().y);
		currentAddress += 8;
	}

	public void putVector2Uniform(Vector2Uniform uniform) {
		MemoryUtil.memPutFloat(currentAddress, uniform.getValue().x);
		MemoryUtil.memPutFloat(currentAddress + 4, uniform.getValue().y);
		currentAddress += 8;
	}

	public void putVector3Uniform(Vector3Uniform uniform) {
		MemoryUtil.memPutFloat(currentAddress, uniform.getValue().x);
		MemoryUtil.memPutFloat(currentAddress + 4, uniform.getValue().y);
		MemoryUtil.memPutFloat(currentAddress + 8, uniform.getValue().z);
		currentAddress += 16;
	}

	public void putVector4IntegerJomlUniform(Vector4IntegerJomlUniform uniform) {
		MemoryUtil.memPutInt(currentAddress, uniform.getValue().x);
		MemoryUtil.memPutInt(currentAddress + 4, uniform.getValue().y);
		MemoryUtil.memPutInt(currentAddress + 8, uniform.getValue().z);
		MemoryUtil.memPutInt(currentAddress + 12, uniform.getValue().w);
		currentAddress += 16;
	}

	public void putVector4Uniform(Vector4Uniform uniform) {
		MemoryUtil.memPutFloat(currentAddress, uniform.getValue().x);
		MemoryUtil.memPutFloat(currentAddress + 4, uniform.getValue().y);
		MemoryUtil.memPutFloat(currentAddress + 8, uniform.getValue().z);
		MemoryUtil.memPutFloat(currentAddress + 12, uniform.getValue().w);
		currentAddress += 16;
	}

	public String getBufferStuff() {
		StringBuilder sb = new StringBuilder();
		sb.append("layout (std140, binding = 1) uniform CommonUniforms { \n");
		for (Uniform uniform : uniformTypes) {
			sb.append(uniform.getType().name().toLowerCase()).append(" ").append(uniform.getName()).append(";\n");
		}
		sb.append("} uniformValues;");
		return sb.toString();
	}

	public void sendBufferToGPU() {
		buffer.position((int) (currentAddress - MemoryUtil.memAddress(buffer)));
		GL32C.glBindBuffer(GL32C.GL_UNIFORM_BUFFER, getGlId());
		GL20C.glBufferSubData(GL32C.GL_UNIFORM_BUFFER, 0, buffer);
		GL32C.glBindBuffer(GL32C.GL_UNIFORM_BUFFER, 0);
	}

	public void addUniforms(List<Uniform> uniforms) {
		this.uniformTypes = uniforms;
	}

	@Override
	protected void destroyInternal() {
		GL30C.glDeleteBuffers(getGlId());
	}
}
