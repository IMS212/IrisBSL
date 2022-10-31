package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;

public class VanillaParameters extends OverlayParameters {
	public final AlphaTest alpha;
	public final boolean hasChunkOffset;

	public VanillaParameters(Patch patch, AlphaTest alpha, boolean hasChunkOffset,
			boolean hasGeometry) {
		super(patch, hasGeometry);
		this.alpha = alpha;
		this.hasChunkOffset = hasChunkOffset;
	}

	@Override
	public AlphaTest getAlphaTest() {
		return alpha;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((alpha == null) ? 0 : alpha.hashCode());
		result = prime * result + (hasChunkOffset ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VanillaParameters other = (VanillaParameters) obj;
		if (alpha == null) {
			if (other.alpha != null)
				return false;
		} else if (!alpha.equals(other.alpha))
			return false;
		if (hasChunkOffset != other.hasChunkOffset)
			return false;
		return true;
	}
}
