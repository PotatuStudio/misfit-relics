package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.stench.StenchEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public final class ModEffects {
	public static final Holder<MobEffect> STENCH =
			Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, MisfitRelics.id("stench"), new StenchEffect());

	private ModEffects() {
	}

	public static void init() {
		// Fuerza la carga de la clase para registrar los efectos.
	}
}
