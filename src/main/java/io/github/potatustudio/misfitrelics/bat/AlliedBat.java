package io.github.potatustudio.misfitrelics.bat;

import io.github.potatustudio.misfitrelics.util.Allies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
	protected static final double TARGET_RANGE = 16.0;
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

	/** Qué está haciendo el murciélago cuando no hay pelea. */
	private enum Mode {
		/** Gira alrededor de la cabeza del dueño. */
		FOLLOW,
		/** Un vuelo corto cerca del dueño, cazando insectos. */
		WANDER,
		/** Vuela hacia un techo para colgarse. */
		SEEK_ROOST,
		/** Colgado boca abajo, descansando. */
		ROOSTING,
		/** Posado sobre la cabeza del dueño (montado en él, así lo sigue sin tirones). */
		PERCH
	}

	/** Cada cuánto decide qué hacer mientras acompaña (1 s). */
	private static final int DECIDE_EVERY = 20;
	/** Distancia máxima del dueño a la que sigue colgado. */
	private static final double ROOST_LEASH = 12.0;
	/** De noche se cuelga si el dueño lleva quieto este tiempo (15 s). */
	private static final int NIGHT_IDLE_TO_ROOST = 300;

	private Mode mode = Mode.FOLLOW;
	private @Nullable BlockPos roost;
	private @Nullable Vec3 wanderGoal;
	private int modeTicks;
	private int roostCooldown;
	private int ownerIdleTicks;
	private Vec3 lastOwnerPos = Vec3.ZERO;

	/** Reemplaza el vuelo al azar del murciélago vanilla por un compañero con rutinas. */
	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (!(getOwner() instanceof Player player) || !player.isAlive() || player.level() != level
				|| BatCollar.findWorn(player).isEmpty()) {
			discard();
			return;
		}
		if (biteCooldown > 0) {
			biteCooldown--;
		}
		if (roostCooldown > 0) {
			roostCooldown--;
		}
		modeTicks++;
		ownerIdleTicks = player.position().distanceToSqr(lastOwnerPos) < 0.01 ? ownerIdleTicks + 1 : 0;
		lastOwnerPos = player.position();

		LivingEntity target = pickTarget(level, player);
		extraAi(level, player, target);
		if (target != null) {
			// Pelea: deja lo que estaba haciendo.
			wake();
			setMode(Mode.FOLLOW);
			fly(target.getBoundingBox().getCenter(), 0.7);
			if (biteCooldown == 0 && getBoundingBox().inflate(0.6).intersects(target.getBoundingBox())) {
				bite(level, player, target);
			}
			return;
		}
		if (mode == Mode.PERCH) {
			perch(player);
			return;
		}
		if (distanceToSqr(player) > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
			wake();
			setMode(Mode.FOLLOW);
			Vec3 near = player.getEyePosition().add(0, 0.8, 0);
			snapTo(near.x, near.y, near.z, getYRot(), 0f);
			return;
		}
		switch (mode) {
			case ROOSTING -> roosting(level, player);
			case SEEK_ROOST -> seekRoost(level);
			case WANDER -> wander(level, player);
			default -> follow(level, player);
		}
	}

	/** Radio y altura de la órbita alrededor del dueño; el rey, más grande, vuela más lejos para no estorbar. */
	protected double orbitRadius() {
		return 1.9;
	}

	protected double orbitHeight() {
		return 1.1;
	}

	/** ¿Puede posarse sobre la cabeza del dueño? (El rey es demasiado grande.) */
	protected boolean canPerch() {
		return true;
	}

	private void follow(ServerLevel level, Player player) {
		// De día y al sol, sin refugio a mano: se queda más cerca del dueño, pero sin estorbar.
		boolean exposed = level.isBrightOutside() && level.canSeeSky(blockPosition());
		double radius = orbitRadius() * (exposed ? 0.8 : 1.0);
		double angle = tickCount * 0.09 + getId();
		Vec3 goal = player.getEyePosition().add(Math.cos(angle) * radius,
				orbitHeight() * (exposed ? 0.7 : 1.0) + Math.sin(tickCount * 0.07) * 0.25, Math.sin(angle) * radius);
		fly(goal, 0.45);

		if (modeTicks % DECIDE_EVERY != 0) {
			return;
		}
		if (roostCooldown == 0 && wantsToRoost(level)) {
			BlockPos spot = findRoost(level, player);
			if (spot != null) {
				roost = spot;
				setMode(Mode.SEEK_ROOST);
				return;
			}
			roostCooldown = 60;
		}
		if (canPerch() && random.nextInt(6) == 0 && !player.isFallFlying() && !player.isSprinting()
				&& player.getPassengers().isEmpty() && startRiding(player, true, true)) {
			// Se posa sobre la cabeza del dueño un rato.
			setDeltaMovement(Vec3.ZERO);
			setMode(Mode.PERCH);
			return;
		}
		if (random.nextInt(level.isDarkOutside() ? 4 : 8) == 0) {
			// Sale a cazar insectos un momento (de noche, más seguido).
			wanderGoal = player.position().add(random.nextGaussian() * 3.5, 1.5 + random.nextDouble() * 2.5,
					random.nextGaussian() * 3.5);
			setMode(Mode.WANDER);
		}
	}

	private void wander(ServerLevel level, Player player) {
		if (wanderGoal == null || modeTicks > 80 || position().distanceToSqr(wanderGoal) < 0.4) {
			setMode(Mode.FOLLOW);
			return;
		}
		fly(wanderGoal, 0.55);
		if (modeTicks % 15 == 0 && random.nextInt(3) == 0) {
			// Atrapa un insecto al vuelo.
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.BAT_AMBIENT, SoundSource.NEUTRAL, 0.25f, 1.8f);
			level.sendParticles(ParticleTypes.WHITE_ASH, getX(), getY() + 0.3, getZ(), 3, 0.1, 0.1, 0.1, 0.0);
		}
	}

	private void seekRoost(ServerLevel level) {
		if (roost == null || !canRoostAt(level, roost) || modeTicks > 200) {
			roostCooldown = 200;
			setMode(Mode.FOLLOW);
			return;
		}
		Vec3 hang = hangPosition(roost);
		fly(hang, 0.5);
		if (position().distanceToSqr(hang) < 0.2) {
			snapTo(hang.x, hang.y, hang.z, getYRot(), 0f);
			setDeltaMovement(Vec3.ZERO);
			setResting(true);
			setMode(Mode.ROOSTING);
		}
	}

	private void roosting(ServerLevel level, Player player) {
		boolean tooFar = roost == null || player.distanceToSqr(Vec3.atCenterOf(roost)) > ROOST_LEASH * ROOST_LEASH;
		if (!isResting() || roost == null || !canRoostAt(level, roost) || tooFar || !wantsToRoost(level)) {
			wake();
			roostCooldown = 100;
			setMode(Mode.FOLLOW);
			return;
		}
		if (random.nextInt(600) == 0) {
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.BAT_AMBIENT, SoundSource.NEUTRAL, 0.15f, 0.8f);
		}
	}

	private void perch(Player player) {
		boolean restless = modeTicks > 200 + random.nextInt(200) || player.isSprinting() || player.isFallFlying()
				|| player.isInWater() || getVehicle() != player;
		if (restless) {
			stopRiding();
			setMode(Mode.FOLLOW);
			return;
		}
		setYRot(player.getYHeadRot());
	}

	/** De día siempre busca sombra; de noche solo descansa si el dueño lleva un rato quieto. */
	private boolean wantsToRoost(ServerLevel level) {
		return level.isBrightOutside() || ownerIdleTicks > NIGHT_IDLE_TO_ROOST;
	}

	/** Un hueco de aire bajo un techo firme, cerca del dueño; de día, además, a la sombra. */
	private @Nullable BlockPos findRoost(ServerLevel level, Player player) {
		BlockPos base = player.blockPosition();
		BlockPos best = null;
		double bestDistance = Double.MAX_VALUE;
		for (int i = 0; i < 64; i++) {
			BlockPos p = base.offset(random.nextInt(15) - 7, random.nextInt(7) - 1, random.nextInt(15) - 7);
			if (!canRoostAt(level, p) || (level.isBrightOutside() && level.canSeeSky(p))) {
				continue;
			}
			double d = distanceToSqr(Vec3.atCenterOf(p));
			if (d < bestDistance) {
				bestDistance = d;
				best = p;
			}
		}
		return best;
	}

	private boolean canRoostAt(ServerLevel level, BlockPos p) {
		BlockPos above = p.above();
		return level.isEmptyBlock(p) && level.isEmptyBlock(p.below())
				&& level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN);
	}

	/** Colgado: con la cabeza (el borde superior de su caja) tocando el techo. */
	private Vec3 hangPosition(BlockPos p) {
		return new Vec3(p.getX() + 0.5, p.getY() + 1.0 - getBbHeight(), p.getZ() + 0.5);
	}

	private void wake() {
		if (isResting()) {
			setResting(false);
		}
		if (isPassenger()) {
			stopRiding();
		}
		roost = null;
	}

	private void setMode(Mode next) {
		mode = next;
		modeTicks = 0;
	}

	/**
	 * Colgado, el murciélago vanilla se acomoda solo bajo el bloque de arriba suponiendo que mide menos de un bloque;
	 * el rey es más alto, así que aquí se mantiene quieto donde se colgó (en el servidor y en el cliente).
	 */
	@Override
	public void tick() {
		if (!isResting()) {
			super.tick();
			return;
		}
		double x = getX();
		double y = getY();
		double z = getZ();
		super.tick();
		if (isResting()) {
			setPos(x, y, z);
			setDeltaMovement(Vec3.ZERO);
		}
	}

	/** Para variantes con poderes extra (el rey murciélago). */
	protected void extraAi(ServerLevel level, Player player, @Nullable LivingEntity target) {
	}

	protected float biteDamage() {
		return BITE_DAMAGE;
	}

	/** Fracción del mordisco que cura al dueño. */
	protected float lifesteal() {
		return BatCollar.LIFESTEAL;
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

	protected boolean isValidTarget(ServerLevel level, Player player, LivingEntity target) {
		return target.isAlive() && target != this && target != player && !(target instanceof AlliedBat)
				&& distanceToSqr(target) < TARGET_RANGE * TARGET_RANGE && !Allies.isAlly(level, target, player.getUUID());
	}

	private void bite(ServerLevel level, Player player, LivingEntity target) {
		biteCooldown = BITE_COOLDOWN;
		if (target.hurtServer(level, damageSources().mobAttack(this), biteDamage())) {
			player.heal(biteDamage() * lifesteal());
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
