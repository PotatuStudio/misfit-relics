package io.github.potatustudio.misfitrelics.plague;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import io.github.potatustudio.misfitrelics.registry.ModAttachments;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import io.github.potatustudio.misfitrelics.util.Allies;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
	public static final int MAX_BUGS = 2;
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
		ServerLivingEntityEvents.AFTER_DEATH.register(PlagueRings::onDeath);
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(PlagueRings::allowDamage);
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof Mob mob && mob.hasAttached(ModAttachments.PLAGUE_OWNER)) {
				BUGS.add(mob);
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(PlagueRings::tick);
	}

	public static boolean isWearing(LivingEntity entity, Item ring) {
		return TrinketsApi.getAttachment(entity).findFirst(ring).map(TrinketSlotAccess::get).isPresent();
	}

	private static boolean isBug(Entity entity) {
		return entity instanceof Silverfish || entity instanceof Endermite;
	}

	private static @Nullable UUID ownerOf(Entity entity) {
		return entity.getAttached(ModAttachments.PLAGUE_OWNER);
	}

	// --- Anillo de la plaga ---------------------------------------------------------------------------------

	private static void onDeath(LivingEntity victim, DamageSource source) {
		if (!(victim instanceof Enemy) || isBug(victim) || !(victim.level() instanceof ServerLevel level)
				|| !(source.getEntity() instanceof ServerPlayer player) || !isWearing(player, ModItems.PLAGUE_RING)) {
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
			bug.setAttached(ModAttachments.PLAGUE_OWNER, player.getUUID());
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
			if (bug.tickCount >= LIFETIME_TICKS) {
				level.sendParticles(ParticleTypes.POOF, bug.getX(), bug.getY() + 0.2, bug.getZ(), 6, 0.2, 0.1, 0.2, 0.02);
				bug.discard();
				it.remove();
				continue;
			}
			UUID ownerId = ownerOf(bug);
			Player owner = ownerId == null ? null : level.getPlayerByUUID(ownerId);
			LivingEntity target = bug.getTarget();
			if (bug.tickCount < FIGHT_TICKS) {
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
			if (isWearing(player, ModItems.FEAST_RING)) {
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
