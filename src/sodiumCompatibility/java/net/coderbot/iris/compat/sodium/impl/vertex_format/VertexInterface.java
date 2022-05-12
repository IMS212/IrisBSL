package net.coderbot.iris.compat.sodium.impl.vertex_format;

public interface VertexInterface {
	float getPrevX();
	float getPrevY();
	float getPrevZ();
	void updatePrevValues(float x, float y, float z);
}
