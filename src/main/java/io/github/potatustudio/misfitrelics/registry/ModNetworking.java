package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.bat.BatCollar;
import io.github.potatustudio.misfitrelics.network.EcholocatePayload;
import io.github.potatustudio.misfitrelics.network.EcholocationPulsePayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Paquetes de red y eventos de servidor del mod. */
public final class ModNetworking {
	private ModNetworking() {
	}

	public static void init() {
		PayloadTypeRegistry.serverboundPlay().register(EcholocatePayload.TYPE, EcholocatePayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(EcholocationPulsePayload.TYPE, EcholocationPulsePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(EcholocatePayload.TYPE,
				(payload, context) -> BatCollar.echolocate(context.player()));

		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) ->
				BatCollar.onDamageDealt(entity, source, damageTaken, blocked));
	}
}
