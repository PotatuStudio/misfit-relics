package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

/** Reglas del Hedor, configurables por mundo con /gamerule. */
public final class ModGameRules {
	/** El Hedor daña a aliados (mascotas, aldeanos, golems, jugadores del mismo equipo). */
	public static final GameRule<Boolean> STENCH_FRIENDLY_FIRE = GameRuleBuilder.forBoolean(true)
			.category(GameRuleCategory.MOBS)
			.buildAndRegister(MisfitRelics.id("stench_friendly_fire"));

	/** El Hedor afecta a quien lo originó. */
	public static final GameRule<Boolean> STENCH_AFFECTS_SOURCE = GameRuleBuilder.forBoolean(true)
			.category(GameRuleCategory.MOBS)
			.buildAndRegister(MisfitRelics.id("stench_affects_source"));

	/** El contagio sigue en cadena; si es false, solo contagian los infectados directos. */
	public static final GameRule<Boolean> STENCH_CHAIN_CONTAGION = GameRuleBuilder.forBoolean(true)
			.category(GameRuleCategory.MOBS)
			.buildAndRegister(MisfitRelics.id("stench_chain_contagion"));

	/** El Hedor puede matar; si es false, deja con medio corazón como el veneno. */
	public static final GameRule<Boolean> STENCH_CAN_KILL = GameRuleBuilder.forBoolean(true)
			.category(GameRuleCategory.MOBS)
			.buildAndRegister(MisfitRelics.id("stench_can_kill"));

	/** Porcentaje (0-100) de zombis que aparecen como portadores del Hedor. */
	public static final GameRule<Integer> STENCH_CARRIER_CHANCE = GameRuleBuilder.forInteger(2)
			.range(0, 100)
			.category(GameRuleCategory.SPAWNING)
			.buildAndRegister(MisfitRelics.id("stench_carrier_chance"));

	/** La trompeta aturde a aliados (mascotas, aldeanos, golems, jugadores del mismo equipo). */
	public static final GameRule<Boolean> TRUMPET_FRIENDLY_FIRE = GameRuleBuilder.forBoolean(true)
			.category(GameRuleCategory.MOBS)
			.buildAndRegister(MisfitRelics.id("trumpet_friendly_fire"));

	private ModGameRules() {
	}

	public static void init() {
		// Fuerza la carga de la clase para registrar las reglas.
	}
}
