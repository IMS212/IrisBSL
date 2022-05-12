package net.coderbot.iris.compat.sodium.mixin.entity_render;

import com.mojang.math.Vector3f;
import net.coderbot.iris.compat.sodium.impl.vertex_format.BakedQuadInterface;
import net.coderbot.iris.compat.sodium.impl.vertex_format.VertexInterface;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BakedQuad.class)
public class MixinBakedQuad implements BakedQuadInterface {
	@Unique
	Vector3f[] prevXYZ = new Vector3f[4];

	@Inject(method = "<init>", at = @At("TAIL"))
	private void setVelocityVectors(int[] is, int i, Direction arg, TextureAtlasSprite arg2, boolean bl, CallbackInfo ci) {
		for (int it = 0; it < prevXYZ.length; it++) {
			prevXYZ[it] = new Vector3f();
		}
	}

	@Override
	public float getPrevX(int i) {
		return prevXYZ[i].x();
	}

	@Override
	public float getPrevY(int i) {
		return prevXYZ[i].y();
	}

	@Override
	public float getPrevZ(int i) {
		return prevXYZ[i].z();
	}

	@Override
	public void updatePrevValues(int i, float x, float y, float z) {
		prevXYZ[i].set(x, y, z);
	}
}
