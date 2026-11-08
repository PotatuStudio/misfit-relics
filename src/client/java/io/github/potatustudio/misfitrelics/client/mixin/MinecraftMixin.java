package io.github.potatustudio.misfitrelics.client.mixin;

import io.github.potatustudio.misfitrelics.client.EcholocationClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Las entidades detectadas por la ecolocación se dibujan con contorno, solo para este jugador. */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Inject(method = "shouldEntityAppearGlowing", at = @At("RETURN"), cancellable = true)
	private void misfitrelics$echolocationOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ() && EcholocationClient.isMarked(entity)) {
			cir.setReturnValue(true);
		}
	}
}
