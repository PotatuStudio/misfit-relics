package io.github.potatustudio.misfitrelics.neoforge.client;

import io.github.potatustudio.misfitrelics.client.MisfitRelicsClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Cliente de NeoForge: inicialización común y renderizadores de Curios para las reliquias puestas. */
public final class MisfitRelicsNeoForgeClient {
	private MisfitRelicsNeoForgeClient() {
	}

	public static void init(IEventBus modBus) {
		MisfitRelicsClient.init();
		modBus.addListener(FMLClientSetupEvent.class, event -> event.enqueueWork(TrinketsJsonCurioRenderer::registerAll));
		// Solo con RELIC_TEST=1 (./gradlew :neoforge:runClientTest): prueba automática.
		NeoForgeClientTest.initIfRequested();
	}
}
