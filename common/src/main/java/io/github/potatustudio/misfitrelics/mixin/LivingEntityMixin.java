package io.github.potatustudio.misfitrelics.mixin;

import io.github.potatustudio.misfitrelics.registry.ModAttachments;
import io.github.potatustudio.misfitrelics.registry.ModEffects;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.stench.StenchData;
import io.github.potatustudio.misfitrelics.stench.StenchRules;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cuando una entidad recibe Hedor de una poción, nube o proyectil:
 * bloquea el efecto si las game rules protegen a ese objetivo y, si entró, registra quién lo originó.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(
			method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
			at = @At("HEAD"),
			cancellable = true)
	private void misfitrelics$trackStenchSource(MobEffectInstance effect, @Nullable Entity effectSource,
			CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!effect.is(ModEffects.STENCH.asHolder()) || !(self.level() instanceof ServerLevel level)) {
			return;
		}
		Optional<UUID> source = effectSource == null ? Optional.empty() : StenchRules.resolveSource(effectSource);
		if (!StenchRules.canAffect(level, self, source)) {
			cir.setReturnValue(false);
		}
	}

	/** Registra quién originó el Hedor solo si el efecto entró; un portador sigue siéndolo (no empieza a dañarse). */
	@Inject(
			method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
			at = @At("RETURN"))
	private void misfitrelics$recordStenchSource(MobEffectInstance effect, @Nullable Entity effectSource,
			CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!cir.getReturnValueZ() || !effect.is(ModEffects.STENCH.asHolder()) || self.level().isClientSide()) {
			return;
		}
		StenchData current = EntityData.get(self, ModAttachments.STENCH);
		if (current != null && current.carrier()) {
			return;
		}
		Optional<UUID> source = effectSource == null ? Optional.empty() : StenchRules.resolveSource(effectSource);
		EntityData.set(self, ModAttachments.STENCH, new StenchData(source, true, false));
	}
}
