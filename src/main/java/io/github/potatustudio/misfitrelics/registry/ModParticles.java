package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModParticles {
	/** Moscas que revolotean alrededor de quien tiene Hedor. Textura: textures/particle/fly.png. */
	public static final SimpleParticleType FLY =
			Registry.register(BuiltInRegistries.PARTICLE_TYPE, MisfitRelics.id("fly"), FabricParticleTypes.simple());

	private ModParticles() {
	}

	public static void init() {
		// Fuerza la carga de la clase para registrar las partículas.
	}
}
