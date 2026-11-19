package io.github.potatustudio.misfitrelics.registry;

import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import io.github.potatustudio.misfitrelics.bat.BatCollar;
import io.github.potatustudio.misfitrelics.mobility.Mobility;
import io.github.potatustudio.misfitrelics.network.BreezeDashPayload;
import io.github.potatustudio.misfitrelics.network.EcholocatePayload;
import io.github.potatustudio.misfitrelics.network.EcholocationPulsePayload;
import io.github.potatustudio.misfitrelics.network.RamPayload;
import net.minecraft.server.level.ServerPlayer;

/** Paquetes de red y eventos de servidor del mod. */
public final class ModNetworking {
	private ModNetworking() {
	}

	public static void init() {
		NetworkManager.registerReceiver(NetworkManager.c2s(), EcholocatePayload.TYPE, EcholocatePayload.CODEC,
				(payload, context) -> context.queue(() -> {
					if (context.getPlayer() instanceof ServerPlayer player) {
						BatCollar.echolocate(player);
					}
				}));
		NetworkManager.registerReceiver(NetworkManager.c2s(), BreezeDashPayload.TYPE, BreezeDashPayload.CODEC,
				(payload, context) -> context.queue(() -> {
					if (context.getPlayer() instanceof ServerPlayer player) {
						Mobility.onBreezeDash(player);
					}
				}));
		NetworkManager.registerReceiver(NetworkManager.c2s(), RamPayload.TYPE, RamPayload.CODEC,
				(payload, context) -> context.queue(() -> {
					if (context.getPlayer() instanceof ServerPlayer player) {
						Mobility.onRam(player);
					}
				}));
		// En el cliente, el tipo lo registra el receptor (EcholocationClient); en el servidor dedicado, aquí.
		if (Platform.getEnvironment() == Env.SERVER) {
			NetworkManager.registerS2CPayloadType(EcholocationPulsePayload.TYPE, EcholocationPulsePayload.CODEC);
		}

		EntityEvent.LIVING_DAMAGE_POST.register((entity, source, baseDamage, damageTaken, blocked) ->
				BatCollar.onDamageDealt(entity, source, damageTaken, blocked));
	}
}
