package net.irisshaders.iris.compat.sodium.impl.vertex_format;

import net.irisshaders.iris.vertices.QuadView;

public class SodiumTriView implements QuadView {
	private long address;
	private int stride;
	private int textureOffset;

	public void setInfo(long address, int stride, int textureOffset) {
		this.address = address;
		this.stride = stride;
		this.textureOffset = textureOffset;
	}

	@Override
	public float x(int index) {
		return address - stride * (3L - index);
	}

	@Override
	public float y(int index) {
		return address + 4 - stride * (3L - index);
	}

	@Override
	public float z(int index) {
		return address + 8 - stride * (3L - index);
	}

	@Override
	public float u(int index) {
		return address + textureOffset - stride * (3L - index);
	}

	@Override
	public float v(int index) {
		return address + (textureOffset + 4
		) - stride * (3L - index);
	}
}
