package net.coderbot.iris.pipeline.newshader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;

public class MutableShaderAttributeInputs {
	private boolean color;
	private boolean tex;
	private boolean overlay;
	private boolean light;
	private boolean normal;
	private boolean newLines;


	public MutableShaderAttributeInputs(boolean color, boolean tex, boolean overlay, boolean light, boolean normal, boolean lines) {
		this.color = color;
		this.tex = tex;
		this.overlay = overlay;
		this.light = light;
		this.normal = normal;
		this.newLines = lines;
	}

	public void setInputs(ShaderAttributeInputs inputs) {
		this.color = inputs.hasColor();
		this.tex = inputs.hasTex();
		this.overlay = inputs.hasOverlay();
		this.light = inputs.hasLight();
		this.normal = inputs.hasNormal();
		this.newLines = inputs.isNewLines();
	}

	public void setInputs(boolean color, boolean tex, boolean overlay, boolean light, boolean normal, boolean lines) {
		this.color = color;
		this.tex = tex;
		this.overlay = overlay;
		this.light = light;
		this.normal = normal;
		this.newLines = lines;
	}

	public boolean hasColor() {
		return color;
	}

	public boolean hasTex() {
		return tex;
	}

	public boolean hasOverlay() {
		return overlay;
	}

	public boolean hasLight() {
		return light;
	}

	public boolean hasNormal() {
		return normal;
	}

	public boolean isNewLines() {
		return newLines;
	}

	public InputAvailability toAvailability() {
		return new InputAvailability(tex, light, overlay);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (color ? 1231 : 1237);
		result = prime * result + (light ? 1231 : 1237);
		result = prime * result + (newLines ? 1231 : 1237);
		result = prime * result + (normal ? 1231 : 1237);
		result = prime * result + (overlay ? 1231 : 1237);
		result = prime * result + (tex ? 1231 : 1237);
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
		MutableShaderAttributeInputs other = (MutableShaderAttributeInputs) obj;
		if (color != other.color)
			return false;
		if (light != other.light)
			return false;
		if (newLines != other.newLines)
			return false;
		if (normal != other.normal)
			return false;
		if (overlay != other.overlay)
			return false;
		if (tex != other.tex)
			return false;
		return true;
	}
}
