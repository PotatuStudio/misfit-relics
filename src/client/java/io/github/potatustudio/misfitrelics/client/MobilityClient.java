package io.github.potatustudio.misfitrelics.client;

import com.mojang.blaze3d.platform.InputConstants;
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

	private MobilityClient() {
	}

	public static void init(KeyMapping.Category category) {
		ram = KeyMappingHelper.registerKeyMapping(new KeyMapping(RAM_KEY, InputConstants.Type.KEYBOARD,
				InputConstants.KEY_R, category));
		ClientTickEvents.END_CLIENT_TICK.register(MobilityClient::tick);
	}

	private static void tick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null) {
			return;
		}
		while (ram.consumeClick()) {
			if (Mobility.isWearing(player, ModItems.RAM_HORNS) && ClientPlayNetworking.canSend(RamPayload.TYPE)) {
				ClientPlayNetworking.send(RamPayload.INSTANCE);
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
		boolean clinging = Mobility.isClinging(player);
		if (clinging) {
			Vec3 v = player.getDeltaMovement();
			if (jumpPressed && wallJumpsLeft > 0) {
				// Salto de pared: subes y te vuelves a pegar más arriba.
				wallJumpsLeft--;
				player.setDeltaMovement(v.x, Mobility.WALL_JUMP_SPEED, v.z);
			} else if (v.y < 0.08) {
				// Pegado: compensa justo la gravedad del próximo tick para quedarse quieto.
				player.setDeltaMovement(v.x * 0.5, 0.08, v.z * 0.5);
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
