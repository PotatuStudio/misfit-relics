package io.github.potatustudio.misfitrelics.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.potatustudio.misfitrelics.MisfitRelics;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

public final class ModParticles {
	private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(MisfitRelics.MOD_ID, Registries.PARTICLE_TYPE);

	/** Moscas que revolotean alrededor de quien tiene Hedor. Textura: textures/particle/fly.png. */
	public static final RegistrySupplier<SimpleParticleType> FLY = PARTICLES.register("fly", () -> new SimpleParticleType(false) {
	});

	private ModParticles() {
	}

	public static void init() {
		PARTICLES.register();
	}
}
