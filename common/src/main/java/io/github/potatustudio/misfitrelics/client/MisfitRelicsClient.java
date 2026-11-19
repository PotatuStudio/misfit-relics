package io.github.potatustudio.misfitrelics.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.bat.BatCollar;
import io.github.potatustudio.misfitrelics.network.EcholocatePayload;
import io.github.potatustudio.misfitrelics.registry.ModEntityTypes;
import io.github.potatustudio.misfitrelics.registry.ModParticles;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.particle.FireflyParticle;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/** Inicialización común del cliente (la llaman ambos loaders). */
public final class MisfitRelicsClient {
	/** De cada cuántas partículas de mosca que pide el efecto se crea una. */
	private static final int FLY_KEEP_ONE_IN = 4;

	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(MisfitRelics.id("main"));

	/** Ecolocación del collar del murciélago. Por defecto: B. */
	private static final KeyMapping ECHOLOCATE = new KeyMapping(BatCollar.ECHOLOCATE_KEY, InputConstants.Type.KEYBOARD,
			InputConstants.KEY_B, CATEGORY);

	private MisfitRelicsClient() {
	}

	public static void init() {
		KeyMappingRegistry.register(ECHOLOCATE);
		// La papa disparada se dibuja como el ítem de su munición (papa o papa venenosa).
		EntityRendererRegistry.register(ModEntityTypes.SPUD, ThrownItemRenderer::new);
		EntityRendererRegistry.register(ModEntityTypes.ALLIED_BAT, BatRenderer::new);
		EntityRendererRegistry.register(ModEntityTypes.KING_BAT, KingBatRenderer::new);
		EcholocationClient.init();
		ArcClient.init();
		MobilityClient.init(CATEGORY);
		// Moscas del Hedor: comportamiento errático de la luciérnaga vanilla con textura propia. Los efectos sueltan
		// partículas casi en cada tick y las luciérnagas viven mucho, así que solo se crea una de cada FLY_KEEP_ONE_IN.
		ParticleProviderRegistry.register(ModParticles.FLY, sprites -> {
			FireflyParticle.FireflyProvider firefly = new FireflyParticle.FireflyProvider(sprites);
			return (type, level, x, y, z, dx, dy, dz, random) -> random.nextInt(FLY_KEEP_ONE_IN) == 0
					? firefly.createParticle(type, level, x, y, z, dx, dy, dz, random)
					: null;
		});

		ClientTickEvent.CLIENT_POST.register(client -> {
			while (ECHOLOCATE.consumeClick()) {
				// El servidor comprueba si lleva el collar y el enfriamiento.
				if (client.player != null && NetworkManager.canServerReceive(EcholocatePayload.TYPE)) {
					NetworkManager.sendToServer(EcholocatePayload.INSTANCE);
				}
			}
		});
	}
}
