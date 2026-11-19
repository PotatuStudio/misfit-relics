package io.github.potatustudio.misfitrelics.combat;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Enredado (flechas del carcaj del bosque): enredaderas con espinas atrapan a la entidad. No se mueve ni salta, y las
 * espinas la pinchan cada segundo. A diferencia del aturdido, sí puede atacar a lo que tenga al alcance.
 */
public class EntangledEffect extends MobEffect {
	public static final int COLOR = 0x3F7A2A;
	public static final float THORN_DAMAGE = 1.0f;

	public EntangledEffect() {
		super(MobEffectCategory.HARMFUL, COLOR);
		AttributeModifier.Operation total = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
		addAttributeModifier(Attributes.MOVEMENT_SPEED, MisfitRelics.id("effect.entangled.movement"), -1.0, total);
		addAttributeModifier(Attributes.JUMP_STRENGTH, MisfitRelics.id("effect.entangled.jump"), -1.0, total);
		addAttributeModifier(Attributes.FLYING_SPEED, MisfitRelics.id("effect.entangled.flying"), -1.0, total);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return duration % 20 == 0;
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
		if (entity instanceof Mob mob) {
			mob.getNavigation().stop();
		}
		entity.hurtServer(level, entity.damageSources().sweetBerryBush(), THORN_DAMAGE);
		level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, entity.getX(), entity.getY() + 0.3, entity.getZ(),
				6, 0.3, 0.2, 0.3, 0.0);
		return true;
	}
}
