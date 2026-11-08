package io.github.potatustudio.misfitrelics.combat;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
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
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
 *   <li><b>Concha de nautilus</b> (collar): bajo el agua respiras más y nadas más rápido (atributos en el ítem) y tus
 *       golpes empujan con corriente.</li>
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
	public static final double NAUTILUS_PUSH = 1.1;

	private record Charge(int[] level, long[] lastHit) {
	}

	private static final Map<UUID, Charge> CHARGES = new HashMap<>();
	private static final Map<UUID, Long> HEART_LAST = new HashMap<>();
	private static final Set<AbstractArrow> ENTANGLING = Collections.newSetFromMap(new WeakHashMap<>());
	private static final Map<AbstractArrow, LivingEntity> HOMING = new WeakHashMap<>();

	private CombatRelics() {
	}

	public static void init() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (blocked || damageTaken <= 0 || !(entity.level() instanceof ServerLevel level)) {
				return;
			}
			if (source.getDirectEntity() instanceof AbstractArrow arrow && ENTANGLING.remove(arrow)) {
				entangle(level, entity);
			}
			if (source.getEntity() instanceof ServerPlayer attacker && source.getDirectEntity() == attacker && attacker != entity) {
				if (isWearing(attacker, ModItems.ELECTRIC_GAUNTLET)) {
					electrify(level, attacker, entity);
				}
				if (attacker.isInWater() && isWearing(attacker, ModItems.NAUTILUS_SHELL)) {
					currentPush(level, attacker, entity);
				}
			}
			if (entity instanceof ServerPlayer player && isWearing(player, ModItems.CHARGED_CREEPER_HEART)
					&& !source.is(ModDamageTypes.ELECTROCUTION) && !source.is(DamageTypeTags.IS_LIGHTNING)) {
				creeperHeart(level, player, source);
			}
		});
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof AbstractArrow arrow && arrow.tickCount == 0 && arrow.getOwner() instanceof ServerPlayer owner) {
				if (isWearing(owner, ModItems.FOREST_QUIVER)) {
					ENTANGLING.add(arrow);
				} else if (isWearing(owner, ModItems.WATCHER_QUIVER)) {
					HOMING.put(arrow, null);
				}
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(CombatRelics::tick);
	}

	public static boolean isWearing(LivingEntity entity, Item item) {
		return TrinketsApi.getAttachment(entity).findFirst(item).map(TrinketSlotAccess::get).isPresent();
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
			next.setInvulnerableTime(0);
			next.hurtServer(level, electrocution(level, player), damage);
			hit.add(next);
			from = next;
		}
		level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT,
				SoundSource.PLAYERS, 0.25f + 0.08f * c, 1.8f);
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(),
				2 + c * 2, 0.4, 0.4, 0.4, 0.05);
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

	/** Arco eléctrico entre dos entidades: chispas en zigzag a lo largo de la línea. */
	private static void arc(ServerLevel level, LivingEntity a, LivingEntity b) {
		Vec3 start = a.getBoundingBox().getCenter();
		Vec3 end = b.getBoundingBox().getCenter();
		Vec3 delta = end.subtract(start);
		int steps = Math.max(4, (int) (delta.length() * 4));
		for (int i = 0; i <= steps; i++) {
			Vec3 p = start.add(delta.scale((double) i / steps));
			double jitter = (i == 0 || i == steps) ? 0 : 0.15;
			level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x + (level.getRandom().nextDouble() - 0.5) * jitter,
					p.y + (level.getRandom().nextDouble() - 0.5) * jitter, p.z + (level.getRandom().nextDouble() - 0.5) * jitter,
					1, 0, 0, 0, 0);
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
			e.addEffect(new MobEffectInstance(ModEffects.STUNNED, HEART_STUN), player);
			level.sendParticles(ParticleTypes.ELECTRIC_SPARK, e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ(),
					10, 0.3, 0.4, 0.3, 0.1);
		}
	}

	// --- Carcajes -----------------------------------------------------------------------------------------------------

	private static void entangle(ServerLevel level, LivingEntity target) {
		target.addEffect(new MobEffectInstance(ModEffects.ENTANGLED, ENTANGLE_TICKS));
		level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SWEET_BERRY_BUSH_BREAK,
				SoundSource.PLAYERS, 1.0f, 0.8f);
		level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY() + 0.5, target.getZ(),
				15, 0.4, 0.5, 0.4, 0.0);
	}

	private static void tick(MinecraftServer server) {
		Iterator<Map.Entry<AbstractArrow, LivingEntity>> it = HOMING.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<AbstractArrow, LivingEntity> entry = it.next();
			AbstractArrow arrow = entry.getKey();
			Vec3 v = arrow.getDeltaMovement();
			double speed = v.length();
			if (arrow.isRemoved() || speed < 0.4 || !(arrow.level() instanceof ServerLevel level)
					|| !(arrow.getOwner() instanceof Player owner)) {
				it.remove();
				continue;
			}
			LivingEntity target = entry.getValue();
			if (target == null || !target.isAlive()) {
				target = findHomingTarget(level, owner, arrow, v);
				entry.setValue(target);
			}
			if (target == null) {
				continue;
			}
			Vec3 desired = target.getBoundingBox().getCenter().subtract(arrow.position()).normalize().scale(speed);
			Vec3 steered = v.add(desired.subtract(v).scale(HOMING_TURN)).normalize().scale(speed);
			arrow.setDeltaMovement(steered);
			arrow.needsSync = true;
			if (arrow.tickCount % 2 == 0) {
				level.sendParticles(ParticleTypes.END_ROD, arrow.getX(), arrow.getY(), arrow.getZ(), 1, 0, 0, 0, 0);
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

	// --- Concha de nautilus ---------------------------------------------------------------------------------------------

	private static void currentPush(ServerLevel level, ServerPlayer player, LivingEntity target) {
		Vec3 away = target.position().subtract(player.position()).multiply(1, 0, 1);
		if (away.lengthSqr() < 1.0E-4) {
			return;
		}
		away = away.normalize();
		target.push(away.x * NAUTILUS_PUSH, 0.2, away.z * NAUTILUS_PUSH);
		target.needsSync = true;
		level.sendParticles(ParticleTypes.BUBBLE, target.getX(), target.getY() + 0.5, target.getZ(), 15, 0.4, 0.4, 0.4, 0.1);
		level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE,
				SoundSource.PLAYERS, 0.8f, 1.2f);
	}

	/** Para el tooltip: carga actual del jugador (0 si no tiene). */
	public static int chargeOf(Entity entity) {
		Charge c = CHARGES.get(entity.getUUID());
		return c == null ? 0 : c.level()[0];
	}
}
