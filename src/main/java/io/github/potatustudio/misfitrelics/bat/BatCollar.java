package io.github.potatustudio.misfitrelics.bat;

import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import io.github.potatustudio.misfitrelics.registry.ModComponents;
import io.github.potatustudio.misfitrelics.registry.ModEntityTypes;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/** Poderes del collar del murciélago: ecolocación (tecla), robo de vida (golpes cuerpo a cuerpo) y murciélago aliado. */
public final class BatCollar {
	/** Clave de traducción de la tecla de ecolocación (se registra en el cliente). */
	public static final String ECHOLOCATE_KEY = "key.misfitrelics.echolocate";
	public static final double ECHOLOCATION_RADIUS = 32.0;
	/** Duración del contorno brillante (6 s). */
	public static final int ECHOLOCATION_GLOW = 120;
	/** Enfriamiento de la ecolocación (10 s). */
	public static final int ECHOLOCATION_COOLDOWN = 200;
	/** Fracción del daño cuerpo a cuerpo que se recupera como vida (10 %). */
	public static final float LIFESTEAL = 0.10f;
	/** Tiempo que tarda en revivir el murciélago aliado si muere (60 s). */
	public static final int REVIVE_TICKS = 1200;
	/** El rey murciélago revive antes (45 s). */
	public static final int KING_REVIVE_TICKS = 900;

	/** Murciélago aliado de cada jugador con collar. */
	private static final Map<UUID, AlliedBat> BATS = new HashMap<>();

	private BatCollar() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(BatCollar::tick);
	}

	/** Mantiene un murciélago aliado por cada jugador con collar, salvo mientras el collar está "reviviendo". */
	private static void tick(MinecraftServer server) {
		BATS.values().removeIf(Entity::isRemoved);
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			Optional<TrinketSlotAccess> access = findWornAccess(player);
			if (access.isEmpty() || !player.isAlive() || player.isSpectator()) {
				continue;
			}
			ItemStack collar = access.get().get();
			Integer revive = collar.get(ModComponents.BAT_REVIVE);
			if (revive != null) {
				// Se actualiza una vez por segundo para no sincronizar el ítem en cada tick.
				if (server.getTickCount() % 20 == 0) {
					int left = revive - 20;
					if (left <= 0) {
						collar.remove(ModComponents.BAT_REVIVE);
					} else {
						collar.set(ModComponents.BAT_REVIVE, left);
					}
					access.get().inventory().setChanged();
				}
				continue;
			}
			boolean king = isKing(collar);
			AlliedBat current = BATS.get(player.getUUID());
			if (current != null && (current instanceof KingBat) != king) {
				// Cambió de collar: se va el murciélago anterior y llega el que corresponde.
				current.discard();
				BATS.remove(player.getUUID());
				current = null;
			}
			if (current == null) {
				summon(player, king);
			}
		}
	}

	private static void summon(ServerPlayer player, boolean king) {
		ServerLevel level = player.level();
		AlliedBat bat = king
				? ModEntityTypes.KING_BAT.create(level, EntitySpawnReason.MOB_SUMMONED)
				: ModEntityTypes.ALLIED_BAT.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (bat == null) {
			return;
		}
		bat.setOwner(player);
		bat.snapTo(player.getX(), player.getEyeY() + 0.6, player.getZ(), player.getYRot(), 0f);
		level.addFreshEntity(bat);
		BATS.put(player.getUUID(), bat);
		level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, bat.getX(), bat.getY(), bat.getZ(), 8, 0.2, 0.2, 0.2, 0.02);
	}

	/** El murciélago murió: el collar queda reviviendo y se ve con una barra que se va llenando. */
	public static void onAlliedBatDied(Player player) {
		BATS.remove(player.getUUID());
		findWornAccess(player).ifPresent(access -> {
			int ticks = reviveTicks(access.get());
			access.get().set(ModComponents.BAT_REVIVE, ticks);
			access.inventory().setChanged();
			player.sendOverlayMessage(Component.translatable("message.misfitrelics.allied_bat_died", ticks / 20));
		});
	}

	public static Optional<ItemStack> findWorn(LivingEntity entity) {
		return findWornAccess(entity).map(TrinketSlotAccess::get);
	}

	/** El collar del murciélago o el del rey murciélago, el que lleve puesto. */
	public static Optional<TrinketSlotAccess> findWornAccess(LivingEntity entity) {
		Optional<TrinketSlotAccess> king = TrinketsApi.getAttachment(entity).findFirst(ModItems.KING_BAT_COLLAR);
		return king.isPresent() ? king : TrinketsApi.getAttachment(entity).findFirst(ModItems.BAT_COLLAR);
	}

	public static boolean isKing(ItemStack collar) {
		return collar.is(ModItems.KING_BAT_COLLAR);
	}

	public static int reviveTicks(ItemStack collar) {
		return isKing(collar) ? KING_REVIVE_TICKS : REVIVE_TICKS;
	}

	/** Pulso de ecolocación: marca con contorno a las criaturas cercanas, incluso a través de paredes. */
	public static void echolocate(ServerPlayer player) {
		Optional<ItemStack> collar = findWorn(player);
		if (collar.isEmpty()) {
			return;
		}
		if (player.getCooldowns().isOnCooldown(collar.get())) {
			player.sendOverlayMessage(Component.translatable("message.misfitrelics.echolocation_cooldown"));
			return;
		}
		ServerLevel level = player.level();
		AABB area = player.getBoundingBox().inflate(ECHOLOCATION_RADIUS);
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
				t -> t != player && t.isAlive() && t.distanceToSqr(player) <= ECHOLOCATION_RADIUS * ECHOLOCATION_RADIUS)) {
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, ECHOLOCATION_GLOW, 0, false, false), player);
		}
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BAT_AMBIENT,
				SoundSource.PLAYERS, 1.0f, 1.6f);
		level.sendParticles(ParticleTypes.SONIC_BOOM, player.getX(), player.getEyeY(), player.getZ(), 1, 0, 0, 0, 0);
		player.getCooldowns().addCooldown(collar.get(), ECHOLOCATION_COOLDOWN);
	}

	/** Robo de vida: solo golpes directos (no proyectiles, nubes ni efectos). */
	public static void onDamageDealt(LivingEntity victim, DamageSource source, float damageTaken, boolean blocked) {
		if (blocked || damageTaken <= 0 || !(source.getEntity() instanceof Player attacker)
				|| source.getDirectEntity() != attacker || attacker == victim) {
			return;
		}
		if (findWorn(attacker).isPresent()) {
			attacker.heal(damageTaken * LIFESTEAL);
			if (attacker.level() instanceof ServerLevel level) {
				level.sendParticles(ParticleTypes.HEART, attacker.getX(), attacker.getEyeY() + 0.5, attacker.getZ(),
						1, 0.2, 0.1, 0.2, 0.0);
			}
		}
	}
}
