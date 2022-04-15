package net.coderbot.iris.compat.sodium.impl.shader_overrides;

public enum IrisTerrainPass {
    SHADOW_SOLID("shadow_terrain", true),
    SHADOW_TRANSLUCENT("shadow_translucent", true),
    GBUFFER_SOLID("gbuffers_terrain", false),
    GBUFFER_TRANSLUCENT("gbuffers_water", false);

    private final String name;
    private final boolean shadow;

    IrisTerrainPass(String name, boolean shadow) {
        this.name = name;
		this.shadow = shadow;
    }

    public String getName() {
        return name;
    }

	public boolean isShadow() {
		return shadow;
	}
}
