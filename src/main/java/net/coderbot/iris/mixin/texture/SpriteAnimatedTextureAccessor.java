package net.coderbot.iris.mixin.texture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(targets = "net/minecraft/client/renderer/texture/TextureAtlasSprite$AnimatedTexture")
public interface SpriteAnimatedTextureAccessor {
	@Accessor("frame")
	int getFrame();

	@Accessor("frame")
	void setFrame(int frame);

	@Accessor("subFrame")
    int getSubFrame();

	@Accessor("subFrame")
    void setSubFrame(int subFrame);

	@Accessor("frames")
	List<Object> getFrames();

	@Invoker("uploadFrame")
	void invokeUploadFrame(int frameIndex);
}
