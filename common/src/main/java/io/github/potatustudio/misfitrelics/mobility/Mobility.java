package io.github.potatustudio.misfitrelics.mobility;

import io.github.potatustudio.misfitrelics.relic.WornRelics;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.registry.ModEffects;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import io.github.potatustudio.misfitrelics.util.Allies;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Reliquias de movilidad (Trinkets):
 * <ul>
 *   <li><b>Guantes de seda</b> (guantes): con Shift te quedas pegado a una pared; saltando desde ahí subes y te
 *       vuelves a pegar (saltos de pared limitados hasta tocar el suelo).</li>
 *   <li><b>Guantes de la viuda</b> (guantes): además caminas por las paredes como por una escalera y te cuelgas de
 *       los techos manteniendo el salto.</li>
 *   <li><b>Breeze en una botella</b> (cinturón): en el aire, Shift + salto suelta una ráfaga que te impulsa hacia donde
 *       miras y empuja a los mobs cercanos. Una ráfaga por salto.</li>
 *   <li><b>Botas de magma</b> (zapatos): caminas sobre la lava sin quemarte (Shift para hundirte) y en el Nether vas
 *       más rápido.</li>
 *   <li><b>Cuernos del carnero</b> (sombrero): con una tecla embistes hacia adelante; lo que golpeas recibe daño,
 *       sale volando y queda aturdido.</li>
 * </ul>
 * El movimiento lo decide el cliente (en Minecraft el jugador mueve a su propio personaje); aquí está lo común y lo
 * que corre en el servidor: caídas, daño, efectos y la embestida.
 */
public final class Mobility {
	/** Saltos de pared antes de tener que tocar el suelo. */
	public static final int WALL_JUMPS = 6;
	public static final double WALL_JUMP_SPEED = 0.62;
	public static final double BREEZE_DASH_SPEED = 1.15;
	public static final double BREEZE_PUSH_RADIUS = 3.0;
	/** Gracia tras salir de la lava con las botas de magma, sin quemarse (2 s). */
	public static final int MAGMA_GRACE_TICKS = 40;
	public static final double MAGMA_NETHER_SPEED = 0.15;
	/** Superficie de la lava para quien lleva las botas de magma: la misma media altura que usa el strider. */
	public static final VoxelShape LAVA_SURFACE = Block.column(16.0, 0.0, 8.0);
	public static final float RAM_DAMAGE = 10.0f;
	/** Velocidad horizontal con la que sale volando lo que embistes. */
	public static final double RAM_KNOCKBACK = 2.2;
	public static final int RAM_COOLDOWN = 60;
	/** Dash de la embestida: velocidad constante (bloques por tick) durante RAM_DASH_TICKS, unos 8 bloques en línea recta. */
	public static final double RAM_SPEED = 1.0;
	public static final int RAM_DASH_TICKS = 8;
	public static final int RAM_STUN = 20;
	/** Ticks que dura la carrera: mientras tanto, el jugador es invulnerable (0,6 s). */
	public static final int RAM_ACTIVE_TICKS = 12;
	private static final Identifier MAGMA_SPEED_ID = MisfitRelics.id("magma_boots_nether_speed");
	public static final Identifier RAM_COOLDOWN_GROUP = MisfitRelics.id("ram_horns");

	private record Ram(Vec3 direction, Set<UUID> hit, int[] ticksLeft) {
	}

	private static final Map<UUID, Ram> RAMS = new HashMap<>();
	private static final Map<UUID, Integer> MAGMA_GRACE = new HashMap<>();
	private static final Map<UUID, Long> LAST_DASH = new HashMap<>();
	/**
	 * Durante un salto de pared, la pared de los guantes de la viuda deja de contar como escalera (vanilla limita la
	 * subida por escalera a 0,2 por tick y cortaría el salto). UUID -> tick hasta el que dura.
	 */
	private static final Map<UUID, Long> CLIMB_SUPPRESSED = new HashMap<>();
	public static final int WALL_JUMP_NO_CLIMB_TICKS = 6;

	/** Jugadores que no reciben daño de caída hasta volver a tocar el suelo (tras sus propios dashes): UUID -> tick. */
	private static final Map<UUID, Long> SAFE_FALL = new HashMap<>();

	private Mobility() {
	}

	public static void init() {
		TickEvent.SERVER_POST.register(Mobility::tick);
		EntityEvent.LIVING_HURT.register((entity, source, amount) ->
				allowDamage(entity, source) ? EventResult.pass() : EventResult.interruptFalse());
	}

