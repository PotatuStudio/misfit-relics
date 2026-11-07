package io.github.potatustudio.misfitrelics.registry;

import io.github.potatustudio.misfitrelics.MisfitRelics;
import io.github.potatustudio.misfitrelics.spud.SpudProjectile;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntityTypes {
	private static final ResourceKey<EntityType<?>> SPUD_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, MisfitRelics.id("spud"));

	/** Papa disparada por el cañón. */
	public static final EntityType<SpudProjectile> SPUD = Registry.register(BuiltInRegistries.ENTITY_TYPE, SPUD_KEY,
			EntityType.Builder.<SpudProjectile>of(SpudProjectile::new, MobCategory.MISC)
					.sized(0.25f, 0.25f)
					.clientTrackingRange(4)
					.updateInterval(10)
					.build(SPUD_KEY));

	private ModEntityTypes() {
	}

	public static void init() {
		// Fuerza la carga de la clase para registrar las entidades.
	}
}
