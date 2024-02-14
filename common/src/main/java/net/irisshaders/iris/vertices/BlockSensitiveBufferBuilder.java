package net.irisshaders.iris.vertices;

import com.mojang.blaze3d.vertex.VertexFormat;

public interface BlockSensitiveBufferBuilder {
	void beginBlock(short block, short renderType, int localPosX, int localPosY, int localPosZ);

	void endBlock();
	short getCurrentBlock();

	VertexFormat.Mode getMode();

	int getStride();
}