	/** Si el daño sigue (false lo anula). */
	private static boolean allowDamage(LivingEntity entity, DamageSource source) {
		if (!(entity instanceof Player player)) {
			return true;
		}
		// Embistiendo: invulnerable.
		if (RAMS.containsKey(player.getUUID())) {
			return false;
		}
		// Tus propios dashes no te hacen daño por caída.
		if (source.is(DamageTypeTags.IS_FALL) && SAFE_FALL.containsKey(player.getUUID())) {
			SAFE_FALL.remove(player.getUUID());
			return false;
		}
		return !(source.is(DamageTypeTags.IS_FIRE) && isWearing(player, ModItems.MAGMA_BOOTS.get())
				&& MAGMA_GRACE.getOrDefault(player.getUUID(), 0) > 0);
	}

	public static boolean isWearing(LivingEntity entity, Item item) {
		return WornRelics.isWearing(entity, item);
	}

	/** Lleva las botas de magma y no se está hundiendo a propósito (Shift). */
	public static boolean walksOnLava(Player player) {
		return !player.isShiftKeyDown() && isWearing(player, ModItems.MAGMA_BOOTS.get());
	}

	/** Superficie de lava que corre: a la altura de su propia superficie, como mucho la media altura de la fuente. */
	public static VoxelShape lavaSurface(float ownHeight) {
		double top = Math.max(2.0, Math.min(8.0, ownHeight * 16.0 - 1.0));
		return top >= 8.0 ? LAVA_SURFACE : Block.column(16.0, 0.0, top);
	}

	/** Con las botas de magma: inmune al fuego tocando lava y durante la gracia al salir (la gracia solo la conoce el servidor). */
	public static boolean magmaProtected(Player player) {
		return isWearing(player, ModItems.MAGMA_BOOTS.get())
				&& (player.isInLava() || MAGMA_GRACE.getOrDefault(player.getUUID(), 0) > 0);
	}

	/** Lo llama el cliente al hacer un salto de pared. */
	public static void suppressClimb(Player player) {
		CLIMB_SUPPRESSED.put(player.getUUID(), player.level().getGameTime() + WALL_JUMP_NO_CLIMB_TICKS);
	}

	public static boolean hasSilk(LivingEntity entity) {
		return isWearing(entity, ModItems.SILK_GLOVES.get()) || isWearing(entity, ModItems.WIDOW_GLOVES.get());
	}

	/** Toca una pared por algún costado (sin contar el suelo ni el techo). */
	public static boolean touchingWall(LivingEntity entity) {
		AABB bb = entity.getBoundingBox();
		// Desde los pies (sin tocar el suelo, que está justo bajo minY): así se detecta la pared hasta terminar de subir.
		AABB probe = new AABB(bb.minX - 0.08, bb.minY + 0.01, bb.minZ - 0.08, bb.maxX + 0.08, bb.maxY - 0.05, bb.maxZ + 0.08);
		return !entity.level().noCollision(entity, probe);
	}

	public static boolean touchingCeiling(LivingEntity entity) {
		AABB bb = entity.getBoundingBox();
		return !entity.level().noCollision(entity, new AABB(bb.minX + 0.05, bb.maxY, bb.minZ + 0.05,
				bb.maxX - 0.05, bb.maxY + 0.12, bb.maxZ - 0.05));
	}

	/** Pegado a la pared con los guantes (Shift contra una pared, en el aire). */
	public static boolean isClinging(Player player) {
		return !player.onGround() && player.isShiftKeyDown() && hasSilk(player) && touchingWall(player);
	}

	/** Guantes de la viuda: la pared (o el techo, manteniendo el salto) cuenta como escalera. */
	public static boolean widowClimbs(Player player, boolean jumping) {
		Long until = CLIMB_SUPPRESSED.get(player.getUUID());
		if (until != null && player.level().getGameTime() < until) {
			return false;
		}
		return isWearing(player, ModItems.WIDOW_GLOVES.get()) && !player.getAbilities().flying
				&& (touchingWall(player) || (jumping && touchingCeiling(player)));
	}

	// --- Servidor --------------------------------------------------------------------------------------------------

