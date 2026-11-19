package io.github.potatustudio.misfitrelics.client;

import io.github.potatustudio.misfitrelics.network.EcholocationPulsePayload;
import java.util.HashMap;
import java.util.Map;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
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
		NetworkManager.registerReceiver(NetworkManager.s2c(), EcholocationPulsePayload.TYPE, EcholocationPulsePayload.CODEC,
				(payload, context) -> context.queue(() -> {
					long until = clientTicks + payload.ticks();
					for (int id : payload.entityIds()) {
						MARKED.put(id, until);
					}
				}));
		ClientTickEvent.CLIENT_POST.register(EcholocationClient::tick);
		ClientPlayerEvent.CLIENT_PLAYER_RESPAWN.register((oldPlayer, newPlayer) -> MARKED.clear());
		ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> MARKED.clear());
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
