package net.coderbot.iris.mixin;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Suppresses Minecraft's authentication check in development environments. It's unnecessary log spam, and there's no
 * need to send off a network request to Microsoft telling them that we're using Fabric/Quilt every time we launch the
 * game in the development environment.
 */
@Mixin(Minecraft.class)
public class MixinMinecraft_NoAuthInDev {
	@Shadow
	@Final
	private static Logger LOGGER;
}
