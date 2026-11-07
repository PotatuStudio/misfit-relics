package io.github.potatustudio.misfitrelics.client;

import io.github.potatustudio.misfitrelics.registry.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class MisfitRelicsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// La papa disparada se dibuja como el ítem de su munición (papa o papa venenosa).
		EntityRendererRegistry.register(ModEntityTypes.SPUD, ThrownItemRenderer::new);
	}
}
