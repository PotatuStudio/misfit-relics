package io.github.potatustudio.misfitrelics.mixin;

import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.registry.ModAttachments;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.registry.ModEffects;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.stench.StenchData;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.stench.StenchRules;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import java.util.Optional;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import java.util.UUID;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import net.minecraft.server.level.ServerLevel;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import net.minecraft.world.effect.MobEffectInstance;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import net.minecraft.world.entity.Entity;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import net.minecraft.world.entity.LivingEntity;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import org.jspecify.annotations.Nullable;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import org.spongepowered.asm.mixin.Mixin;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import org.spongepowered.asm.mixin.injection.At;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import org.spongepowered.asm.mixin.injection.Inject;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cuando una entidad recibe Hedor de una poción, nube o proyectil:
 * registra quién lo originó y bloquea el efecto si las game rules protegen a ese objetivo.
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
			return;
		}
		EntityData.set(self, ModAttachments.STENCH, new StenchData(source, true, false));
	}
}
