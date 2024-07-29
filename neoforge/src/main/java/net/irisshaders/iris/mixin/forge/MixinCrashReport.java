package net.irisshaders.iris.mixin.forge;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.CrashReport;
import net.minecraft.ReportType;
import net.neoforged.fml.loading.LoadingModList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(CrashReport.class)
public class MixinCrashReport {
	@Shadow
	@Final
	private Throwable exception;

	@Inject(method = "getFriendlyReport(Lnet/minecraft/ReportType;Ljava/util/List;)Ljava/lang/String;", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;", ordinal = 0))
	private void addIrisToReportIfNeeded(ReportType p_350860_, List<String> p_350563_, CallbackInfoReturnable<String> cir, @Local StringBuilder builder) {
		try {
			if (LoadingModList.get().getModFileById("embeddium") == null) return;

			if (this.exception != null) {
				AtomicBoolean relatedToIris = new AtomicBoolean(false);

				Arrays.stream(this.exception.getStackTrace()).forEach(element -> {
					if (element.getClassName().contains("net.irisshaders") || element.getClassName().contains("org.embeddedt") || element.getClassName().contains("me.jellysquid.mods.sodium")) {
						relatedToIris.set(true);
					}
				});

				if (!relatedToIris.get()) {
					Throwable cause = this.exception.getCause();
					while (cause != null) {
						cause = cause.getCause();
						Arrays.stream(this.exception.getStackTrace()).forEach(element -> {
							if (element.getClassName().contains("net.irisshaders") || element.getClassName().contains("org.embeddedt") || element.getClassName().contains("me.jellysquid.mods.sodium")) {
								relatedToIris.set(true);
							}
						});
					}
				}

				if (relatedToIris.get()) {
					builder.append("*** IRIS COMPATIBILITY ***\n");
					builder.append("Errors with Iris when in use with Embeddium should not be reported to the Iris team. Please confirm any issues with Sodium before reporting.\n\n");
				}
			}
		} catch (Exception ignored) {

		}
	}
}
