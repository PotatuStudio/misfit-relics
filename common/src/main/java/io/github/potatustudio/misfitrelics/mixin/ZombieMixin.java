package io.github.potatustudio.misfitrelics.mixin;

import io.github.potatustudio.misfitrelics.registry.ModAttachments;
import io.github.potatustudio.misfitrelics.registry.ModEffects;
import io.github.potatustudio.misfitrelics.registry.ModGameRules;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.stench.StenchData;
import java.util.Optional;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Portadores del Hedor: un pequeño porcentaje de zombis (incluye husks, drowned y aldeanos zombi)
 * aparece con Hedor infinito, también desde huevos, {@code /summon} y spawners. No sufren su propio Hedor, pero contagian.
 */
@Mixin(Zombie.class)
public abstract class ZombieMixin {
	@Inject(method = "finalizeSpawn", at = @At("RETURN"))
	private void misfitrelics$maybeSpawnAsCarrier(ServerLevelAccessor levelAccessor, DifficultyInstance difficulty,
			EntitySpawnReason reason, SpawnGroupData groupData, CallbackInfoReturnable<SpawnGroupData> cir) {
		// Cualquier aparición nueva (natural, huevo, /summon, spawner, refuerzos...). Se excluyen las que no
		// crean un zombi nuevo: conversión (aldeano que se zombifica), carga del chunk y cambio de dimensión.
		switch (reason) {
			case CONVERSION, LOAD, DIMENSION_TRAVEL, BUCKET, BREEDING -> {
				return;
			}
			default -> {
			}
		}
		Zombie self = (Zombie) (Object) this;
		int chance = levelAccessor.getLevel().getGameRules().get(ModGameRules.STENCH_CARRIER_CHANCE.get());
		if (chance <= 0 || self.getRandom().nextInt(100) >= chance) {
			return;
		}
		self.addEffect(new MobEffectInstance(ModEffects.STENCH.asHolder(), MobEffectInstance.INFINITE_DURATION, 0, false, true));
		EntityData.set(self, ModAttachments.STENCH, new StenchData(Optional.empty(), true, true));
	}
}
