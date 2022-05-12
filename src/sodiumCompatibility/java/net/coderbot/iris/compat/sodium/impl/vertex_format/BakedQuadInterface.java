package net.coderbot.iris.compat.sodium.impl.vertex_format;

public interface BakedQuadInterface {
	float getPrevX(int i);
	float getPrevY(int i);
	float getPrevZ(int i);
	void updatePrevValues(int i, float x, float y, float z);
}