	private static void tick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			Long since = SAFE_FALL.get(player.getUUID());
			if (since != null && (player.onGround() || player.isInWater()) && player.level().getGameTime() - since > 5) {
				SAFE_FALL.remove(player.getUUID());
			}
			// Pegado a la pared no se acumula caída (el cliente lo sostiene; aquí se perdona la altura).
			if (isClinging(player)) {
				player.resetFallDistance();
			}
			magmaBoots(player);
		}
		tickRams(server);
	}

	private static void magmaBoots(ServerPlayer player) {
		boolean wearing = isWearing(player, ModItems.MAGMA_BOOTS.get());
		UUID id = player.getUUID();
		if (wearing && player.isInLava()) {
			MAGMA_GRACE.put(id, MAGMA_GRACE_TICKS);
			player.clearFire();
			if (player.tickCount % 10 == 0 && player.level() instanceof ServerLevel level) {
				level.sendParticles(ParticleTypes.LAVA, player.getX(), player.getY(), player.getZ(), 1, 0.2, 0.0, 0.2, 0.0);
			}
		} else if (MAGMA_GRACE.containsKey(id)) {
			int left = MAGMA_GRACE.get(id) - 1;
			if (left <= 0 || !wearing) {
				MAGMA_GRACE.remove(id);
			} else {
				MAGMA_GRACE.put(id, left);
				player.clearFire();
			}
		}
		AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed != null) {
			if (wearing && player.level().dimension() == Level.NETHER) {
				speed.addOrUpdateTransientModifier(new AttributeModifier(MAGMA_SPEED_ID, MAGMA_NETHER_SPEED,
						AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
			} else {
				speed.removeModifier(MAGMA_SPEED_ID);
			}
		}
	}

	/** El cliente avisa que usó la ráfaga: efectos, empuje a los mobs y se perdona la caída. */
	public static void onBreezeDash(ServerPlayer player) {
		if (!isWearing(player, ModItems.BREEZE_BOTTLE.get()) || player.onGround()) {
			return;
		}
		long now = player.level().getGameTime();
		if (now - LAST_DASH.getOrDefault(player.getUUID(), 0L) < 5) {
			return;
		}
		LAST_DASH.put(player.getUUID(), now);
		safeFall(player);
		ServerLevel level = player.level();
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(),
				SoundSource.PLAYERS, 0.8f, 1.2f);
		level.sendParticles(ParticleTypes.GUST, player.getX(), player.getY() + 0.5, player.getZ(), 1, 0, 0, 0, 0);
		level.sendParticles(ParticleTypes.SMALL_GUST, player.getX(), player.getY() + 0.5, player.getZ(), 8, 0.5, 0.3, 0.5, 0.05);
		for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(BREEZE_PUSH_RADIUS),
				e -> e != player && e.isAlive() && !Allies.isAlly(level, e, player.getUUID()))) {
			Vec3 away = mob.position().subtract(player.position()).normalize();
			mob.push(away.x * 0.9, 0.35, away.z * 0.9);
			mob.needsSync = true;
		}
	}

	/** Desde ahora y hasta que vuelva a tocar el suelo, el jugador no recibe daño de caída. */
	private static void safeFall(ServerPlayer player) {
		player.resetFallDistance();
		SAFE_FALL.put(player.getUUID(), player.level().getGameTime());
	}

	/** Embestida de los cuernos del carnero (tecla). */
	public static void onRam(ServerPlayer player) {
		var horns = WornRelics.find(player, ModItems.RAM_HORNS.get()).map(WornRelics.Worn::stack);
		if (horns.isEmpty() || player.getCooldowns().isOnCooldown(horns.get()) || RAMS.containsKey(player.getUUID())) {
			return;
		}
		Vec3 look = player.getLookAngle();
		Vec3 dir = new Vec3(look.x, 0, look.z);
		if (dir.lengthSqr() < 1.0E-4) {
			return;
		}
		dir = dir.normalize();
		// El dash lo hace el cliente (mueve a su propio personaje); aquí van los golpes, la invulnerabilidad y la recarga.
		safeFall(player);
		player.getCooldowns().addCooldown(RAM_COOLDOWN_GROUP, RAM_COOLDOWN);
		RAMS.put(player.getUUID(), new Ram(dir, new HashSet<>(), new int[] {RAM_ACTIVE_TICKS}));
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GOAT_PREPARE_RAM,
				SoundSource.PLAYERS, 1.0f, 1.0f);
	}

	private static void tickRams(MinecraftServer server) {
		Iterator<Map.Entry<UUID, Ram>> it = RAMS.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, Ram> entry = it.next();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
			Ram ram = entry.getValue();
			if (player == null || --ram.ticksLeft()[0] < 0) {
				it.remove();
				continue;
			}
			ServerLevel level = player.level();
			level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.2, player.getZ(), 2, 0.2, 0.05, 0.2, 0.0);
			for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(0.8),
					e -> e != player && e.isAlive() && !ram.hit().contains(e.getUUID())
							&& !Allies.isAlly(level, e, player.getUUID()))) {
				ram.hit().add(target.getUUID());
				target.hurtServer(level, player.damageSources().playerAttack(player), RAM_DAMAGE);
				// Empuje directo en la dirección de la embestida (más fuerte que el knockback normal).
				target.push(ram.direction().x * RAM_KNOCKBACK, 0.5, ram.direction().z * RAM_KNOCKBACK);
				target.needsSync = true;
				target.addEffect(new MobEffectInstance(ModEffects.STUNNED.asHolder(), RAM_STUN), player);
				level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GOAT_RAM_IMPACT,
						SoundSource.PLAYERS, 1.0f, 1.0f);
				level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6,
						target.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
			}
		}
	}
}
