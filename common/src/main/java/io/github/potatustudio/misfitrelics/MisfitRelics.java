package io.github.potatustudio.misfitrelics;

import io.github.potatustudio.misfitrelics.bat.BatCollar;
import io.github.potatustudio.misfitrelics.combat.CombatRelics;
import io.github.potatustudio.misfitrelics.mobility.Mobility;
import io.github.potatustudio.misfitrelics.plague.PlagueRings;
import io.github.potatustudio.misfitrelics.registry.ModAttachments;
import io.github.potatustudio.misfitrelics.registry.ModComponents;
import io.github.potatustudio.misfitrelics.registry.ModEffects;
import io.github.potatustudio.misfitrelics.registry.ModEntityTypes;
import io.github.potatustudio.misfitrelics.registry.ModGameRules;
import io.github.potatustudio.misfitrelics.registry.ModItems;
import io.github.potatustudio.misfitrelics.registry.ModLoot;
import io.github.potatustudio.misfitrelics.registry.ModNetworking;
import io.github.potatustudio.misfitrelics.registry.ModParticles;
import io.github.potatustudio.misfitrelics.registry.ModPotions;
import io.github.potatustudio.misfitrelics.relic.ServerState;
import io.github.potatustudio.misfitrelics.trumpet.TrumpetSongs;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Punto de entrada común (lo llaman los inicializadores de Fabric y de NeoForge). */
public final class MisfitRelics {
	public static final String MOD_ID = "misfitrelics";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private MisfitRelics() {
	}

	/**
	 * Las claves de datos de entidad tienen que existir antes de que el loader instale su proveedor (que las registra),
	 * así que cada loader llama esto primero.
	 */
	public static void declareEntityData() {
		ModAttachments.init();
	}

	public static void init() {
		ModGameRules.init();
		ModParticles.init();
		ModComponents.init();
		ModEffects.init();
		ModPotions.init();
		ModEntityTypes.init();
		ModItems.init();
		ModLoot.init();
		ModNetworking.init();
		ServerState.init();
		TrumpetSongs.init();
		PlagueRings.init();
		BatCollar.init();
		Mobility.init();
		CombatRelics.init();
		LOGGER.info("Misfit Relics cargado");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
