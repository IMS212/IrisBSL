package net.coderbot.iris.gl.uniform;

import net.coderbot.iris.gl.GlResource;
import org.lwjgl.opengl.ARBShaderImageLoadStore;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Locale;

public class UBOCreator extends GlResource {
	private ByteBuffer buffer;
	private int size;
	private List<Uniform> uniformTypes;

	public UBOCreator() {
		super(GL30C.glGenBuffers());
	}

	public void reset(int size) {
		this.size = size;
		reset();
	}

	private void reset() {
		if (this.buffer != null) {
			MemoryUtil.memFree(this.buffer);
		}
		this.buffer = MemoryUtil.memAlloc(size);
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
		buffer.putInt(uniform.getValue());
	}

	public void putFloatUniform(FloatUniform uniform) {
		buffer.putFloat(uniform.getValue());
	}

	public void putJomlMatrixUniform(JomlMatrixUniform uniform) {
		uniform.getValue().get(buffer);
		buffer.position(buffer.position() + 16);
	}

	public void putMatrixFromFloatArrayUniform(MatrixFromFloatArrayUniform uniform) {
		for (float value : uniform.getValue()) {
			buffer.putFloat(value);
		}
	}

	public void putMatrixUniform(MatrixUniform uniform) {
		uniform.getValue().store(buffer.asFloatBuffer());
	}

	public void putVector2IntegerJomlUniform(Vector2IntegerJomlUniform uniform) {
		uniform.getValue().get(buffer);
		buffer.position(buffer.position() + 8);
	}

	public void putVector2Uniform(Vector2Uniform uniform) {
		uniform.getValue().get(buffer);
		buffer.position(buffer.position() + 8);
	}

	public void putVector3Uniform(Vector3Uniform uniform) {
		uniform.getValue().get(buffer);
		// Aligned to std140
		buffer.position(buffer.position() + 16);
	}

	public void putVector4IntegerJomlUniform(Vector4IntegerJomlUniform uniform) {
		uniform.getValue().get(buffer);
		buffer.position(buffer.position() + 16);
	}

	public void putVector4Uniform(Vector4Uniform uniform) {
		uniform.getValue().get(buffer);
		buffer.position(buffer.position() + 16);
	}

	public String getBufferStuff() {
		StringBuilder sb = new StringBuilder();
		sb.append("layout (std140, binding = 0) uniform CommonUniforms { \n");
		for (Uniform uniform : uniformTypes) {
			sb.append(uniform.getType().name().toLowerCase()).append(" ").append(uniform.getName()).append(";\n");
		}
		sb.append("} uniformValues;");
		return sb.toString();
	}

	public void sendBufferToGPU() {
		buffer.rewind();
		GL20C.glBindBuffer(GL32C.GL_UNIFORM_BUFFER, getGlId());
		GL20C.glBufferData(GL32C.GL_UNIFORM_BUFFER, buffer, GL30C.GL_STATIC_DRAW);
		GL20C.glBindBuffer(GL32C.GL_UNIFORM_BUFFER, 0);
		GL30C.glBindBufferBase(GL32C.GL_UNIFORM_BUFFER, 0, getGlId());
	}

	public void addUniforms(List<Uniform> uniforms) {
		this.uniformTypes = uniforms;
	}

	@Override
	protected void destroyInternal() {
		GL30C.glDeleteBuffers(getGlId());
	}
}
