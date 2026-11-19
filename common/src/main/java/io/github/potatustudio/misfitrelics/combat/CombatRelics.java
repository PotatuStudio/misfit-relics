package io.github.potatustudio.misfitrelics.combat;

import io.github.potatustudio.misfitrelics.relic.WornRelics;
import io.github.potatustudio.misfitrelics.registry.ModDamageTypes;
import io.github.potatustudio.misfitrelics.registry.ModEffects;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import io.github.potatustudio.misfitrelics.util.Allies;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Reliquias de combate (Trinkets):
 * <ul>
 *   <li><b>Guantelete eléctrico</b> (guantes): cada golpe cuerpo a cuerpo te carga (hasta 5; se pierde a los 3 s sin
 *       golpear) y suelta una electrocución sobre el objetivo que salta en cadena a los enemigos cercanos. Más carga,
 *       más daño.</li>
 *   <li><b>Corazón de creeper cargado</b> (collar): al recibir daño, probabilidad de que caiga un rayo sobre quien te
 *       pegó: electrocuta y aturde a los enemigos alrededor (a ti no te daña).</li>
 *   <li><b>Carcaj del bosque</b> (espalda): tus flechas enredan con enredaderas con espinas.</li>
 *   <li><b>Carcaj del vigilante</b> (espalda): tus flechas persiguen al enemigo más cercano.</li>
 *   <li><b>Concha de nautilus</b> (collar): bajo el agua nadas mucho más rápido (gracia del delfín).</li>
 * </ul>
 */
public final class CombatRelics {
	public static final int MAX_CHARGE = 5;
	public static final int CHARGE_TIMEOUT = 60;
	public static final float SHOCK_BASE = 1.0f;
	public static final float SHOCK_PER_CHARGE = 0.8f;
	public static final int CHAIN_JUMPS = 3;
	public static final double CHAIN_RANGE = 4.0;
	public static final float CHAIN_FALLOFF = 0.7f;

	public static final float HEART_CHANCE = 0.20f;
	public static final int HEART_COOLDOWN = 100;
	public static final float HEART_DAMAGE = 5.0f;
	public static final int HEART_STUN = 40;
	public static final double HEART_RADIUS = 3.5;

	public static final int ENTANGLE_TICKS = 40;
	public static final double HOMING_RANGE = 20.0;
	public static final double HOMING_TURN = 0.35;

	private record Charge(int[] level, long[] lastHit) {
	}

	private static final Map<UUID, Charge> CHARGES = new HashMap<>();
	private static final Map<UUID, Long> HEART_LAST = new HashMap<>();
	private static final Set<AbstractArrow> ENTANGLING = Collections.newSetFromMap(new WeakHashMap<>());
	/** Flechas guiadas y su estado. */
	private static final Map<AbstractArrow, Homing> HOMING = new WeakHashMap<>();

	private static final class Homing {
		@Nullable LivingEntity target;
		double speed = -1;
	}

	private CombatRelics() {
	}

