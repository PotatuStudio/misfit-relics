package io.github.potatustudio.misfitrelics.mixin;

import io.github.potatustudio.misfitrelics.mobility.Mobility;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Botas de magma: inmune al fuego desde el primer contacto con la lava (y un rato después de salir), así no prende
 * ni recibe daño en el primer tick.
 */
@Mixin(Entity.class)
public abstract class EntityFireMixin {
	@Inject(method = "fireImmune", at = @At("HEAD"), cancellable = true)
	private void misfitrelics$magmaFireImmune(CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof Player player && Mobility.magmaProtected(player)) {
			cir.setReturnValue(true);
		}
	}
}
