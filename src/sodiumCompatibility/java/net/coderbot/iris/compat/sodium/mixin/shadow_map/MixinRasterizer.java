package net.coderbot.iris.compat.sodium.mixin.shadow_map;

import me.jellysquid.mods.sodium.client.render.chunk.graph.Rasterizer;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.coderbot.iris.uniforms.CelestialUniforms;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Rasterizer.class)
public class MixinRasterizer {
	@Redirect(method = "setCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;toVector3f()Lorg/joml/Vector3f;"))
	private Vector3f changeShadowPos(Vec3 instance) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			Vector4f value = ShadowRenderingState.getCelestialUniforms().getShadowLightPositionInWorldSpace2();
			return new Vector3f(value.x, value.y, value.z);
		}
		return instance.toVector3f();
	}
}
