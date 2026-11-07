package io.github.potatustudio.misfitrelics.spud;

import io.github.potatustudio.misfitrelics.registry.ModEffects;
import io.github.potatustudio.misfitrelics.registry.ModEntityTypes;
import io.github.potatustudio.misfitrelics.stench.StenchEffect;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Papa disparada por el cañón. El efecto depende de la munición:
 * papa normal = solo daño; papa venenosa = además, nube de Hedor.
 */
public class SpudProjectile extends ThrowableItemProjectile {
	/** Daño por impacto directo (un corazón y medio). */
	public static final float IMPACT_DAMAGE = 3.0f;
	public static final float CLOUD_RADIUS = 2.5f;
	public static final int CLOUD_DURATION = 120;
	/** Duración del Hedor que aplica la nube (10 s). */
	public static final int CLOUD_EFFECT_DURATION = 200;
	private static final byte EVENT_BREAK = 3;

	public SpudProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) {
		super(type, level);
	}

	public SpudProjectile(Level level, LivingEntity owner, ItemStack ammo) {
		super(ModEntityTypes.SPUD, owner, level, ammo);
	}

	@Override
	protected Item getDefaultItem() {
		return Items.POTATO;
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		if (level() instanceof ServerLevel level) {
			hitResult.getEntity().hurtServer(level, damageSources().thrown(this, getOwner()), IMPACT_DAMAGE);
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (level() instanceof ServerLevel level) {
			if (getItem().is(Items.POISONOUS_POTATO)) {
				spawnStenchCloud(level, hitResult);
			}
			level.broadcastEntityEvent(this, EVENT_BREAK);
			discard();
		}
	}

	private void spawnStenchCloud(ServerLevel level, HitResult hitResult) {
		AreaEffectCloud cloud = new AreaEffectCloud(level, hitResult.getLocation().x, hitResult.getLocation().y,
				hitResult.getLocation().z);
		if (getOwner() instanceof LivingEntity owner) {
			// La nube recuerda a su dueño: así el Hedor sabe quién lo originó (reglas de aliados).
			cloud.setOwner(owner);
		}
		cloud.setRadius(CLOUD_RADIUS);
		cloud.setRadiusOnUse(-0.1f);
		cloud.setWaitTime(5);
		cloud.setDuration(CLOUD_DURATION);
		cloud.setRadiusPerTick(-CLOUD_RADIUS / CLOUD_DURATION);
		cloud.setPotionDurationScale(1.0f);
		cloud.setCustomParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFF000000 | StenchEffect.COLOR));
		cloud.addEffect(new MobEffectInstance(ModEffects.STENCH, CLOUD_EFFECT_DURATION));
		level.addFreshEntity(cloud);
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == EVENT_BREAK) {
			ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, getItem().getItem());
			for (int i = 0; i < 8; i++) {
				level().addParticle(particle, getX(), getY(), getZ(),
						(random.nextDouble() - 0.5) * 0.15, random.nextDouble() * 0.15, (random.nextDouble() - 0.5) * 0.15);
			}
		} else {
			super.handleEntityEvent(id);
		}
	}
}
