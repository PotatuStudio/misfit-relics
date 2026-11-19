package io.github.potatustudio.misfitrelics.fabric.client;

import io.github.potatustudio.misfitrelics.client.MisfitRelicsClient;
import net.fabricmc.api.ClientModInitializer;

public final class MisfitRelicsFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MisfitRelicsClient.init();
	}
}
