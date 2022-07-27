package net.coderbot.iris.gl.uniform;

import org.lwjgl.opengl.GL30C;

public enum UniformType {
	INT(GL30C.GL_INT),
	FLOAT(GL30C.GL_FLOAT),
	MAT4(GL30C.GL_FLOAT_MAT4),
	VEC2(GL30C.GL_FLOAT_VEC2),
	IVEC2(GL30C.GL_INT_VEC2),
	VEC3(GL30C.GL_FLOAT_VEC3),
	VEC4(GL30C.GL_FLOAT_VEC4),
	IVEC4(GL30C.GL_INT_VEC4);

	private final int glType;

	UniformType(int glType) {
		this.glType = glType;
	}

	public int getGlType() {
		return glType;
	}
}
