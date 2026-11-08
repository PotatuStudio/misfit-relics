package io.github.potatustudio.misfitrelics.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.bat.BatCollar;
import io.github.potatustudio.misfitrelics.network.EcholocatePayload;
import io.github.potatustudio.misfitrelics.registry.ModEntityTypes;
import io.github.potatustudio.misfitrelics.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.particle.FireflyParticle;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class MisfitRelicsClient implements ClientModInitializer {
	/** De cada cuántas partículas de mosca que pide el efecto se crea una. */
	private static final int FLY_KEEP_ONE_IN = 4;

	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(MisfitRelics.id("main"));

	/** Ecolocación del collar del murciélago. Por defecto: B. */
	private static final KeyMapping ECHOLOCATE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			BatCollar.ECHOLOCATE_KEY, InputConstants.Type.KEYBOARD, InputConstants.KEY_B, CATEGORY));

	@Override
	public void onInitializeClient() {
		// La papa disparada se dibuja como el ítem de su munición (papa o papa venenosa).
		EntityRendererRegistry.register(ModEntityTypes.SPUD, ThrownItemRenderer::new);
		EntityRendererRegistry.register(ModEntityTypes.ALLIED_BAT, BatRenderer::new);
		// Moscas del Hedor: comportamiento errático de la luciérnaga vanilla con textura propia. Los efectos sueltan
		// partículas casi en cada tick y las luciérnagas viven mucho, así que solo se crea una de cada FLY_KEEP_ONE_IN.
		ParticleProviderRegistry.getInstance().register(ModParticles.FLY, sprites -> {
			FireflyParticle.FireflyProvider firefly = new FireflyParticle.FireflyProvider(sprites);
			return (type, level, x, y, z, dx, dy, dz, random) -> random.nextInt(FLY_KEEP_ONE_IN) == 0
					? firefly.createParticle(type, level, x, y, z, dx, dy, dz, random)
					: null;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (ECHOLOCATE.consumeClick()) {
				// El servidor comprueba si lleva el collar y el enfriamiento.
				if (client.player != null && ClientPlayNetworking.canSend(EcholocatePayload.TYPE)) {
					ClientPlayNetworking.send(EcholocatePayload.INSTANCE);
				}
			}
		});
	}
}
