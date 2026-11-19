package io.github.potatustudio.misfitrelics.stench;

import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.registry.ModAttachments;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.registry.ModDamageTypes;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.registry.ModEffects;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.registry.ModGameRules;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.registry.ModParticles;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import net.minecraft.core.particles.ParticleOptions;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import net.minecraft.core.registries.Registries;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import net.minecraft.server.level.ServerLevel;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import net.minecraft.world.damagesource.DamageSource;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import net.minecraft.world.effect.MobEffect;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import net.minecraft.world.effect.MobEffectCategory;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import net.minecraft.world.effect.MobEffectInstance;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import net.minecraft.world.entity.LivingEntity;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import net.minecraft.world.entity.player.Player;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import net.minecraft.world.phys.AABB;

/**
 * Hedor: daño periódico que se contagia a las entidades cercanas.
 * Cada salto de contagio dura un 25 % menos, así la cadena se apaga sola.
 */
public class StenchEffect extends MobEffect {
	/** Color verde amarillento de la nube. */
	public static final int COLOR = 0x8DB33A;
	/** Cada cuántos ticks se intenta contagiar (1 s). */
	public static final int SPREAD_INTERVAL = 20;
	/** Radio de contagio en bloques alrededor de la caja de colisión. */
	public static final double SPREAD_RADIUS = 1.5;
	/** Fracción de la duración que recibe el contagiado. */
	public static final float SPREAD_DECAY = 0.75f;
	/** Bajo esta duración (3 s) ya no se contagia. */
	public static final int MIN_SPREAD_DURATION = 60;
	/** Duración base que contagia un portador (efecto infinito). */
	public static final int CARRIER_SPREAD_DURATION = 600;
	/** Daño por golpe de Hedor (medio corazón). */
	public static final float DAMAGE = 1.0f;

	public StenchEffect() {
		super(MobEffectCategory.HARMFUL, COLOR);
	}

	/** Nivel I: cada 2 s. Cada nivel extra divide el intervalo, como el veneno. */
	static int damageInterval(int amplifier) {
		return Math.max(10, 40 >> amplifier);
	}

	/** En vez de los remolinos de color de los efectos, revolotean moscas. */
	@Override
	public ParticleOptions createParticleOptions(MobEffectInstance effect) {
		return ModParticles.FLY.get();
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		// Se decide en applyEffectTick con tickCount, porque la duración de un efecto infinito no avanza.
		return true;
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
		StenchData data = EntityData.getOrElse(entity, ModAttachments.STENCH, StenchData.NONE);

		if (entity.tickCount % damageInterval(amplifier) == 0) {
			hurt(level, entity, data);
		}
		if (data.canSpread() && entity.tickCount % SPREAD_INTERVAL == 0) {
			spread(level, entity, data);
		}
		return true;
	}

	private static void hurt(ServerLevel level, LivingEntity entity, StenchData data) {
		if (data.carrier()) {
			return;
		}
		if (!level.getGameRules().get(ModGameRules.STENCH_CAN_KILL.get()) && entity.getHealth() <= DAMAGE) {
			return;
		}
		DamageSource source = new DamageSource(
				level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ModDamageTypes.STENCH));
		entity.hurtServer(level, source, DAMAGE);
	}

	private static void spread(ServerLevel level, LivingEntity carrier, StenchData data) {
		MobEffectInstance current = carrier.getEffect(ModEffects.STENCH.asHolder());
		if (current == null) {
			return;
		}
		int base = current.isInfiniteDuration() ? CARRIER_SPREAD_DURATION : current.getDuration();
		int next = (int) (base * SPREAD_DECAY);
		if (next < MIN_SPREAD_DURATION) {
			return;
		}
		boolean chain = level.getGameRules().get(ModGameRules.STENCH_CHAIN_CONTAGION.get());
		AABB area = carrier.getBoundingBox().inflate(SPREAD_RADIUS);

		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, t -> isValidTarget(t, carrier))) {
			MobEffectInstance existing = target.getEffect(ModEffects.STENCH.asHolder());
			if (existing != null && (existing.isInfiniteDuration() || existing.getDuration() >= next)) {
				continue;
			}
			if (!StenchRules.canAffect(level, target, data.source())) {
				continue;
			}
			// addEffect pasa por LivingEntityMixin, que registra un origen; aquí se conserva el original y la cadena.
			if (target.addEffect(new MobEffectInstance(ModEffects.STENCH.asHolder(), next, current.getAmplifier()), carrier)) {
				EntityData.set(target, ModAttachments.STENCH, new StenchData(data.source(), chain, false));
			}
		}
	}

	private static boolean isValidTarget(LivingEntity target, LivingEntity carrier) {
		if (target == carrier || !target.isAlive() || target.isSpectator()) {
			return false;
		}
		return !(target instanceof Player player && player.isCreative());
	}
}
