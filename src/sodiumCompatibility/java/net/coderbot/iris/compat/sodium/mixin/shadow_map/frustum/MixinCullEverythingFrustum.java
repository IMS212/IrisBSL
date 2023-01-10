package net.coderbot.iris.compat.sodium.mixin.shadow_map.frustum;

import me.jellysquid.mods.sodium.client.util.frustum.Frustum;
import me.jellysquid.mods.sodium.client.util.frustum.FrustumAdapter;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.shadows.frustum.CullEverythingFrustum;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CullEverythingFrustum.class)
public class MixinCullEverythingFrustum implements Frustum, FrustumAdapter {
	@Override
	public Visibility testBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return Visibility.OUTSIDE;
	}

	@Override
	public Matrix4f getMatrix() {
		return new Matrix4f();
	}

	@Override
	public Frustum sodium$createFrustum() {
		return this;
	}
}
