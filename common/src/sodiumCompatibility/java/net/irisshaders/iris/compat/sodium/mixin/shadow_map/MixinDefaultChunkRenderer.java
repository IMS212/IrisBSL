package net.irisshaders.iris.compat.sodium.mixin.shadow_map;

import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptions;
import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DefaultChunkRenderer.class)
public class MixinDefaultChunkRenderer {
    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/gui/SodiumGameOptions$PerformanceSettings;useBlockFaceCulling:Z"), remap = false)
    private boolean iris$disableBlockFaceCullingInShadowPass(SodiumGameOptions.PerformanceSettings instance) {
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) return false;
        return instance.useBlockFaceCulling;
    }
}
