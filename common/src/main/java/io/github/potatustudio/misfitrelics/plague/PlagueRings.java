package io.github.potatustudio.misfitrelics.plague;

import io.github.potatustudio.misfitrelics.relic.WornRelics;
import io.github.potatustudio.misfitrelics.registry.ModAttachments;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import io.github.potatustudio.misfitrelics.util.Allies;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.TickEvent;
import io.github.potatustudio.misfitrelics.mixin.MobAccessor;
import io.github.potatustudio.misfitrelics.relic.EntityData;
import io.github.potatustudio.misfitrelics.relic.ServerState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

/**
 * El par de anillos de los bichos (espacios de anillo de Trinkets, uno en cada mano):
 * <ul>
 *   <li><b>Anillo de la plaga:</b> al matar un enemigo, probabilidad de que salgan silverfish o endermites (al azar)
 *       aliados. Pelean contra los enemigos cercanos unos segundos, nunca contra su dueño ni sus aliados, luego
 *       vuelven a su dueño y al rato se deshacen.</li>
 *   <li><b>Anillo del festín:</b> al tocar un silverfish o un endermite (aliado o salvaje) lo absorbes y te cura.</li>
 * </ul>
 * Juntos cierran un ciclo de sustento: matas, salen bichos, pelean y vuelven a ti para que te los comas.
 */
public final class PlagueRings {
	/** Probabilidad de que un enemigo muerto suelte bichos (25 %). */
	public static final float SPAWN_CHANCE = 0.25f;
	public static final int MAX_BUGS = 3;
	/** Ticks que pelean antes de volver al dueño (8 s) y que viven en total (20 s). */
	public static final int FIGHT_TICKS = 160;
	public static final int LIFETIME_TICKS = 400;
	public static final double SEARCH_RADIUS = 10.0;
	/** Vida que cura cada bicho absorbido (2 corazones). */
	public static final float HEAL = 4.0f;

	/** Bichos aliados cargados en el mundo. */
	private static final Set<Mob> BUGS = Collections.newSetFromMap(new IdentityHashMap<>());

	private PlagueRings() {
	}

	public static void init() {
		EntityEvent.LIVING_DEATH.register((victim, source) -> {
			onDeath(victim, source);
			return EventResult.pass();
		});
		EntityEvent.LIVING_HURT.register((target, source, amount) ->
				allowDamage(target, source, amount) ? EventResult.pass() : EventResult.interruptFalse());
		EntityEvent.ADD.register((entity, level) -> {
			if (!level.isClientSide() && entity instanceof Mob mob && EntityData.has(mob, ModAttachments.PLAGUE_OWNER)) {
				tame(mob);
				BUGS.add(mob);
			}
			return EventResult.pass();
		});
		TickEvent.SERVER_POST.register(PlagueRings::tick);
		ServerState.clearOnStop(BUGS);
	}

	/**
	 * Un bicho aliado no tiene objetivos propios (los decide el anillo cada tick) y el silverfish no se mete en la
	 * piedra: si no, al terminar se esconde y después sale un silverfish salvaje.
	 */
	private static void tame(Mob bug) {
		MobAccessor goals = (MobAccessor) bug;
		goals.misfitrelics$targetSelector().removeAllGoals(goal -> true);
		goals.misfitrelics$goalSelector().removeAllGoals(goal -> goal.getClass().getSimpleName().contains("MergeWithStone"));
	}

	/** Ticks desde que nació el bicho (guardado con el mundo: tickCount vuelve a 0 al recargar). */
	private static long age(Mob bug, ServerLevel level) {
		Long born = EntityData.get(bug, ModAttachments.PLAGUE_BORN);
		if (born == null || born > level.getGameTime()) {
			born = level.getGameTime() - bug.tickCount;
			EntityData.set(bug, ModAttachments.PLAGUE_BORN, born);
		}
		return level.getGameTime() - born;
	}

	private static boolean isBug(Entity entity) {
		return entity instanceof Silverfish || entity instanceof Endermite;
	}

	private static @Nullable UUID ownerOf(Entity entity) {
		return EntityData.get(entity, ModAttachments.PLAGUE_OWNER);
	}

	// --- Anillo de la plaga ---------------------------------------------------------------------------------

