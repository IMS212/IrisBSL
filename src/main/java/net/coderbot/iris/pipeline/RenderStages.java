package net.coderbot.iris.pipeline;

public enum RenderStages {
	MC_RENDER_STAGE_NONE,
	MC_RENDER_STAGE_SKY,
	MC_RENDER_STAGE_SUNSET,
	MC_RENDER_STAGE_CUSTOM_SKY, // Unused
	MC_RENDER_STAGE_SUN,
	MC_RENDER_STAGE_MOON,
	MC_RENDER_STAGE_STARS,
	MC_RENDER_STAGE_VOID,
	MC_RENDER_STAGE_TERRAIN_SOLID,
	MC_RENDER_STAGE_TERRAIN_CUTOUT_MIPPED,
	MC_RENDER_STAGE_TERRAIN_CUTOUT,
	MC_RENDER_STAGE_ENTITIES,
	MC_RENDER_STAGE_BLOCK_ENTITIES,
	MC_RENDER_STAGE_DESTROY,
	MC_RENDER_STAGE_OUTLINE,
	MC_RENDER_STAGE_DEBUG,
	MC_RENDER_STAGE_HAND_SOLID,
	MC_RENDER_STAGE_TERRAIN_TRANSLUCENT,
	MC_RENDER_STAGE_TRIPWIRE,
	MC_RENDER_STAGE_PARTICLES,
	MC_RENDER_STAGE_CLOUDS,
	MC_RENDER_STAGE_RAIN_SNOW,
	MC_RENDER_STAGE_WORLD_BORDER,
	MC_RENDER_STAGE_HAND_TRANSLUCENT
}
