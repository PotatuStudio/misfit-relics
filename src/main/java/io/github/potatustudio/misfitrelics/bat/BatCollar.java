package io.github.potatustudio.misfitrelics.bat;

import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/** Poderes del collar del murciélago: ecolocación (tecla) y robo de vida (golpes cuerpo a cuerpo). */
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

	private BatCollar() {
	}

	public static Optional<ItemStack> findWorn(LivingEntity entity) {
		return TrinketsApi.getAttachment(entity).findFirst(ModItems.BAT_COLLAR).map(TrinketSlotAccess::get);
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
