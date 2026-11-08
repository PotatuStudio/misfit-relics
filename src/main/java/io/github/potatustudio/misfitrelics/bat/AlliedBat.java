package io.github.potatustudio.misfitrelics.bat;

import io.github.potatustudio.misfitrelics.util.Allies;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Murciélago aliado del collar. Revolotea alrededor de la cabeza de su dueño y muerde a lo que el dueño golpea
 * o a quien lo golpea a él. Cada mordisco cura al dueño con el robo de vida del collar.
 * <p>
 * No se guarda con el mundo: lo invoca {@link BatCollar} mientras el dueño lleve el collar. Si muere, el collar
 * queda "reviviendo" un rato antes de invocar otro.
 */
public class AlliedBat extends Bat implements OwnableEntity {
	public static final float BITE_DAMAGE = 3.0f;
	public static final int BITE_COOLDOWN = 20;
	public static final double MAX_HEALTH = 12.0;
	/** Ticks desde el golpe del dueño (o al dueño) en que todavía persigue a ese objetivo (6 s). */
	private static final int TARGET_MEMORY = 120;
	private static final double TARGET_RANGE = 16.0;
	private static final double TELEPORT_DISTANCE = 24.0;

	private @Nullable EntityReference<LivingEntity> owner;
	private int biteCooldown;

	public AlliedBat(EntityType<? extends Bat> type, Level level) {
		super(type, level);
		setResting(false);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Bat.createAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH);
	}

	public void setOwner(Player player) {
		this.owner = EntityReference.of(player);
	}

	@Override
	public @Nullable EntityReference<LivingEntity> getOwnerReference() {
		return owner;
	}

	/** Lo invoca el collar; no se guarda en el mundo. */
	@Override
	public boolean shouldBeSaved() {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	/** Reemplaza el vuelo al azar del murciélago vanilla. */
	@Override
	protected void customServerAiStep(ServerLevel level) {
		setResting(false);
		if (!(getOwner() instanceof Player player) || !player.isAlive() || player.level() != level
				|| BatCollar.findWorn(player).isEmpty()) {
			discard();
			return;
		}
		if (biteCooldown > 0) {
			biteCooldown--;
		}
		LivingEntity target = pickTarget(level, player);
		Vec3 goal;
		double speed;
		if (target != null) {
			goal = target.getBoundingBox().getCenter();
			speed = 0.7;
			if (biteCooldown == 0 && getBoundingBox().inflate(0.6).intersects(target.getBoundingBox())) {
				bite(level, player, target);
			}
		} else {
			double angle = tickCount * 0.12;
			goal = player.getEyePosition().add(Math.cos(angle) * 1.1, 0.7 + Math.sin(tickCount * 0.07) * 0.25,
					Math.sin(angle) * 1.1);
			speed = 0.45;
			if (distanceToSqr(player) > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
				snapTo(goal.x, goal.y, goal.z, getYRot(), 0f);
			}
		}
		fly(goal, speed);
	}

	private void fly(Vec3 goal, double speed) {
		Vec3 delta = goal.subtract(position());
		double distance = delta.length();
		Vec3 wanted = distance < 1.0E-3 ? Vec3.ZERO : delta.scale(Math.min(speed, distance * 0.35) / distance);
		Vec3 current = getDeltaMovement();
		setDeltaMovement(current.add(wanted.subtract(current).scale(0.3)));
		if (wanted.horizontalDistanceSqr() > 1.0E-4) {
			float yaw = (float) (Mth.atan2(wanted.z, wanted.x) * Mth.RAD_TO_DEG) - 90.0f;
			setYRot(Mth.wrapDegrees(yaw));
		}
		zza = 0.5f;
	}

	private @Nullable LivingEntity pickTarget(ServerLevel level, Player player) {
		LivingEntity hit = player.getLastHurtMob();
		if (hit != null && player.tickCount - player.getLastHurtMobTimestamp() < TARGET_MEMORY && isValidTarget(level, player, hit)) {
			return hit;
		}
		LivingEntity attacker = player.getLastHurtByMob();
		if (attacker != null && player.tickCount - player.getLastHurtByMobTimestamp() < TARGET_MEMORY
				&& isValidTarget(level, player, attacker)) {
			return attacker;
		}
		return null;
	}

	private boolean isValidTarget(ServerLevel level, Player player, LivingEntity target) {
		return target.isAlive() && target != this && target != player && !(target instanceof AlliedBat)
				&& distanceToSqr(target) < TARGET_RANGE * TARGET_RANGE && !Allies.isAlly(level, target, player.getUUID());
	}

	private void bite(ServerLevel level, Player player, LivingEntity target) {
		biteCooldown = BITE_COOLDOWN;
		if (target.hurtServer(level, damageSources().mobAttack(this), BITE_DAMAGE)) {
			player.heal(BITE_DAMAGE * BatCollar.LIFESTEAL);
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.BAT_HURT, SoundSource.PLAYERS, 0.6f, 1.6f);
		}
	}

	/** El dueño no puede herir a su murciélago (por ejemplo, al golpear a lo loco en una pelea). */
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (owner != null && source.getEntity() != null && owner.getUUID().equals(source.getEntity().getUUID())) {
			return false;
		}
		return super.hurtServer(level, source, amount);
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (getOwner() instanceof Player player) {
			BatCollar.onAlliedBatDied(player);
		}
	}
}
