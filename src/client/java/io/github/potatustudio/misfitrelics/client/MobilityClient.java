package io.github.potatustudio.misfitrelics.client;

import com.mojang.blaze3d.platform.InputConstants;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import io.github.potatustudio.misfitrelics.mobility.Mobility;
import io.github.potatustudio.misfitrelics.network.BreezeDashPayload;
import io.github.potatustudio.misfitrelics.network.RamPayload;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.Vec3;

/**
 * Lado del cliente de las reliquias de movilidad: en Minecraft el jugador mueve a su propio personaje, así que
 * sostenerse en la pared, los saltos de pared y la ráfaga del breeze se aplican aquí (el servidor perdona la caída y
 * hace los efectos).
 */
public final class MobilityClient {
	public static final String RAM_KEY = "key.misfitrelics.ram";
	private static KeyMapping ram;
	private static boolean jumpWasDown;
	private static int wallJumpsLeft = Mobility.WALL_JUMPS;
	private static boolean breezeReady = true;
	/** Dash de la embestida en curso: dirección fija y ticks que le quedan. */
	private static Vec3 ramDir = Vec3.ZERO;
	private static int ramTicksLeft;
	/** Por sobre esta velocidad está en pleno salto de pared: no se frena hasta que vuelva a bajar. */
	private static final double WALL_JUMP_RISE_THRESHOLD = 0.25;

	private MobilityClient() {
	}

	public static void init(KeyMapping.Category category) {
		ram = KeyMappingHelper.registerKeyMapping(new KeyMapping(RAM_KEY, InputConstants.Type.KEYBOARD,
				InputConstants.KEY_R, category));
		ClientTickEvents.END_CLIENT_TICK.register(MobilityClient::tick);
	}

	/**
	 * Embestida: el impulso se aplica aquí mismo (el jugador mueve a su personaje, así responde al instante) y el
	 * servidor hace los golpes, la invulnerabilidad y la recarga.
	 */
	private static void ram(LocalPlayer player) {
		var horns = TrinketsApi.getAttachment(player).findFirst(ModItems.RAM_HORNS).map(TrinketSlotAccess::get);
		if (horns.isEmpty()) {
			player.sendOverlayMessage(Component.translatable("message.misfitrelics.ram_no_horns"));
			return;
		}
		if (player.getCooldowns().isOnCooldown(horns.get())) {
			return;
		}
		Vec3 look = player.getLookAngle();
		Vec3 dir = new Vec3(look.x, 0, look.z);
		if (dir.lengthSqr() < 1.0E-4 || !ClientPlayNetworking.canSend(RamPayload.TYPE)) {
			return;
		}
		ramDir = dir.normalize();
		ramTicksLeft = Mobility.RAM_DASH_TICKS;
		ClientPlayNetworking.send(RamPayload.INSTANCE);
	}

	private static void tick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null) {
			return;
		}
		while (ram.consumeClick()) {
			ram(player);
		}
		if (ramTicksLeft > 0) {
			// Dash en línea recta a velocidad constante, sin rozamiento ni caída mientras dura.
			ramTicksLeft--;
			player.setDeltaMovement(ramDir.x * Mobility.RAM_SPEED, 0.0, ramDir.z * Mobility.RAM_SPEED);
			if (ramTicksLeft == 0) {
				player.setDeltaMovement(ramDir.x * 0.3, 0.0, ramDir.z * 0.3);
			}
		}
		boolean jumpDown = client.options.keyJump.isDown();
		boolean jumpPressed = jumpDown && !jumpWasDown;
		jumpWasDown = jumpDown;
		if (player.onGround() || player.isInWater()) {
			wallJumpsLeft = Mobility.WALL_JUMPS;
			breezeReady = true;
		}
		if (player.getAbilities().flying) {
			return;
		}
		if (player.getFluidHeight(FluidTags.LAVA) > 0.5 && Mobility.walksOnLava(player)) {
			// Hundido en la lava con las botas de magma (más de medio bloque): flota hacia la superficie, como el
			// strider. Parado en la superficie no empuja, para no dar brincos.
			Vec3 v = player.getDeltaMovement();
			player.setDeltaMovement(v.x, Math.max(v.y, 0.12), v.z);
		}
		boolean clinging = Mobility.isClinging(player);
		if (clinging) {
			Vec3 v = player.getDeltaMovement();
			if (jumpPressed && wallJumpsLeft > 0) {
				// Salto de pared: subes y te vuelves a pegar más arriba.
				wallJumpsLeft--;
				player.setDeltaMovement(v.x, Mobility.WALL_JUMP_SPEED, v.z);
			} else if (v.y < WALL_JUMP_RISE_THRESHOLD) {
				// Pegado: sin velocidad vertical. (El jugador se mueve con esta velocidad antes de que se le aplique la
				// gravedad, así que cualquier valor positivo lo haría subir.) Anula también la subida de "escalera" de los
				// guantes de la viuda: con Shift te quedas quieto.
				player.setDeltaMovement(v.x * 0.5, 0.0, v.z * 0.5);
			}
			return;
		}
		if (jumpPressed && !player.onGround() && player.isShiftKeyDown() && breezeReady
				&& Mobility.isWearing(player, ModItems.BREEZE_BOTTLE) && ClientPlayNetworking.canSend(BreezeDashPayload.TYPE)) {
			breezeReady = false;
			Vec3 look = player.getLookAngle();
			player.setDeltaMovement(look.x * Mobility.BREEZE_DASH_SPEED, Math.max(look.y * Mobility.BREEZE_DASH_SPEED, 0.35),
					look.z * Mobility.BREEZE_DASH_SPEED);
			player.resetFallDistance();
			ClientPlayNetworking.send(BreezeDashPayload.INSTANCE);
		}
	}
}