	public static void init() {
		EntityEvent.LIVING_DAMAGE_POST.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (blocked || damageTaken <= 0 || !(entity.level() instanceof ServerLevel level)) {
				return;
			}
			if (source.getDirectEntity() instanceof AbstractArrow arrow && ENTANGLING.remove(arrow)) {
				entangle(level, entity);
			}
			if (source.getEntity() instanceof ServerPlayer attacker && source.getDirectEntity() == attacker && attacker != entity) {
				if (isWearing(attacker, ModItems.ELECTRIC_GAUNTLET.get())) {
					electrify(level, attacker, entity);
				}
			}
			if (entity instanceof ServerPlayer player && isWearing(player, ModItems.CHARGED_CREEPER_HEART.get())
					&& !source.is(ModDamageTypes.ELECTROCUTION) && !source.is(DamageTypeTags.IS_LIGHTNING)) {
				creeperHeart(level, player, source);
			}
		});
		EntityEvent.ADD.register((entity, level) -> {
			if (!level.isClientSide() && entity instanceof AbstractArrow arrow && arrow.tickCount == 0 && arrow.getOwner() instanceof ServerPlayer owner) {
				if (isWearing(owner, ModItems.FOREST_QUIVER.get())) {
					ENTANGLING.add(arrow);
				} else if (isWearing(owner, ModItems.WATCHER_QUIVER.get())) {
					HOMING.put(arrow, new Homing());
				}
			}
			return EventResult.pass();
		});
		TickEvent.SERVER_POST.register(CombatRelics::tick);
	}

	public static boolean isWearing(LivingEntity entity, Item item) {
		return WornRelics.isWearing(entity, item);
	}

	private static boolean isEnemyOf(ServerLevel level, LivingEntity target, Player player) {
		return target != player && target.isAlive() && !target.isSpectator() && !Allies.isAlly(level, target, player.getUUID())
				&& !(target instanceof Player other && other.isCreative());
	}

	private static DamageSource electrocution(ServerLevel level, Player player) {
		// Sin entidad directa: así no se vuelve a disparar el guantelete (que reacciona a golpes directos).
		return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ModDamageTypes.ELECTROCUTION),
				null, player);
	}

	// --- Guantelete eléctrico ----------------------------------------------------------------------------------------

	private static void electrify(ServerLevel level, ServerPlayer player, LivingEntity victim) {
		long now = level.getGameTime();
		Charge charge = CHARGES.computeIfAbsent(player.getUUID(), id -> new Charge(new int[1], new long[1]));
		if (now - charge.lastHit()[0] > CHARGE_TIMEOUT) {
			charge.level()[0] = 0;
		}
		charge.level()[0] = Math.min(MAX_CHARGE, charge.level()[0] + 1);
		charge.lastHit()[0] = now;
		int c = charge.level()[0];
		float shock = SHOCK_BASE + SHOCK_PER_CHARGE * c;

		// Electrocución sobre el objetivo (se saltan los ticks de invulnerabilidad del golpe recién dado).
		shockBurst(level, victim, c);
		if (victim.isAlive()) {
			victim.setInvulnerableTime(0);
			victim.hurtServer(level, electrocution(level, player), shock);
		}
		// Arco en cadena hacia los enemigos cercanos.
		List<LivingEntity> hit = new ArrayList<>();
		hit.add(victim);
		LivingEntity from = victim;
		float damage = shock;
		for (int i = 0; i < CHAIN_JUMPS; i++) {
			LivingEntity next = nearestEnemy(level, player, from.position(), CHAIN_RANGE, hit);
			if (next == null) {
				break;
			}
			damage *= CHAIN_FALLOFF;
			arc(level, from, next);
			shockBurst(level, next, c);
			next.setInvulnerableTime(0);
			next.hurtServer(level, electrocution(level, player), damage);
			hit.add(next);
			from = next;
		}
		level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT,
				SoundSource.PLAYERS, 0.25f + 0.08f * c, 1.8f);
		// Chispas en la mano: más mientras más cargado.
		Vec3 hand = player.getEyePosition().add(player.getLookAngle().scale(0.6)).add(0, -0.4, 0);
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, hand.x, hand.y, hand.z, 2 + c * 2, 0.15, 0.15, 0.15, 0.06);
		player.sendOverlayMessage(Component.translatable("message.misfitrelics.electric_charge", "⚡".repeat(c), c, MAX_CHARGE)
				.withStyle(c == MAX_CHARGE ? ChatFormatting.AQUA : ChatFormatting.YELLOW));
	}

	private static @Nullable LivingEntity nearestEnemy(ServerLevel level, Player player, Vec3 from, double range,
			List<LivingEntity> exclude) {
		LivingEntity best = null;
		double bestDistance = range * range;
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
				new net.minecraft.world.phys.AABB(from, from).inflate(range), e -> !exclude.contains(e) && isEnemyOf(level, e, player))) {
			double d = e.position().distanceToSqr(from);
			if (d < bestDistance) {
				bestDistance = d;
				best = e;
			}
		}
		return best;
	}

	/** Cuántos ticks dura un arco: en cada uno se redibuja con otra forma (parpadea) y la corriente avanza. */
	public static final int ARC_TICKS = 6;

	/** Arco eléctrico vivo entre dos entidades (sigue sus posiciones mientras dura). */
	private record Arc(ServerLevel level, LivingEntity from, LivingEntity to, int[] age) {
	}

	private static final List<Arc> ARCS = new ArrayList<>();

	private static void arc(ServerLevel level, LivingEntity a, LivingEntity b) {
		ARCS.add(new Arc(level, a, b, new int[1]));
		drawArc(level, a.getBoundingBox().getCenter(), b.getBoundingBox().getCenter(), 0);
	}

	/**
	 * Dibuja el arco una vez: rayo quebrado (desplazamiento del punto medio) con chispas eléctricas, que duran poco y
	 * se ven brillar; como cada tick sale otra forma, el rayo parpadea. La "corriente" es una chispa brillante que
	 * recorre el arco del origen al objetivo.
	 */
	private static void drawArc(ServerLevel level, Vec3 start, Vec3 end, int age) {
		List<Vec3> points = new ArrayList<>(List.of(start, end));
		double offset = start.distanceTo(end) * 0.22;
		for (int pass = 0; pass < 3; pass++) {
			List<Vec3> refined = new ArrayList<>();
			for (int i = 0; i < points.size() - 1; i++) {
				Vec3 p = points.get(i);
				Vec3 q = points.get(i + 1);
				Vec3 mid = p.add(q).scale(0.5).add((level.getRandom().nextDouble() - 0.5) * offset,
						(level.getRandom().nextDouble() - 0.5) * offset, (level.getRandom().nextDouble() - 0.5) * offset);
				refined.add(p);
				refined.add(mid);
			}
			refined.add(points.getLast());
			points = refined;
			offset *= 0.5;
		}
		for (int i = 0; i < points.size() - 1; i++) {
			Vec3 p = points.get(i);
			Vec3 q = points.get(i + 1);
			int steps = Math.max(1, (int) (p.distanceTo(q) / 0.26));
			for (int k = 0; k < steps; k++) {
				Vec3 t = p.add(q.subtract(p).scale((double) k / steps));
				level.sendParticles(ParticleTypes.ELECTRIC_SPARK, t.x, t.y, t.z, 1, 0, 0, 0, 0);
			}
		}
		// La corriente: avanza por el arco a lo largo de su duración.
		double f = Math.min(1.0, (age + 1) / (double) ARC_TICKS);
		Vec3 head = start.add(end.subtract(start).scale(f));
		level.sendParticles(ParticleTypes.END_ROD, head.x, head.y, head.z, 1, 0.02, 0.02, 0.02, 0.0);
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, head.x, head.y, head.z, 2, 0.06, 0.06, 0.06, 0.08);
	}

	/** Descarga sobre una entidad: chispas que saltan alrededor del cuerpo. */
	private static void shockBurst(ServerLevel level, LivingEntity e, int intensity) {
		Vec3 c = e.getBoundingBox().getCenter();
		double w = e.getBbWidth() * 0.6;
		double h = e.getBbHeight() * 0.4;
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y, c.z, 5 + intensity * 2, w, h, w, 0.2);
	}

	/** Arcos vivos y chisporroteo de la mano mientras hay carga. */
	private static void tickElectricity(MinecraftServer server) {
		Iterator<Arc> it = ARCS.iterator();
		while (it.hasNext()) {
			Arc arc = it.next();
			int age = ++arc.age()[0];
			if (age >= ARC_TICKS || arc.from().isRemoved() || arc.to().isRemoved()) {
				it.remove();
				continue;
			}
			drawArc(arc.level(), arc.from().getBoundingBox().getCenter(), arc.to().getBoundingBox().getCenter(), age);
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			Charge charge = CHARGES.get(player.getUUID());
			if (charge == null || charge.level()[0] == 0) {
				continue;
			}
			ServerLevel level = player.level();
			if (level.getGameTime() - charge.lastHit()[0] > CHARGE_TIMEOUT) {
				charge.level()[0] = 0;
				continue;
			}
			// Cada pocos ticks, un arco chico salta entre dos puntos de la mano (más seguido con más carga).
			if (player.tickCount % Math.max(1, 6 - charge.level()[0]) == 0) {
				Vec3 hand = player.getEyePosition().add(player.getLookAngle().scale(0.5)).add(0, -0.45, 0);
				Vec3 a = hand.add((level.getRandom().nextDouble() - 0.5) * 0.5, (level.getRandom().nextDouble() - 0.5) * 0.5,
						(level.getRandom().nextDouble() - 0.5) * 0.5);
				Vec3 b = hand.add((level.getRandom().nextDouble() - 0.5) * 0.5, (level.getRandom().nextDouble() - 0.5) * 0.5,
						(level.getRandom().nextDouble() - 0.5) * 0.5);
				drawArc(level, a, b, ARC_TICKS - 1);
			}
		}
	}

	// --- Corazón de creeper cargado ------------------------------------------------------------------------------------

	private static void creeperHeart(ServerLevel level, ServerPlayer player, DamageSource source) {
		long now = level.getGameTime();
		if (now - HEART_LAST.getOrDefault(player.getUUID(), -HEART_COOLDOWN * 2L) < HEART_COOLDOWN
				|| player.getRandom().nextFloat() >= HEART_CHANCE) {
			return;
		}
		LivingEntity target = source.getEntity() instanceof LivingEntity attacker && isEnemyOf(level, attacker, player)
				? attacker : nearestEnemy(level, player, player.position(), 8.0, List.of());
		if (target == null) {
			return;
		}
		HEART_LAST.put(player.getUUID(), now);
		LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
		if (bolt != null) {
			// Solo visual: el daño y el aturdimiento los pone el corazón, así no quema ni daña al jugador.
			bolt.setVisualOnly(true);
			bolt.snapTo(target.getX(), target.getY(), target.getZ());
			level.addFreshEntity(bolt);
		}
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(HEART_RADIUS),
				e -> isEnemyOf(level, e, player))) {
			e.setInvulnerableTime(0);
			e.hurtServer(level, electrocution(level, player), HEART_DAMAGE);
			e.addEffect(new MobEffectInstance(ModEffects.STUNNED.asHolder(), HEART_STUN), player);
			shockBurst(level, e, 4);
			if (e != target) {
				arc(level, target, e);
			}
		}
	}

	// --- Carcajes -----------------------------------------------------------------------------------------------------

	private static void entangle(ServerLevel level, LivingEntity target) {
		target.addEffect(new MobEffectInstance(ModEffects.ENTANGLED.asHolder(), ENTANGLE_TICKS));
		level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SWEET_BERRY_BUSH_BREAK,
				SoundSource.PLAYERS, 1.0f, 0.8f);
		level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY() + 0.5, target.getZ(),
				15, 0.4, 0.5, 0.4, 0.0);
	}

	private static void tick(MinecraftServer server) {
		tickElectricity(server);
		Iterator<Map.Entry<AbstractArrow, Homing>> it = HOMING.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<AbstractArrow, Homing> entry = it.next();
			AbstractArrow arrow = entry.getKey();
			Homing homing = entry.getValue();
			Vec3 v = arrow.getDeltaMovement();
			double speed = v.length();
			if (homing.speed < 0) {
				homing.speed = speed;
			}
			// Clavada o detenida: se deja de guiar. Bajo el agua el agua la frena, así que ahí se le devuelve la velocidad.
			if (arrow.isRemoved() || speed < 0.05 || !(arrow.level() instanceof ServerLevel level)
					|| !(arrow.getOwner() instanceof Player owner)) {
				it.remove();
				continue;
			}
			double travel = arrow.isInWater() ? homing.speed : speed;
			Vec3 forward = v.normalize().scale(travel);
			if (homing.target == null || !homing.target.isAlive()) {
				homing.target = findHomingTarget(level, owner, arrow, v);
			}
			if (homing.target == null) {
				if (arrow.isInWater()) {
					arrow.setDeltaMovement(forward);
					arrow.needsSync = true;
				}
				continue;
			}
			Vec3 desired = homing.target.getBoundingBox().getCenter().subtract(arrow.position()).normalize().scale(travel);
			Vec3 steered = forward.add(desired.subtract(forward).scale(HOMING_TURN)).normalize().scale(travel);
			arrow.setDeltaMovement(steered);
			arrow.needsSync = true;
			if (arrow.tickCount % 2 == 0) {
				level.sendParticles(arrow.isInWater() ? ParticleTypes.BUBBLE : ParticleTypes.END_ROD,
						arrow.getX(), arrow.getY(), arrow.getZ(), 1, 0, 0, 0, 0);
			}
		}
		// Concha de nautilus: bajo el agua, la gracia del delfín (nado mucho más rápido) mientras la lleve.
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (player.isInWater() && isWearing(player, ModItems.NAUTILUS_SHELL.get())) {
				player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 0, true, false, true));
			}
		}
	}

	private static @Nullable LivingEntity findHomingTarget(ServerLevel level, Player owner, AbstractArrow arrow, Vec3 v) {
		Vec3 dir = v.normalize();
		LivingEntity best = null;
		double bestScore = Double.MAX_VALUE;
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, arrow.getBoundingBox().inflate(HOMING_RANGE),
				e -> isEnemyOf(level, e, owner) && (e instanceof Enemy || e == owner.getLastHurtMob()))) {
			Vec3 to = e.getBoundingBox().getCenter().subtract(arrow.position());
			double dist = to.length();
			if (dist > HOMING_RANGE || to.normalize().dot(dir) < 0.3 || !e.hasLineOfSight(arrow)) {
				continue;
			}
			double score = dist * (2.0 - to.normalize().dot(dir));
			if (score < bestScore) {
				bestScore = score;
				best = e;
			}
		}
		return best;
	}

	/** Para el tooltip: carga actual del jugador (0 si no tiene). */
	public static int chargeOf(Entity entity) {
		Charge c = CHARGES.get(entity.getUUID());
		return c == null ? 0 : c.level()[0];
	}
}
