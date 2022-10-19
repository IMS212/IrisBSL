package net.coderbot.iris.pipeline.transform;

import com.google.common.collect.ImmutableSet;
import io.github.douira.glsl_transformer.job_parameter.JobParameters;

class Parameters extends JobParameters {
	public final Patch patch;
	public final ImmutableSet<Integer> flipped;
	public PatchShaderType type;

	public Parameters(Patch patch, ImmutableSet<Integer> flipped) {
		this.patch = patch;
		this.flipped = flipped;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((patch == null) ? 0 : patch.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameters other = (Parameters) obj;
		if (patch != other.patch)
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
