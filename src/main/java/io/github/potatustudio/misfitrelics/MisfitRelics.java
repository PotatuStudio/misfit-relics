package io.github.potatustudio.misfitrelics;

import io.github.potatustudio.misfitrelics.registry.ModAttachments;
import io.github.potatustudio.misfitrelics.registry.ModEffects;
import io.github.potatustudio.misfitrelics.registry.ModEntityTypes;
import io.github.potatustudio.misfitrelics.registry.ModGameRules;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import io.github.potatustudio.misfitrelics.registry.ModLoot;
import io.github.potatustudio.misfitrelics.registry.ModNetworking;
import io.github.potatustudio.misfitrelics.trumpet.TrumpetSongs;
import io.github.potatustudio.misfitrelics.registry.ModParticles;
import io.github.potatustudio.misfitrelics.registry.ModPotions;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MisfitRelics implements ModInitializer {
	public static final String MOD_ID = "misfitrelics";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModGameRules.init();
		ModParticles.init();
		ModAttachments.init();
		ModEffects.init();
		ModPotions.init();
		ModEntityTypes.init();
		ModItems.init();
		ModLoot.init();
		ModNetworking.init();
		TrumpetSongs.init();
		LOGGER.info("Misfit Relics cargado");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
