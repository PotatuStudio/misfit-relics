package io.github.potatustudio.misfitrelics.trumpet;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;

/**
 * Aturdido: la entidad no se mueve, no salta, no vuela y sus golpes no hacen daño.
 * Los mobs además pierden su objetivo y los creepers dejan de inflarse.
 */
public class StunnedEffect extends MobEffect {
	public static final int COLOR = 0xF2D16B;

	public StunnedEffect() {
		super(MobEffectCategory.HARMFUL, COLOR);
		AttributeModifier.Operation total = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
		addAttributeModifier(Attributes.MOVEMENT_SPEED, MisfitRelics.id("effect.stunned.movement"), -1.0, total);
		addAttributeModifier(Attributes.FLYING_SPEED, MisfitRelics.id("effect.stunned.flying"), -1.0, total);
		addAttributeModifier(Attributes.JUMP_STRENGTH, MisfitRelics.id("effect.stunned.jump"), -1.0, total);
		addAttributeModifier(Attributes.ATTACK_DAMAGE, MisfitRelics.id("effect.stunned.attack"), -1.0, total);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
		if (entity instanceof Mob mob) {
			mob.getNavigation().stop();
			// Sin objetivo no atacan (incluye arqueros). Al pasar el efecto lo vuelven a buscar solos.
			mob.setTarget(null);
			if (mob instanceof Creeper creeper) {
				creeper.setSwellDir(-1);
			}
		}
		return true;
	}
}
