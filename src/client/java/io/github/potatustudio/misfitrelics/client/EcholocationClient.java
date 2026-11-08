package io.github.potatustudio.misfitrelics.client;

import io.github.potatustudio.misfitrelics.network.EcholocationPulsePayload;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/** Contorno de la ecolocación, solo en el cliente de quien la usó (ver MinecraftMixin). */
public final class EcholocationClient {
	/** Id de entidad → tick del cliente en que deja de verse. */
	private static final Map<Integer, Long> MARKED = new HashMap<>();
	private static long clientTicks;

	private EcholocationClient() {
	}

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(EcholocationPulsePayload.TYPE, (payload, context) -> {
			long until = clientTicks + payload.ticks();
			for (int id : payload.entityIds()) {
				MARKED.put(id, until);
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(EcholocationClient::tick);
	}

	private static void tick(Minecraft client) {
		clientTicks++;
		if (client.level == null) {
			MARKED.clear();
			return;
		}
		MARKED.values().removeIf(until -> until <= clientTicks);
	}

	public static boolean isMarked(Entity entity) {
		return MARKED.containsKey(entity.getId());
	}
}
