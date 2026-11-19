package io.github.potatustudio.misfitrelics.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

/**
 * Pociones de Hedor. Las recetas de destilería son datos (26.3+):
 * ver data/misfitrelics/recipe/brewing/.
 */
public final class ModPotions {
	private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(MisfitRelics.MOD_ID, Registries.POTION);

	public static final RegistrySupplier<Potion> STENCH = register("stench", 900, 0);
	public static final RegistrySupplier<Potion> LONG_STENCH = register("long_stench", 1800, 0);
	public static final RegistrySupplier<Potion> STRONG_STENCH = register("strong_stench", 440, 1);

	private ModPotions() {
	}

	private static RegistrySupplier<Potion> register(String path, int ticks, int amplifier) {
		// El nombre "stench" se usa para la traducción en las tres variantes, como en vanilla.
		return POTIONS.register(path, () -> new Potion("stench", new MobEffectInstance(ModEffects.STENCH.asHolder(), ticks, amplifier)));
	}

	public static void init() {
		POTIONS.register();
	}
}
