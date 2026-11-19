package io.github.potatustudio.misfitrelics.registry;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;

/** Reglas del Hedor y de la trompeta, configurables por mundo con /gamerule. Se registran igual que las de vanilla. */
public final class ModGameRules {
	private static final DeferredRegister<GameRule<?>> RULES = DeferredRegister.create(MisfitRelics.MOD_ID, Registries.GAME_RULE);

	/** El Hedor daña a aliados (mascotas, aldeanos, golems, jugadores del mismo equipo). */
	public static final RegistrySupplier<GameRule<Boolean>> STENCH_FRIENDLY_FIRE = bool("stench_friendly_fire", GameRuleCategory.MOBS, true);

	/** El Hedor afecta a quien lo originó. */
	public static final RegistrySupplier<GameRule<Boolean>> STENCH_AFFECTS_SOURCE = bool("stench_affects_source", GameRuleCategory.MOBS, true);

	/** El contagio sigue en cadena; si es false, solo contagian los infectados directos. */
	public static final RegistrySupplier<GameRule<Boolean>> STENCH_CHAIN_CONTAGION = bool("stench_chain_contagion", GameRuleCategory.MOBS, true);

	/** El Hedor puede matar; si es false, deja con medio corazón como el veneno. */
	public static final RegistrySupplier<GameRule<Boolean>> STENCH_CAN_KILL = bool("stench_can_kill", GameRuleCategory.MOBS, true);

	/** Porcentaje (0-100) de zombis que aparecen como portadores del Hedor. */
	public static final RegistrySupplier<GameRule<Integer>> STENCH_CARRIER_CHANCE =
			integer("stench_carrier_chance", GameRuleCategory.SPAWNING, 16, 0, 100);

	/** La trompeta aturde a aliados (mascotas, aldeanos, golems, jugadores del mismo equipo). */
	public static final RegistrySupplier<GameRule<Boolean>> TRUMPET_FRIENDLY_FIRE = bool("trumpet_friendly_fire", GameRuleCategory.MOBS, true);

	private ModGameRules() {
	}

	private static RegistrySupplier<GameRule<Boolean>> bool(String path, GameRuleCategory category, boolean defaultValue) {
		return RULES.register(path, () -> new GameRule<>(category, GameRuleType.BOOL, BoolArgumentType.bool(),
				GameRuleTypeVisitor::visitBoolean, Codec.BOOL, value -> value ? 1 : 0, defaultValue, FeatureFlagSet.of()));
	}

	private static RegistrySupplier<GameRule<Integer>> integer(String path, GameRuleCategory category, int defaultValue, int min, int max) {
		return RULES.register(path, () -> new GameRule<>(category, GameRuleType.INT, IntegerArgumentType.integer(min, max),
				GameRuleTypeVisitor::visitInteger, Codec.intRange(min, max), value -> value, defaultValue, FeatureFlagSet.of()));
	}

	public static void init() {
		RULES.register();
	}
}
