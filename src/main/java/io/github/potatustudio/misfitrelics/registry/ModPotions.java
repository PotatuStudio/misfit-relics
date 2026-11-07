package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

/**
 * Pociones de Hedor. Las recetas de destilería son datos (26.3+):
 * ver data/misfitrelics/recipe/brewing/.
 */
public final class ModPotions {
	public static final Holder<Potion> STENCH = register("stench",
			new MobEffectInstance(ModEffects.STENCH, 900));
	public static final Holder<Potion> LONG_STENCH = register("long_stench",
			new MobEffectInstance(ModEffects.STENCH, 1800));
	public static final Holder<Potion> STRONG_STENCH = register("strong_stench",
			new MobEffectInstance(ModEffects.STENCH, 440, 1));

	private ModPotions() {
	}

	private static Holder<Potion> register(String path, MobEffectInstance effect) {
		// El nombre "stench" se usa para la traducción en las tres variantes, como en vanilla.
		return Registry.registerForHolder(BuiltInRegistries.POTION, MisfitRelics.id(path), new Potion("stench", effect));
	}

	public static void init() {
		// Fuerza la carga de la clase para registrar las pociones.
	}
}
