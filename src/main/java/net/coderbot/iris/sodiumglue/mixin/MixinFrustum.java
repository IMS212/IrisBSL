package net.coderbot.iris.sodiumglue.mixin;

import me.jellysquid.mods.sodium.client.util.math.FrustumExtended;
import net.coderbot.iris.sodiumglue.impl.IrisFrustumExtended;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Frustum.class, priority = 999)
public abstract class MixinFrustum implements FrustumExtended, IrisFrustumExtended {
	@Shadow
	protected abstract boolean cubeInFrustum(float f, float g, float h, float i, float j, float k);

	private float irisxF, irisyF, iriszF;


    @Inject(method = "prepare", at = @At("HEAD"))
    private void prePositionUpdate(double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        this.irisxF = (float) cameraX;
        this.irisyF = (float) cameraY;
        this.iriszF = (float) cameraZ;
    }

    @Override
    public boolean fastAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        if (!preAabbTest(minX, minY, minZ, maxX, maxY, maxZ)) {
            return false;
        }

        return this.cubeInFrustum(minX - this.irisxF, minY - this.irisyF, minZ - this.iriszF,
                maxX - this.irisxF, maxY - this.irisyF, maxZ - this.iriszF);
    }
}
