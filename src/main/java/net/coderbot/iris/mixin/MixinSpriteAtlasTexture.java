package net.coderbot.iris.mixin;

import net.coderbot.iris.texunits.SpriteAtlasTextureInterface;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(SpriteAtlasTexture.class)
public class MixinSpriteAtlasTexture implements SpriteAtlasTextureInterface {
	private Vec2f atlasSize;

	//@Inject(method = "stitch", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void getAtlasSize(ResourceManager resourceManager, Stream<Identifier> idStream, Profiler profiler, int mipmapLevel, CallbackInfoReturnable<SpriteAtlasTexture.Data> cir, TextureStitcher textureStitcher) {
		atlasSize = new Vec2f(1024, 1024);
	}

	@Override
	public Vec2f getAtlasSize() {
		return atlasSize;
	}
}

