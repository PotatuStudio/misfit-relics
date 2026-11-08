package io.github.potatustudio.misfitrelics.bat;

import io.github.potatustudio.misfitrelics.registry.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Rey murciélago: la variante especial del murciélago aliado, que invoca el collar del rey murciélago.
 * <p>
 * Inspirado en los machos dominantes de verdad (el macho de harén del murciélago lancero mayor y el cabeza de
 * martillo, cuyos machos pesan casi el doble que las hembras): más grande y gordo, muerde más fuerte, roba más vida
 * y cada tanto lanza un chillido que aturde a los enemigos cercanos.
 */
public class KingBat extends AlliedBat {
	public static final float KING_BITE_DAMAGE = 6.0f;
	public static final float KING_LIFESTEAL = 0.25f;
	public static final double KING_MAX_HEALTH = 24.0;
	/** Chillido: cada 10 s, aturde 1 s a los enemigos en 5 bloques. */
	public static final int SCREECH_COOLDOWN = 200;
	public static final int SCREECH_STUN = 20;
	public static final double SCREECH_RADIUS = 5.0;

	private int screechCooldown = SCREECH_COOLDOWN / 2;

	public KingBat(EntityType<? extends Bat> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createKingAttributes() {
		return Bat.createAttributes().add(Attributes.MAX_HEALTH, KING_MAX_HEALTH);
	}

	@Override
	protected float biteDamage() {
		return KING_BITE_DAMAGE;
	}

	@Override
	protected float lifesteal() {
		return KING_LIFESTEAL;
	}

	@Override
	protected void extraAi(ServerLevel level, Player player, @Nullable LivingEntity target) {
		if (screechCooldown > 0) {
			screechCooldown--;
			return;
		}
		if (target == null) {
			return;
		}
		screechCooldown = SCREECH_COOLDOWN;
		for (LivingEntity enemy : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(SCREECH_RADIUS),
				e -> isValidTarget(level, player, e))) {
			enemy.addEffect(new MobEffectInstance(ModEffects.STUNNED, SCREECH_STUN), this);
		}
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.BAT_TAKEOFF, SoundSource.PLAYERS, 1.5f, 0.5f);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.BAT_AMBIENT, SoundSource.PLAYERS, 2.0f, 0.4f);
		level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, getX(), getY() + 0.4, getZ(), 30, SCREECH_RADIUS * 0.4, 0.4,
				SCREECH_RADIUS * 0.4, 0.05);
	}
}
