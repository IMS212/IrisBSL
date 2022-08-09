package net.coderbot.iris.compat.sodium.mixin.shadow_map.frustum;

import net.caffeinemc.sodium.interop.vanilla.math.frustum.Frustum;
import net.caffeinemc.sodium.interop.vanilla.math.frustum.FrustumAdapter;
import net.coderbot.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AdvancedShadowCullingFrustum.class)
public abstract class MixinAdvancedShadowCullingFrustum implements Frustum, FrustumAdapter {
	@Shadow(remap = false)
	public abstract int fastAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);

	@Override
	public int testBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		switch (fastAabbTest(minX, minY, minZ, maxX, maxY, maxZ)) {
			case 0:
				return -3;
			case 1:
				return -2;
			case 2:
				return -1;
			default:
				throw new IllegalStateException("Unknown visibility, this shouldn't be possible!");
		}
	}

	@Override
	public Frustum sodium$createFrustum() {
		return this;
	}
}
