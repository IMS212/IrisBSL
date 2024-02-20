package net.irisshaders.iris.compat.sodium.mixin.font;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.RedStoneOreBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// A hack to fix the most ridiculous Loom bug I've seen so far.
@Mixin(RedStoneOreBlock.class)
public abstract class MixinSpriteFinderCache {
}
