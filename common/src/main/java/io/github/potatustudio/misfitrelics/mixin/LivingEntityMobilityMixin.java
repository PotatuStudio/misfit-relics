package io.github.potatustudio.misfitrelics.mixin;

import io.github.potatustudio.misfitrelics.mobility.Mobility;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Reliquias de movilidad que tocan la física del jugador (en el cliente y en el servidor, para que coincidan):
 * los guantes de la viuda hacen que las paredes cuenten como escalera, y las botas de magma dejan pararse sobre la lava.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMobilityMixin {
	@Shadow
	protected boolean jumping;

	@Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
	private void misfitrelics$widowClimb(CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof Player player && !player.isSpectator() && Mobility.widowClimbs(player, jumping)) {
			cir.setReturnValue(true);
		}
	}

	/**
	 * La lava solo es sólida para quien devuelve una forma aquí (el strider devuelve media altura de bloque). Sin esto,
	 * canStandOnFluid por sí solo no sostiene al jugador: se hunde y no puede saltar.
	 */
	@Inject(method = "getLiquidCollisionShape", at = @At("HEAD"), cancellable = true)
	private void misfitrelics$magmaSurface(CallbackInfoReturnable<VoxelShape> cir) {
		if ((Object) this instanceof Player player && Mobility.walksOnLava(player)) {
			cir.setReturnValue(Mobility.LAVA_SURFACE);
		}
	}

	@Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
	private void misfitrelics$magmaWalk(FluidState fluid, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof Player player && fluid.is(FluidTags.LAVA) && !player.isShiftKeyDown()
				&& Mobility.isWearing(player, ModItems.MAGMA_BOOTS.get())) {
			cir.setReturnValue(true);
		}
	}
}