	private static void onDeath(LivingEntity victim, DamageSource source) {
		if (!(victim instanceof Enemy) || isBug(victim) || !(victim.level() instanceof ServerLevel level)
				|| !(source.getEntity() instanceof ServerPlayer player) || !WornRelics.isWearing(player, ModItems.PLAGUE_RING.get())) {
			return;
		}
		RandomSource random = player.getRandom();
		if (random.nextFloat() >= SPAWN_CHANCE) {
			return;
		}
		int count = 1 + random.nextInt(MAX_BUGS);
		for (int i = 0; i < count; i++) {
			Mob bug = random.nextBoolean()
					? EntityTypes.SILVERFISH.create(level, EntitySpawnReason.MOB_SUMMONED)
					: EntityTypes.ENDERMITE.create(level, EntitySpawnReason.MOB_SUMMONED);
			if (bug == null) {
				continue;
			}
			bug.snapTo(victim.getX() + random.nextGaussian() * 0.3, victim.getY(), victim.getZ() + random.nextGaussian() * 0.3,
					random.nextFloat() * 360f, 0f);
			EntityData.set(bug, ModAttachments.PLAGUE_OWNER, player.getUUID());
			EntityData.set(bug, ModAttachments.PLAGUE_BORN, level.getGameTime());
			level.addFreshEntity(bug);
			BUGS.add(bug);
		}
		level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.SILVERFISH_AMBIENT,
				SoundSource.PLAYERS, 1.0f, 0.8f);
		level.sendParticles(ParticleTypes.SQUID_INK, victim.getX(), victim.getY() + 0.3, victim.getZ(),
				10, 0.3, 0.2, 0.3, 0.02);
	}

	/** Los bichos aliados no dañan a su dueño ni a sus aliados, ni entre ellos. */
	private static boolean allowDamage(LivingEntity target, DamageSource source, float amount) {
		Entity attacker = source.getEntity();
		UUID owner = attacker == null ? null : ownerOf(attacker);
		if (owner == null || !(target.level() instanceof ServerLevel level)) {
			return true;
		}
		return !(target.getUUID().equals(owner) || ownerOf(target) != null || Allies.isAlly(level, target, owner));
	}

	private static void tick(MinecraftServer server) {
		Iterator<Mob> it = BUGS.iterator();
		while (it.hasNext()) {
			Mob bug = it.next();
			if (bug.isRemoved() || !bug.isAlive() || !(bug.level() instanceof ServerLevel level)) {
				it.remove();
				continue;
			}
			long age = age(bug, level);
			if (age >= LIFETIME_TICKS) {
				level.sendParticles(ParticleTypes.POOF, bug.getX(), bug.getY() + 0.2, bug.getZ(), 6, 0.2, 0.1, 0.2, 0.02);
				bug.discard();
				it.remove();
				continue;
			}
			UUID ownerId = ownerOf(bug);
			Player owner = ownerId == null ? null : level.getPlayerByUUID(ownerId);
			LivingEntity target = bug.getTarget();
			if (age < FIGHT_TICKS) {
				if (target == null || !target.isAlive() || !isValidTarget(level, target, ownerId)) {
					target = findTarget(level, bug, ownerId);
					bug.setTarget(target);
				}
				if (target == null) {
					follow(bug, owner);
				}
			} else {
				bug.setTarget(null);
				follow(bug, owner);
			}
			if (bug.tickCount % 10 == 0) {
				level.sendParticles(ParticleTypes.COMPOSTER, bug.getX(), bug.getY() + 0.3, bug.getZ(), 1, 0.1, 0.05, 0.1, 0.0);
			}
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (WornRelics.isWearing(player, ModItems.FEAST_RING.get())) {
				feast(player);
			}
		}
	}

	private static boolean isValidTarget(ServerLevel level, LivingEntity target, @Nullable UUID ownerId) {
		if (ownerOf(target) != null || !(target instanceof Enemy)) {
			return false;
		}
		return ownerId == null || (!target.getUUID().equals(ownerId) && !Allies.isAlly(level, target, ownerId));
	}

	private static @Nullable LivingEntity findTarget(ServerLevel level, Mob bug, @Nullable UUID ownerId) {
		return level.getEntitiesOfClass(LivingEntity.class, bug.getBoundingBox().inflate(SEARCH_RADIUS),
						t -> t != bug && t.isAlive() && isValidTarget(level, t, ownerId))
				.stream().min(Comparator.comparingDouble(bug::distanceToSqr)).orElse(null);
	}

	private static void follow(Mob bug, @Nullable Player owner) {
		if (owner != null && owner.level() == bug.level() && bug.distanceToSqr(owner) > 2.0) {
			bug.getNavigation().moveTo(owner, 1.2);
		}
	}

	// --- Anillo del festín ----------------------------------------------------------------------------------

	private static void feast(ServerPlayer player) {
		ServerLevel level = player.level();
		for (Mob bug : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(0.4),
				m -> m.isAlive() && isBug(m))) {
			bug.discard();
			BUGS.remove(bug);
			player.heal(HEAL);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EAT.value(),
					SoundSource.PLAYERS, 0.8f, 1.2f);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP,
					SoundSource.PLAYERS, 0.5f, 1.0f);
			level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.8, player.getZ(), 1, 0.2, 0.1, 0.2, 0.0);
			level.sendParticles(ParticleTypes.SQUID_INK, bug.getX(), bug.getY() + 0.2, bug.getZ(), 6, 0.2, 0.1, 0.2, 0.02);
		}
	}
}
